package net.forthecrown.core.challenge;

import it.unimi.dsi.fastutil.ints.IntIntMutablePair;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.commands.click.ClickableTextNode;
import net.forthecrown.commands.click.ClickableTexts;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.economy.TransactionType;
import net.forthecrown.economy.Transactions;
import net.forthecrown.user.User;
import net.forthecrown.utils.book.BookBuilder;
import net.forthecrown.utils.text.TextInfo;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.*;

import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;

public class ChallengeBook {
    /** Pay challenge cost */
    public static final int PAY_COST = 10_000;

    /** Clickable text node used to run the pay challenge logic */
    public static final ClickableTextNode PAY_NODE = ClickableTexts.register(
            new ClickableTextNode("challenge_pay")
                    .setExecutor(user -> {
                        if (!user.hasBalance(PAY_COST)) {
                            throw Exceptions.cannotAfford(PAY_COST);
                        }

                        Challenges.apply("daily/pay", challenge -> {
                            if (!challenge.canComplete(user)) {
                                return;
                            }

                            user.removeBalance(PAY_COST);
                            challenge.trigger(user);

                            user.sendMessage(
                                    Text.format(
                                            "Paid &e{0, rhines}&r to complete challenge!",
                                            NamedTextColor.GRAY,
                                            PAY_COST
                                    )
                            );

                            Transactions.builder()
                                    .type(TransactionType.PAY_CHALLENGE)
                                    .sender(user.getUniqueId())
                                    .amount(PAY_COST)
                                    .log();
                        });

                        open(user);
                    })

                    .setPrompt(user -> {
                        return Text.format("[{0, rhines}]",
                                NamedTextColor.DARK_AQUA,
                                PAY_COST
                        )
                                .hoverEvent(text("Click to pay"));
                    })
    );

    public static void open(User user) {
        BookBuilder builder = new BookBuilder()
                .setAuthor("")
                .setTitle("Challenge progress");

        ChallengeEntry entry = ChallengeManager.getInstance()
                .getOrCreateEntry(user.getUniqueId());

        mainPage(builder, entry);

        for (var r: ResetInterval.values()) {
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
        Map<ResetInterval, IntIntPair>
                summary = new EnumMap<>(ResetInterval.class);

        // Count completed challenges by their category
        for (var c: ChallengeManager.getInstance().getActiveChallenges()) {
            // Item challenges are only shown in /shop
            if (c instanceof ItemChallenge) {
                continue;
            }

            boolean completed = Challenges.hasCompleted(c, entry.getId());

            IntIntPair pair = summary.computeIfAbsent(
                    c.getResetInterval(),
                    interval -> new IntIntMutablePair(0, 0)
            );

            pair.left(pair.leftInt() + 1);

            if (completed) {
                pair.right(pair.rightInt() + 1);
            }
        }

        for (var e: summary.entrySet()) {
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
        List<Challenge> activeList = new ObjectArrayList<>();
        activeList.addAll(
                ChallengeManager.getInstance()
                        .getActiveChallenges()
        );

        // Remove challenges that don't match this category
        activeList.removeIf(challenge -> {
            return challenge.getResetInterval() != interval
                    || challenge instanceof ItemChallenge;
        });

        // If there's nothing to display now lol
        if (activeList.isEmpty()) {
            return;
        }

        // Challenge 2 isCompleted map
        Object2BooleanMap<Challenge>
                completed = new Object2BooleanOpenHashMap<>();

        for (var c: activeList) {
            boolean isCompleted = Challenges.hasCompleted(c, entry.getId());
            completed.put(c, isCompleted);
        }

        builder.addCentered(
                text(interval.getDisplayName() + " Challenges")
        );

        float totalProgress = 0.0F;
        float totalRequired = 0.0F;

        for (var e: completed.object2BooleanEntrySet()) {
            var c = e.getKey();
            boolean isCompleted = e.getBooleanValue();

            float progress = entry.getProgress()
                    .getFloat(c);

            float goal = c.getGoal(entry.getUser());

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

            builder.addCentered(
                    Text.format("{0, number, #.#}% done.",
                            NamedTextColor.GRAY,
                            progress
                    )
            );
        }

        builder.addEmptyLine();
        Challenge payChallenge = ChallengeManager.getInstance()
                .getChallengeRegistry()
                .get("daily/pay")
                .orElse(null);

        for (var e: completed.object2BooleanEntrySet()) {
            var c = e.getKey();
            boolean isCompleted = e.getBooleanValue();

            float progress = entry.getProgress()
                    .getFloat(c);

            float goal = c.getGoal(entry.getUser());

            // Ensure that if the challenge is completed, it always
            // shows the goal, not more, not less
            if (isCompleted) {
                progress = goal;
            }

            Component displayName = c.displayName(entry.getUser())
                    .color(null);

            // Hard coded exception for the pay challenge
            if (Objects.equals(payChallenge, c) && !isCompleted) {
                displayName = displayName.append(space())
                        .append(PAY_NODE.prompt(entry.getUser()));
            }

            int displayNameSize = TextInfo.getPxWidth(Text.plain(displayName));
            int filler = BookBuilder.PIXELS_PER_LINE - displayNameSize;

            if (filler > 1) {
                displayName = displayName
                        .append(
                                text(
                                        TextInfo.getFiller(filler)
                                                .replaceAll("`", "."),
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