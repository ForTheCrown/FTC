package net.forthecrown.challenges;

import static net.kyori.adventure.text.Component.text;

import it.unimi.dsi.fastutil.ints.IntIntMutablePair;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.forthecrown.registry.Holder;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextInfo;
import net.forthecrown.user.User;
import net.forthecrown.utils.BookBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ChallengeBook {

  public static void open(User user) {
    BookBuilder builder = new BookBuilder()
        .setAuthor("")
        .setTitle("Challenge progress");

    ChallengeManager manager = Challenges.getManager();
    ChallengeEntry entry = manager.getEntry(user.getUniqueId());

    mainPage(builder, entry);

    for (var r : ResetInterval.values()) {
      challengePage(builder, entry, r);
    }

    user.openBook(builder.build());
  }

  private static void mainPage(BookBuilder builder, ChallengeEntry entry) {
    builder.addCentered(text("Challenges"))
        .addEmptyLine()
        .addText(text("Progress:"));

    // Left: Total
    // Right: Completed
    Map<ResetInterval, IntIntPair> summary = new EnumMap<>(ResetInterval.class);
    ChallengeManager manager = Challenges.getManager();

    // Count completed challenges by their category
    for (var holder : manager.getActiveChallenges()) {
      var c = holder.getValue();

      // Item challenges are only shown in /shop
      if (c instanceof ItemChallenge) {
        continue;
      }

      boolean completed = entry.hasCompleted(c);

      IntIntPair pair = summary.computeIfAbsent(
          c.getResetInterval(),
          interval -> new IntIntMutablePair(0, 0)
      );

      pair.left(pair.leftInt() + 1);

      if (completed) {
        pair.right(pair.rightInt() + 1);
      }
    }

    for (var e : summary.entrySet()) {
      int total = e.getValue().leftInt();
      int completed = e.getValue().rightInt();

      // If there's no challenges for this type
      if (total <= 0) {
        continue;
      }

      builder
          .addEmptyLine()
          .addField(
              text(e.getKey().getDisplayName() + " challenges"),

              Text.format("{0, number}/{1, number}",
                  completed >= total
                      ? NamedTextColor.DARK_GREEN
                      : NamedTextColor.GRAY,

                  completed, total
              )
          );
    }

    builder.newPage();
  }

  private static void challengePage(BookBuilder builder,
                                    ChallengeEntry entry,
                                    ResetInterval interval
  ) {
    var manager = Challenges.getManager();

    List<Holder<Challenge>> activeList = new ObjectArrayList<>();
    activeList.addAll(manager.getActiveChallenges());

    // Remove challenges that don't match this category
    activeList.removeIf(holder -> {
      var challenge = holder.getValue();
      return challenge.getResetInterval() != interval || challenge instanceof ItemChallenge;
    });

    // If there's nothing to display now lol
    if (activeList.isEmpty()) {
      return;
    }

    // Challenge 2 isCompleted map
    Object2BooleanMap<Holder<Challenge>> completed = new Object2BooleanOpenHashMap<>();

    for (var c : activeList) {
      boolean isCompleted = entry.hasCompleted(c);
      completed.put(c, isCompleted);
    }

    builder.addCentered(text(interval.getDisplayName() + " Challenges"));

    float totalProgress = 0.0F;
    float totalRequired = 0.0F;

    for (var e : completed.object2BooleanEntrySet()) {
      var c = e.getKey();
      boolean isCompleted = e.getBooleanValue();

      float progress = entry.getProgress(e.getKey());

      float goal = c.getValue().getGoal(entry.getUser());

      ++totalRequired;

      // Since just adding the total goal and progress of each challenge
      // together produces wrong results for the percentage count at the
      // top of the page, the percentage value is calculated as a sum of
      // the percentage of each challenge's completion rate
      if (isCompleted || progress >= goal) {
        ++totalProgress;
      } else {
        totalProgress += (progress / goal);
      }
    }

    // I don't think this will happen, as it could only
    // happen if each challenge had a goal of 0 lol
    if (totalRequired != 0.0F) {
      double progress = (totalProgress / totalRequired) * 100;
      builder.addCentered(Text.format("{0, number, #.#}% done.", NamedTextColor.GRAY, progress));
    }

    builder.addEmptyLine();

    for (var e : completed.object2BooleanEntrySet()) {
      var c = e.getKey();
      boolean isCompleted = e.getBooleanValue();

      float progress = entry.getProgress(e.getKey());
      float goal = c.getValue().getGoal(entry.getUser());

      // Ensure that if the challenge is completed, it always
      // shows the goal, not more, not less
      if (isCompleted) {
        progress = goal;
      }

      Component displayName = c.getValue().displayName(entry.getUser()).color(null);

      int displayNameSize = TextInfo.getPxWidth(Text.plain(displayName));
      int filler = BookBuilder.PIXELS_PER_LINE - displayNameSize;

      if (filler > 1) {
        displayName = displayName
            .append(
                text(TextInfo.getFiller(filler).replaceAll("`", "."),
                    NamedTextColor.GRAY
                )
            );
      }

      builder
          .addText(displayName)

          .justifyRight(
              Text.format("{0, number, -floor}/{1, number}",
                      isCompleted
                          ? NamedTextColor.DARK_GREEN
                          : NamedTextColor.GRAY,

                      progress, goal
                  )
                  .hoverEvent(
                      text(isCompleted
                          ? "Completed"
                          : "Uncompleted"
                      )
                  )
          );
    }

    builder.newPage();
  }
}