package net.forthecrown.core.challenge;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.user.User;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;

@Getter
@RequiredArgsConstructor(staticName = "of")
public class ChallengePlaceholders {
    private static final ImmutableMap<String, PlaceholderFormatter>
            FORMATTERS = createFormatter();

    private final Holder<Challenge> holder;

    public static ChallengePlaceholders of(Challenge challenge) {
        return of(
                ChallengeManager.getInstance()
                        .getChallengeRegistry()
                        .getHolderByValue(challenge)
                        .orElseThrow()
        );
    }

    public Component format(Component text, User user) {
        Component result = text;

        for (var e: FORMATTERS.entrySet()) {
            Component replaceValue = e.getValue()
                    .format(holder, user);

            if (replaceValue == null) {
                continue;
            }

            result = result.replaceText(
                    TextReplacementConfig.builder()
                            .matchLiteral("%" + e.getKey())
                            .replacement(replaceValue)
                            .build()
            );
        }

        return result;
    }

    static ImmutableMap<String, PlaceholderFormatter> createFormatter() {
        ImmutableMap.Builder<String, PlaceholderFormatter>
                builder = ImmutableMap.builder();

        builder.put("goal", (holder1, user) -> {
            if (user == null) {
                return null;
            }

            return Text.format(
                    "{0, number}",
                    holder1.getValue().getGoal(user)
            );
        });

        builder.put("streak_category", (holder1, user) -> {
            return Component.text(
                    holder1.getValue()
                            .getStreakCategory()
                            .getDisplayName()
            );
        });

        builder.put("type", (holder1, user) -> {
            return Component.text(
                    holder1.getValue()
                            .getResetInterval()
                            .getDisplayName()
            );
        });

        return builder.build();
    }

    private interface PlaceholderFormatter {
        Component format(Holder<Challenge> holder, User user);
    }
}