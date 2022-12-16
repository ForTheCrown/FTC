package net.forthecrown.core.holidays;

import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.user.User;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;

import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class HolidayTags {
    private static final ImmutableMap<String, TagReplace> REPLACERS = createReplacers();

    static ImmutableMap<String, TagReplace> createReplacers() {
        ImmutableMap.Builder<String, TagReplace> builder = ImmutableMap.builder();

        builder.put("name", context -> context.holiday.name());

        builder.put("month", TagReplace.of(context -> {
            return context.time.getMonth()
                    .getDisplayName(TextStyle.FULL_STANDALONE, Locale.ENGLISH);
        }));

        builder.put("wday", TagReplace.of(context -> {
            return context.time.getDayOfWeek()
                    .getDisplayName(TextStyle.FULL_STANDALONE, Locale.ENGLISH);
        }));

        builder.put("date", TagReplace.of(context -> {
            return context.time.getDayOfMonth() + "";
        }));

        builder.put("year", TagReplace.of(context -> {
            return context.time.getYear() + "";
        }));

        builder.put("type", TagReplace.of(context -> {
            return context.holiday.getContainer().isChest() ?
                    "Chest" : "Shulker";
        }));

        // Player name replace
        TagReplace playerReplace = context -> context.user.displayName();
        builder.put("plr", playerReplace);
        builder.put("player", playerReplace);
        builder.put("user", playerReplace);

        builder.put("p_start_month", TagReplace.of(context -> {
            return context.holiday.getPeriod().getStart().getMonth()
                    .getDisplayName(TextStyle.FULL_STANDALONE, Locale.ENGLISH);
        }));

        builder.put("p_end_month", TagReplace.of(context -> {
            if (context.holiday.getPeriod().isExact()) {
                return context.time.getMonth()
                        .getDisplayName(TextStyle.FULL_STANDALONE, Locale.ENGLISH);
            }

            return context.holiday.getPeriod().getEnd().getMonth()
                    .getDisplayName(TextStyle.FULL_STANDALONE, Locale.ENGLISH);
        }));

        builder.put("p_start_date", TagReplace.of(context -> {
            return context.holiday.getPeriod().getStart().getDayOfMonth() + "";
        }));

        builder.put("p_start_end", TagReplace.of(context -> {
            if (context.holiday.getPeriod().isExact()) {
                return context.time.getDayOfMonth() + "";
            }

            return context.holiday.getPeriod().getEnd().getDayOfMonth() + "";
        }));

        return builder.build();
    }

    public static Component replaceTags(String original, User user, Holiday holiday, ZonedDateTime time) {
        return replaceTags(Text.renderString(original), user, holiday, time);
    }

    public static Component replaceTags(Component original, User user, Holiday holiday, ZonedDateTime time) {
        var context = new TagContext(holiday, time, user, original);
        var filtered = original;

        for (var e: REPLACERS.entrySet()) {
            var replacement = e.getValue().getReplaceValue(context);

            filtered = filtered.replaceText(
                    TextReplacementConfig.builder()
                            .matchLiteral("%" + e.getKey())
                            .replacement(replacement)
                            .build()
            );
        }

        return filtered;
    }

    public static CompletableFuture<Suggestions> addSuggestions(SuggestionsBuilder builder) {
        var token = builder.getRemainingLowerCase();

        for (var s: REPLACERS.keySet()) {
            var suggestion = "%" + s;

            if (!CompletionProvider.startsWith(token, suggestion)) {
                continue;
            }

            builder.suggest(suggestion);
        }

        return builder.buildFuture();
    }

    interface TagReplace {
        static TagReplace of(Function<TagContext, String> function) {
            return context -> Text.renderString(function.apply(context));
        }

        Component getReplaceValue(TagContext context);
    }

    record TagContext(Holiday holiday,
                      ZonedDateTime time,
                      User user,
                      Component original
    ) {

    }
}