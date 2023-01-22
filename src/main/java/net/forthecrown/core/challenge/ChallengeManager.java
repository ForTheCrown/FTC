package net.forthecrown.core.challenge;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import lombok.Getter;
import net.forthecrown.core.config.ConfigManager;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.core.module.OnDayChange;
import net.forthecrown.core.module.OnEnable;
import net.forthecrown.core.module.OnLoad;
import net.forthecrown.core.module.OnSave;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.datafix.ChallengesLogFix;
import net.forthecrown.datafix.Transformers;
import net.forthecrown.economy.Economy;
import net.forthecrown.user.User;
import net.forthecrown.utils.Time;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.inventory.menu.Menu;
import net.forthecrown.utils.io.PathUtil;
import org.apache.logging.log4j.Logger;

public class ChallengeManager {

  private static final Logger LOGGER = Loggers.getLogger();

  @Getter
  private static final ChallengeManager instance = new ChallengeManager();

  @Getter
  private LocalDate date;

  private final List<Holder<Challenge>> activeChallenges = new ObjectArrayList<>();

  private final Map<UUID, ChallengeEntry> entries
      = new Object2ObjectOpenHashMap<>();

  @Getter
  private final Registry<Challenge> challengeRegistry
      = Registries.newRegistry();

  private final EnumMap<ResetInterval, Long> resetTimes
      = new EnumMap<>(ResetInterval.class);

  @Getter
  private final ChallengeDataStorage storage;

  @Getter
  private Menu itemChallengeMenu;

  public ChallengeManager() {
    date = LocalDate.now();

    this.storage = new ChallengeDataStorage(
        PathUtil.getPluginDirectory("challenges")
    );

    storage.ensureDefaultsExist();
  }

  // Called reflectively in BootStrap
  @OnEnable
  private static void init() {
    ConfigManager.get()
        .registerConfig(ChallengeConfig.class);
  }

  public ChallengeEntry getEntry(User user) {
    return getEntry(user.getUniqueId());
  }

  public ChallengeEntry getEntry(UUID uuid) {
    return entries.computeIfAbsent(uuid, ChallengeEntry::new);
  }

  public List<Holder<Challenge>> getActiveChallenges() {
    return Collections.unmodifiableList(activeChallenges);
  }

  /**
   * Tests if challenge progress can be made.
   * <p>
   * Tests if challenges are currently locked, this is to prevent a bug with
   * challenges that listen to very commonly triggered events, like the walk
   * event.
   * <p>
   * If true, then no progress can be made during challenge completion. This can
   * come about as a tick tasks are run near the end of a tick, so packets, move
   * events and so forth can occur before the day change reset is triggered, but
   * since it's a new day, the challenges allow themselves to be completed, over
   * and over again.
   *
   * @return True, if challenge progress is locked, false otherwise
   */
  public boolean areChallengesLocked() {
    LocalDate now = LocalDate.now();
    return now.compareTo(date) != 0;
  }

  @OnDayChange
  void onDayChange(ZonedDateTime time) {
    date = time.toLocalDate();

    if (time.getDayOfWeek() == DayOfWeek.MONDAY) {
      // Clear all item challenge's used items
      // list, so they can be selected again
      for (var h : challengeRegistry.entries()) {
        if (!(h.getValue() instanceof ItemChallenge)) {
          continue;
        }

        var container = storage.loadContainer(h);

        if (container.getUsed().isEmpty()) {
          continue;
        }

        container.getUsed().clear();
        storage.saveContainer(container);
      }

      reset(ResetInterval.WEEKLY);
    }

    reset(ResetInterval.DAILY);
  }

  public void reset(ResetInterval interval) {
    Set<Challenge> current = new ObjectOpenHashSet<>();

    activeChallenges.removeIf(holder -> {
      var challenge = holder.getValue();

      if (challenge.getResetInterval() != interval) {
        return false;
      }

      challenge.deactivate();
      current.add(challenge);

      return true;
    });

    entries.values().forEach(entry -> entry.onReset(interval, this));

    if (!interval.shouldRefill()) {
      return;
    }

    List<Holder<Challenge>> challenges = selectRandom(interval);

    if (interval.getMax() != -1
        && challenges.size() > interval.getMax()
        && !ChallengeConfig.allowRepeatingChallenges
    ) {
      challenges.removeIf(holder -> {
        return current.contains(holder.getValue());
      });

      if (challenges.size() < interval.getMax()) {
        challenges = selectRandom(interval);
      }
    }

    if (challenges.isEmpty()) {
      LOGGER.warn("Found no {} challenges to use!", interval);
      return;
    }

    int required = Math.min(
        challenges.size(),
        interval.getMax()
    );

    Set<Holder<Challenge>> picked = Util.pickUniqueEntries(
        challenges,
        Util.RANDOM,
        required
    );

    resetTimes.put(interval, System.currentTimeMillis());

    picked.forEach(holder -> {
      activate(holder, true);
    });

    LOGGER.info("Reset all {} challenges, added {} new ones",
        interval, picked.size()
    );
  }

  private List<Holder<Challenge>> selectRandom(ResetInterval interval) {
    List<Holder<Challenge>> challenges = challengeRegistry.entries()
        .stream()
        .filter(h -> {
          var c = h.getValue();
          return c.getResetInterval() == interval;
        })
        .collect(ObjectArrayList.toList());

    challenges.removeIf(holder -> {
      var c = holder.getValue();

      if (c instanceof ItemChallenge) {
        activate(holder, true);
        return true;
      }

      return false;
    });

    return challenges;
  }

  public void activate(Holder<Challenge> holder, boolean resetting) {
    activeChallenges.add(holder);

    CompletionStage<String> extra = holder.getValue().activate(resetting);

    if (resetting) {
      extra.whenComplete((s, throwable) -> {
        if (throwable != null) {
          LOGGER.error(
              "Couldn't call activate() on challenge {}",
              holder.getKey(), throwable
          );

          return;
        }

        Challenges.logActivation(holder, s);
      });
    }
  }

  public void clear() {
    activeChallenges.forEach(holder -> holder.getValue().deactivate());
    activeChallenges.clear();

    entries.clear();
  }

  public Collection<ChallengeEntry> getEntries() {
    return Collections.unmodifiableCollection(entries.values());
  }

  /* --------------------------- SERIALIZATION ---------------------------- */

  private void loadChallenges() {
    challengeRegistry.clear();
    storage.loadChallenges(challengeRegistry);
    storage.loadItemChallenges(challengeRegistry);

    var shop = Economy.get().getSellShop();
    itemChallengeMenu = Challenges.createItemMenu(challengeRegistry, shop);
  }

  @OnSave
  public void save() {
    storage.saveEntries(entries.values());
    storage.saveActive(activeChallenges, resetTimes);
  }

  @OnLoad
  public void load() {
    clear();
    loadChallenges();

    Transformers.runTransformer(new ChallengesLogFix(storage));

    storage.loadEntries()
        .resultOrPartial(LOGGER::error)
        .ifPresent(entries1 -> {
          for (var e : entries1) {
            entries.put(e.getId(), e);
          }
        });

    storage.loadActive(activeChallenges, resetTimes, challengeRegistry);

    resetIfRequired(ResetInterval.DAILY);
    resetIfRequired(ResetInterval.WEEKLY);
  }

  private void resetIfRequired(ResetInterval interval) {
    long time = resetTimes.getOrDefault(interval, 0L);
    LocalDate now;

    if (interval == ResetInterval.WEEKLY) {
      now = date.with(
          TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
      );
    } else {
      now = date;
    }

    long epochDay = now.toEpochDay();
    long timeDay = Time.localDate(time).toEpochDay();

    var list  = activeChallenges.stream()
        .filter(holder -> holder.getValue().getResetInterval() == interval)
        .toList();

    if (epochDay < timeDay || list.isEmpty()) {
      reset(interval);
    } else {
      list.forEach(holder -> holder.getValue().activate(false));
    }
  }
}