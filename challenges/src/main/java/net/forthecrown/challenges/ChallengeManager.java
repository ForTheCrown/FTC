package net.forthecrown.challenges;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.Loggers;
import net.forthecrown.menu.Menu;
import net.forthecrown.registry.Holder;
import net.forthecrown.registry.Registries;
import net.forthecrown.registry.Registry;
import net.forthecrown.sellshop.SellShopPlugin;
import net.forthecrown.user.User;
import net.forthecrown.utils.Time;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;

public class ChallengeManager {

  private static final Logger LOGGER = Loggers.getLogger();

  @Getter
  private final ChallengeDataStorage storage;

  @Getter
  private final ChallengesPlugin plugin;

  @Getter @Setter
  private LocalDate date;

  @Getter
  private final Registry<Challenge> challengeRegistry = Registries.newRegistry();

  private final List<Holder<Challenge>> activeChallenges = new ObjectArrayList<>();
  private final Map<UUID, ChallengeEntry> entries = new Object2ObjectOpenHashMap<>();
  private final Map<ResetInterval, Long> resetTimes = new EnumMap<>(ResetInterval.class);

  @Getter
  private Menu itemChallengeMenu;

  public ChallengeManager(ChallengesPlugin plugin) {
    this.plugin = plugin;

    this.storage = new ChallengeDataStorage(plugin.getDataFolder().toPath());
    this.storage.ensureDefaultsExist();

    this.date = LocalDate.now();
  }

  public ChallengeEntry getEntry(User user) {
    return getEntry(user.getUniqueId());
  }

  public ChallengeEntry getEntry(UUID uuid) {
    return entries.computeIfAbsent(uuid, uuid1 -> new ChallengeEntry(uuid1, this));
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

  public void reset(ResetInterval interval) {
    Set<Challenge> current = new ObjectOpenHashSet<>();

    activeChallenges.removeIf(holder -> {
      var challenge = holder.getValue();

      if (challenge.getResetInterval() != interval) {
        return false;
      }

      deactivate(holder);
      current.add(challenge);

      return true;
    });

    entries.values().forEach(entry -> entry.onReset(interval, this));

    if (!interval.shouldRefill()) {
      return;
    }

    List<Holder<Challenge>> challenges = selectRandom(interval);
    var config = getPlugin().getPluginConfig();

    if (interval.getMax(config) != -1
        && challenges.size() > interval.getMax(config)
        && !plugin.getPluginConfig().allowRepeatingChallenges
    ) {
      challenges.removeIf(holder -> {
        return current.contains(holder.getValue());
      });

      if (challenges.size() < interval.getMax(config)) {
        challenges = selectRandom(interval);
      }
    }

    if (challenges.isEmpty()) {
      LOGGER.warn("Found no {} challenges to use!", interval);
      return;
    }

    int required = Math.min(challenges.size(), interval.getMax(config));
    Set<Holder<Challenge>> picked = pickUniqueEntries(challenges, required);

    resetTimes.put(interval, System.currentTimeMillis());
    activeChallenges.addAll(picked);

    picked.forEach(holder -> {
      activate(holder, true, false);
    });

    LOGGER.info("Reset all {} challenges, added {} new ones",
        interval, picked.size()
    );
  }

  private static <V> Set<V> pickUniqueEntries(List<V> list, int amount) {
    Random random = new Random();
    Validate.isTrue(amount <= list.size());

    if (amount == 0) {
      return Collections.emptySet();
    }

    if (amount == list.size()) {
      return new ObjectOpenHashSet<>(list);
    }

    Set<V> result = new ObjectOpenHashSet<>();
    V entry;

    while (result.size() < amount) {
      entry = list.get(random.nextInt(list.size()));
      result.add(entry);
    }

    return result;
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
        activate(holder, true, true);
        return true;
      }

      return false;
    });

    return challenges;
  }

  public void deactivate(Holder<Challenge> challenge) {
    try {
      challenge.getValue().deactivate();
    } catch (Throwable t) {
      LOGGER.error("Failed to deactivate challenge '{}'", challenge.getKey(), t);
    }
  }

  public void activate(Holder<Challenge> holder, boolean resetting, boolean addToList) {
    if (addToList) {
      activeChallenges.add(holder);
    }

    CompletionStage<String> extra;

    try {
      extra = holder.getValue().activate(resetting);
    } catch (Throwable t) {
      LOGGER.error("Failed to activate challenge '{}'", holder.getKey(), t);

      activeChallenges.remove(holder);
      deactivate(holder);

      return;
    }

    if (resetting) {
      extra.whenComplete((s, throwable) -> {
        if (throwable != null) {
          LOGGER.error(
              "Couldn't call activate() on challenge {}",
              holder.getKey(), throwable
          );

          return;
        }

        LOGGER.debug("Activated challenge {}: {}", holder.getKey(), s);
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

    var shop = SellShopPlugin.getPlugin().getSellShop();
    itemChallengeMenu = Challenges.createItemMenu(challengeRegistry, shop);
  }

  public void save() {
    storage.saveEntries(entries.values());
    storage.saveActive(activeChallenges, resetTimes);
  }

  public void load() {
    clear();
    loadChallenges();

    storage.loadEntries(this)
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
      now = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
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