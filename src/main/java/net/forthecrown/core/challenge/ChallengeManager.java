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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import net.forthecrown.core.FTC;
import net.forthecrown.core.config.ConfigManager;
import net.forthecrown.core.module.OnDayChange;
import net.forthecrown.core.module.OnEnable;
import net.forthecrown.core.module.OnLoad;
import net.forthecrown.core.module.OnSave;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.economy.Economy;
import net.forthecrown.log.DateRange;
import net.forthecrown.log.LogManager;
import net.forthecrown.log.LogQuery;
import net.forthecrown.utils.Time;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.inventory.menu.Menu;
import net.forthecrown.utils.io.PathUtil;
import org.apache.logging.log4j.Logger;

public class ChallengeManager {

  private static final Logger LOGGER = FTC.getLogger();

  @Getter
  private static final ChallengeManager instance = new ChallengeManager();

  @Getter
  private LocalDate date;

  private final List<Challenge> activeChallenges = new ObjectArrayList<>();

  private final Map<UUID, ChallengeEntry> entries = new Object2ObjectOpenHashMap<>();

  @Getter
  private final Registry<Challenge> challengeRegistry = Registries.newRegistry();

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

  public ChallengeEntry getEntry(UUID uuid) {
    return entries.get(uuid);
  }

  public ChallengeEntry getOrCreateEntry(UUID uuid) {
    return entries.computeIfAbsent(uuid, ChallengeEntry::new);
  }

  public List<Challenge> getActiveChallenges() {
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

    activeChallenges.removeIf(challenge -> {
      if (challenge.getResetInterval() != interval) {
        return false;
      }

      challenge.deactivate();
      current.add(challenge);

      return true;
    });

    entries.values().forEach(entry -> entry.onReset(interval));

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

    picked.forEach(holder -> {
      activate(holder, true);
    });

    LOGGER.debug("Reset all {} challenges, added {} new ones",
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
    activeChallenges.add(holder.getValue());

    var extra = holder.getValue()
        .activate(resetting);

    if (resetting) {
      Challenges.logActivation(holder, extra);
    }
  }

  public void clear() {
    activeChallenges.forEach(Challenge::deactivate);
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

    LOGGER.debug("creating challenge item menu");
    var shop = Economy.get().getSellShop();
    itemChallengeMenu = Challenges.createItemMenu(challengeRegistry, shop);

    if (shop.getMainMenu() != null) {
      shop.load();
    }
  }

  @OnSave
  public void save() {
    storage.saveEntries(entries.values(), challengeRegistry);
  }

  @OnLoad
  public void load() {
    LOGGER.debug("load() in {} called", getClass().getSimpleName());

    clear();
    loadChallenges();

    storage.loadEntries(challengeRegistry)
        .resultOrPartial(LOGGER::error)
        .ifPresent(entries1 -> {
          for (var e : entries1) {
            entries.put(e.getId(), e);
          }
        });

    loadActive();
  }

  private static DateRange getQueryRange() {
    var now = LocalDate.now();
    return DateRange.between(
        now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
        now
    );
  }

  private void loadActive() {
    var list = LogManager.getInstance()
        .queryLogs(
            LogQuery.builder(ChallengeLogs.ACTIVE)
                .queryRange(getQueryRange())

                .field(ChallengeLogs.A_TYPE)
                .add(Objects::nonNull)

                .entryPredicate(entry -> {
                  var type = entry.get(ChallengeLogs.A_TYPE);

                  if (type != ResetInterval.DAILY) {
                    return true;
                  }

                  var time = entry.getDate();

                  var local = Time.localTime(time);
                  var now = LocalDate.now();

                  return local.getDayOfWeek() == now.getDayOfWeek();
                })

                .build()
        );

    if (list.isEmpty()) {
      reset(ResetInterval.DAILY);
      reset(ResetInterval.WEEKLY);

      return;
    }

    list.forEach(entry -> {
      String key = entry.get(ChallengeLogs.A_CHALLENGE);

      challengeRegistry.getHolder(key)
          .ifPresentOrElse(
              challenge -> {
                activate(challenge, false);
                LOGGER.debug("Loaded active challenge {}", key);
              },

              () -> {
                LOGGER.warn(
                    "Unknown challenge found in data logs: '{}'",
                    key
                );
              }
          );
    });
  }
}