package net.forthecrown.core.challenge;

import static net.forthecrown.core.challenge.ChallengeLogs.ACTIVE;
import static net.forthecrown.core.challenge.ChallengeLogs.A_CHALLENGE;
import static net.forthecrown.core.challenge.ChallengeLogs.A_EXTRA;
import static net.forthecrown.core.challenge.ChallengeLogs.A_TYPE;
import static net.forthecrown.core.challenge.ChallengeLogs.COMPLETED;
import static net.forthecrown.core.challenge.ChallengeLogs.C_CHALLENGE;
import static net.forthecrown.core.challenge.ChallengeLogs.C_PLAYER;
import static net.forthecrown.core.challenge.ChallengeLogs.STREAK_SCHEMA;
import static net.forthecrown.core.challenge.ChallengeLogs.S_CATEGORY;
import static net.forthecrown.core.challenge.ChallengeLogs.S_PLAYER;

import co.aikar.timings.Timing;
import com.google.common.base.Strings;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.function.Consumer;
import net.forthecrown.core.FTC;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.economy.sell.SellShop;
import net.forthecrown.economy.sell.SellShopNodes;
import net.forthecrown.log.DataLogs;
import net.forthecrown.log.DateRange;
import net.forthecrown.log.LogEntry;
import net.forthecrown.log.LogQuery;
import net.forthecrown.user.User;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.Menu;
import net.forthecrown.utils.inventory.menu.MenuBuilder;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.inventory.menu.Menus;
import net.forthecrown.utils.inventory.menu.Slot;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

/**
 * Utility class for challenge-related functions
 */
public final class Challenges {
  private Challenges() {}

  public static final String
      METHOD_ON_RESET = "onReset",
      METHOD_ON_ACTIVATE = "onActivate",
      METHOD_CAN_COMPLETE = "canComplete",
      METHOD_ON_COMPLETE = "onComplete",
      METHOD_GET_PLAYER = "getPlayer",
      METHOD_ON_EVENT = "onEvent",

      METHOD_STREAK_INCREASE = "onStreakIncrease";

  public static final Timing COMPLETION_QUERY
      = FTC.timing("Challenge Completion Query");

  public static final Timing STREAK_QUERY
      = FTC.timing("Challenge Streak Query");

  public static void logActivation(Holder<Challenge> challenge, String extra) {
    LogEntry entry = LogEntry.of(ACTIVE)
        .set(A_CHALLENGE, challenge.getKey())
        .set(A_TYPE, challenge.getValue().getResetInterval());

    if (!Strings.isNullOrEmpty(extra)) {
      entry.set(A_EXTRA, extra);
    }

    DataLogs.log(ACTIVE, entry);
  }

  public static void logCompletion(Holder<Challenge> challenge, UUID uuid) {
    LogEntry entry = LogEntry.of(COMPLETED)
        .set(C_PLAYER, uuid)
        .set(C_CHALLENGE, challenge.getKey());

    DataLogs.log(COMPLETED, entry);
  }

  public static boolean hasCompleted(Challenge challenge, UUID uuid) {
    return ChallengeManager.getInstance()
        .getChallengeRegistry()
        .getHolderByValue(challenge)
        .map(holder -> hasCompleted(holder, uuid))
        .orElse(false);
  }

  public static boolean hasCompleted(Holder<Challenge> challenge, UUID uuid) {
    var reset = challenge.getValue()
        .getResetInterval();

    LocalDate start = switch (reset) {
      case DAILY -> LocalDate.now();
      case WEEKLY -> LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
      case MANUAL -> LocalDate.MIN;
    };

    COMPLETION_QUERY.startTiming();
    var result = !DataLogs.query(
        LogQuery.builder(COMPLETED)
            .maxResults(1)
            .queryRange(DateRange.between(start, LocalDate.now()))

            .field(C_PLAYER)
            .add(uuid1 -> Objects.equals(uuid1, uuid))

            .field(C_CHALLENGE)
            .add(s -> Objects.equals(s, challenge.getKey()))

            .build()
    ).isEmpty();
    COMPLETION_QUERY.stopTiming();

    return result;
  }

  public static void trigger(String challengeName, Object input) {
    apply(challengeName, challenge -> challenge.trigger(input));
  }

  public static boolean isActive(Challenge challenge) {
    return ChallengeManager.getInstance()
        .getActiveChallenges()
        .contains(challenge);
  }

  public static void apply(Challenge challenge,
                           Consumer<Holder<Challenge>> consumer
  ) {
    ChallengeManager.getInstance()
        .getChallengeRegistry()
        .getHolderByValue(challenge)
        .ifPresent(holder -> {
          if (!isActive(holder.getValue())) {
            return;
          }

          consumer.accept(holder);
        });
  }

  public static void apply(String challengeName, Consumer<Challenge> consumer) {
    ChallengeManager.getInstance()
        .getChallengeRegistry()
        .get(challengeName)
        .ifPresent(challenge -> {
          if (!isActive(challenge)) {
            return;
          }

          consumer.accept(challenge);
        });
  }

  static Menu createItemMenu(Registry<Challenge> challenges, SellShop shop) {
    MenuBuilder builder = Menus.builder(Menus.MAX_INV_SIZE - 9)
        .setTitle("Daily Item Challenges")
        .addBorder()

        // < Go back
        .add(Slot.ZERO, SellShopNodes.previousPage(shop))

        // Header
        .add(Slot.of(4), createMenuHeader())

        .add(Slot.of(4, 4),
            MenuNode.builder()
                .setItem((user, context) -> {
                  return ItemStacks.builder(Material.BOOK)
                      .setName("&eInfo")
                      .addLore("&7This challenge is reset daily. Complete it to build a streak.")
                      .addLore("&7The longer your streak, the greater the rewards!")
                      .addLore("")
                      .addLore("&7Rewards include:")
                      .addLore("&7Rhines, Gems, Guild EXP and mob Plushies")
                      .build();
                })
                .build()
        );

    for (var h : challenges.entries()) {
      if (!(h.getValue() instanceof ItemChallenge item)) {
        continue;
      }

      FTC.getLogger().debug("Adding item challenge to shop menu: {}",
          h.getKey()
      );

      builder.add(
          item.getMenuSlot(),
          item.toInvOption()
      );
    }

    return builder
        .build();
  }

  public static MenuNode createMenuHeader() {
    return MenuNode.builder()
        .setItem((user, context) -> {
          var builder = ItemStacks.builder(Material.CLOCK)
              .setName("&bDaily Item Challenges")
              .setFlags(ItemFlag.HIDE_ATTRIBUTES);

          int streak = queryStreak(StreakCategory.ITEMS, user)
              .orElse(0);

          builder.addLore(
              Text.format(
                  "Current streak: {0, number}",
                  NamedTextColor.GRAY,
                  streak
              )
          );

          return builder.build();
        })
        .build();
  }

  public static OptionalInt queryStreak(Challenge challenge, User user) {
    if (user == null
        || challenge.getResetInterval() == ResetInterval.MANUAL
    ) {
      return OptionalInt.empty();
    }

    return queryStreak(challenge.getStreakCategory(), user);
  }

  public static OptionalInt queryStreak(StreakCategory category, User viewer) {
    if (viewer == null) {
      return OptionalInt.empty();
    }

    UUID uuid = viewer.getUniqueId();
    LocalDate start = LocalDate.now();
    DateRange range = null;

    int streak = 0;

    STREAK_QUERY.startTiming();

    // Iterate backwards through date ranges
    // defined by the category
    while (streak < ChallengeConfig.maxStreak) {
      // Range will only be null on the first iteration, meaning this
      // check is a kinda optional check for the current category's
      // timeframe, if it fails, doesn't matter
      if (range == null) {
        range = category.searchRange(start);

        if (hasStreak(range, category, uuid)) {
          ++streak;
        }

        continue;
      }

      // Move the date range backwards
      range = category.moveRange(range);

      // Query streak, if none found, we've reached our
      // final streak count and we stop the loop here
      if (hasStreak(range, category, uuid)) {
        ++streak;
      } else {
        break;
      }
    }

    STREAK_QUERY.stopTiming();

    return streak == 0
        ? OptionalInt.empty()
        : OptionalInt.of(streak);
  }

  public static void logStreak(StreakCategory category, UUID uuid) {
    DataLogs.log(
        STREAK_SCHEMA,

        LogEntry.of(STREAK_SCHEMA)
            .set(S_CATEGORY, category)
            .set(S_PLAYER, uuid)
    );
  }

  private static boolean hasStreak(DateRange dateRange,
                                   StreakCategory category,
                                   UUID uuid
  ) {
    var result = DataLogs.query(
        LogQuery.builder(STREAK_SCHEMA)
            .queryRange(dateRange)
            .maxResults(1)

            .field(S_PLAYER)
            .add(uuid1 -> Objects.equals(uuid1, uuid))

            .field(S_CATEGORY)
            .add(category1 -> Objects.equals(category, category1))

            .build()
    );

    return result.size() >= 1;
  }
}