package net.forthecrown.text.format;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.registry.Keys;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.RomanNumeral;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.math.WorldVec3i;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextReplacementConfig;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.chrono.ChronoZonedDateTime;
import java.util.Date;
import java.util.UUID;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

/**
 * A utility class for formatting components.
 * Similar to {@link java.text.MessageFormat} except way less
 * functional, and tailored towards components instead of strings.
 * <p>
 * It adds some functionality to the standard argument token
 * replacing with the {@link FormatType}s that allow it to do
 * some automatic formatting.
 * <p>
 * Since this copies {@link java.text.MessageFormat} it uses
 * a similar format to it.
 * <blockquote><pre>
 * { <i>ArgumentIndex</i> }
 * { <i>ArgumentIndex</i> , <i>FormatType</i> }
 * { <i>ArgumentIndex</i> , <i>FormatType</i> , <i>Style</i> }
 * </pre></blockquote>
 * <p>
 * The Style is at the moment only used by {@link FormatType#NUMBER},
 * {@link FormatType#DATE} and {@link FormatType#CLASS}, otherwise
 * the styles are useless.
 * <p>
 * As an example, you can tell the formatter to format a date
 * argument like so: "Today's date: {0, date}". You can take
 * this a step further and give it a custom date format with
 * "Today's date: {0, date, d.MMM.yyyy}"
 * @see FormatType
 */
@Getter
@RequiredArgsConstructor
public class ComponentFormat implements ComponentLike {
    /* ----------------------------- CONSTANTS ------------------------------ */

    /**
     * The replacement pattern to use for component's, broadly, it's any
     * text that's enclosed by '{}' chars
     */
    private static final Pattern REPLACE_PATTERN = Pattern.compile("\\{(.+?)}");

    /**
     * Used for parsing the {@link FormatType} in arguments
     */
    private static final EnumArgument<FormatType> TYPE_PARSER = EnumArgument.of(FormatType.class);

    /* ----------------------------- INSTANCE FIELDS ------------------------------ */

    /**
     * The base format
     */
    private final Component format;

    /**
     * Arguments given to this formatter
     */
    private final Object[] args;

    /* ----------------------------- METHODS ------------------------------ */

    /**
     * Formats the given {@link #format}.
     * This will replace the arguments in any part
     * of the text, including in children, and even
     * in {@link net.kyori.adventure.text.TranslatableComponent}
     * arguments.
     *
     * @return The formatted component
     *
     * @throws IllegalStateException Will be thrown if the formatter
     *                               couldn't parse a given argument
     *                               correctly.
     */
    @Override
    public @NotNull Component asComponent() throws IllegalStateException {
        // Create a replacement config and then
        // apply it to the given base format
        return format.replaceText(
                TextReplacementConfig.builder()
                        .match(REPLACE_PATTERN)

                        // Replacement function which will replace
                        // any given arguments in the text
                        .replacement((match, builder) -> formatArgument(match))
                        .build()
        );
    }

    /**
     * Formats the given argument.
     * <p>
     * If this fails to parse a given argument match result
     * then it simply returns the given input in component
     * form
     * @param result The argument match result
     * @return The formatted argument
     */
    private Component formatArgument(MatchResult result) {
        // Remove all white space and surrounding braces
        var group = result.group()
                .substring(1, result.group().length() - 1)
                .trim();

        // Attempt parsing
        try {
            var reader = new StringReader(group);
            // All arguments must have an index, otherwise we don't
            // know which Object argument to use
            int index = reader.readInt();
            reader.skipWhitespace();

            // Create type variables, set them both
            // to default values
            FormatType type = FormatType.DEFAULT;
            String style = "";

            // If there's anything extra given in the arguments
            // Then attempt to read the argument type from it
            if (reader.canRead() && reader.peek() == ',') {
                reader.skip();
                reader.skipWhitespace();
                type = TYPE_PARSER.parse(reader);
                reader.skipWhitespace();

                // If there's even more to read, read style
                if (reader.canRead() && reader.peek() == ',') {
                    reader.skip();
                    reader.skipWhitespace();

                    style = reader.getRemaining();
                    reader.setCursor(reader.getTotalLength());
                }
            }

            // There's something still lingering in the input,
            // must be invalid
            if (reader.canRead()) {
                throw new IllegalStateException("Invalid argument: " + result.group());
            }

            // resolve the argument into a component
            return type.resolveArgument(args[index], style);
        } catch (CommandSyntaxException e) {
            return Component.text(result.group());
        }
    }

    /* ----------------------------- SUB CLASSES ------------------------------ */

    /**
     * An enum of types that can given to the the {@link ComponentFormat} which will
     * then be automatically formatted for you.
     * <p>
     * You let the formatter know how you want an argument to be formatted like so:
     * "Format me! My name is: {0, type}"
     * 0 is the argument index, and the type is name of one of the {@link FormatType}
     * constants, case doesn't matter. Some format types accept extra arguments, such
     * as: "I have a cool item, look: {0, item, -amount}" This tells the {@link #ITEM}
     * type that the formatted message should also include the item's amount before
     * the item display name.
     * <p>
     * The documentation for each of the enum constants in this class contains further
     * documentation on what the format returns and what kind of <code>style</code>
     * argument it accepts and uses
     *
     * @see #DEFAULT
     * @see #NUMBER
     * @see #DATE
     * @see #RHINES
     * @see #GEMS
     * @see #USER
     * @see #CLASS
     * @see #ITEM
     * @see #TIME
     * @see #KEY
     * @see #LOCATION
     */
    public enum FormatType {
        /**
         * Default format type, just returns {@link Text#valueOf(Object)} for
         * the given <code>arg</code>
         */
        DEFAULT {
            @Override
            public Component resolveArgument(Object arg, String style) {
                return Text.valueOf(arg);
            }
        },

        /**
         * Formats the given <code>arg</code> into a decimal number.
         * <p>
         * Style: <pre>
         * - If empty, uses {@link Text#NUMBER_FORMAT} to
         *   format the number.
         *
         * - If the style is '-roman', the returned component
         *   will be the given number translated into a roman
         *   numeral
         *
         * - Otherwise, the given style will be treated as a
         *   {@link DecimalFormat} format and used to format the
         *   given argument
         * </pre>
         * <p>
         * If the argument is null or not a number, then it will
         * call {@link Text#valueOf(Object)}
         */
        NUMBER {
            @Override
            public Component resolveArgument(Object arg, String style) {
                if (!(arg instanceof Number number)) {
                    return Text.valueOf(arg);
                }

                if (style.contains("-roman")) {
                    return Component.text(RomanNumeral.arabicToRoman(number.longValue()));
                }

                return Component.text(
                        Util.isNullOrBlank(style) ?
                                Text.NUMBER_FORMAT.format(number)
                                : createFormat(style).format(number)
                );
            }

            private DecimalFormat createFormat(String pattern) {
                var format = new DecimalFormat(pattern);
                format.setGroupingUsed(true);
                format.setGroupingSize(3);

                return format;
            }
        },

        /**
         * Formats the given {@link Date}/{@link Number} <code>arg</code> into a date component
         * <p>
         * Style:<pre>
         * - If the style is empty, this will use
         *   {@link Text#DATE_FORMAT} to format the given
         *   time into a date.
         *
         * - Otherwise, the given style is treated as a
         *   {@link java.text.DateFormat} format and that
         *   will be used to format the given date
         * </pre>
         * <p>
         * If the argument is null or not a long timestamp or date,
         * then it will call {@link Text#valueOf(Object)}
         */
        DATE {
            @Override
            public Component resolveArgument(Object arg, String style) {
                if (arg instanceof Number number) {
                    long timeStamp = number.longValue();
                    return format(timeStamp, style);
                }

                if (arg instanceof Date date) {
                    return format(date, style);
                }

                if (arg instanceof ChronoZonedDateTime dateTime) {
                    return format(dateTime.toInstant().toEpochMilli(), style);
                }

                return Text.valueOf(arg);
            }

            private Component format(long l, String style) {
                return format(new Date(l), style);
            }

            private Component format(Date date, String style) {
                return Component.text(
                        Util.isNullOrBlank(style) ?
                                Text.DATE_FORMAT.format(date)
                                : new SimpleDateFormat(style).format(date)
                );
            }
        },

        /**
         * Formats the given <code>arg</code> input into a rhines
         * message.
         * <p>
         * If the argument is null or not a number, then it will
         * call {@link Text#valueOf(Object)}
         */
        RHINES {
            @Override
            public Component resolveArgument(Object arg, String style) {
                if (arg instanceof Number number) {
                    return UnitFormat.rhines(number);
                }

                return Text.valueOf(arg);
            }
        },

        /**
         * Same as {@link #RHINES} except it returns
         * the argument formatted with Gems as the unit instead
         * of Rhines.
         */
        GEMS {
            @Override
            public Component resolveArgument(Object arg, String style) {
                if (arg instanceof Number number) {
                    return UnitFormat.gems(number);
                }

                return Text.valueOf(arg);
            }
        },

        /**
         * Formats a given <code>arg</code> into a user's display name.
         * <p>
         * Style: <pre>
         * - If the style is blank. then this will use
         *   {@link User#displayName()} to format the
         *   display name.
         *
         * - If the style contains the '-realName' argument,
         *   then this will use {@link User#displayName()} to
         *   format the display name.
         * </pre>
         * <p>
         * This argument accepts a {@link User}, {@link UUID},
         * {@link OfflinePlayer}, {@link Player} and a {@link CommandSource}
         * as valid argument types.
         * <p>
         * If the given argument is invalid or null, it calls
         * {@link Text#valueOf(Object)} for the given argument
         */
        USER {
            @Override
            public Component resolveArgument(Object arg, String style) {
                boolean nick = style.isBlank() || !style.contains("-realName");

                // Argument is user, return display name
                if (arg instanceof User user) {
                    return nick ? user.displayName() : user.displayName();
                }

                // We were given UUID, get user by that
                // and return display name
                if (arg instanceof UUID uuid) {
                    var user = Users.get(uuid);
                    return fromUser(user, nick);
                }

                // Argument is player, ensure player has played before
                // And then get user by player's ID and then return
                // display name
                if (arg instanceof OfflinePlayer player && player.hasPlayedBefore()) {
                    var user = Users.get(player);
                    return fromUser(user, nick);
                }

                // If we were given a command source that's a player
                // then get the user's display name from the source's player
                if (arg instanceof CommandSource source && source.isPlayer()) {
                    var user = Users.get(source.asOrNull(Player.class));
                    return fromUser(user, nick);
                }

                return Text.valueOf(arg);
            }

            private Component fromUser(User user, boolean useNick) {
                // Get the nickname
                var displayName = useNick ? user.displayName() : user.displayName();

                // Unload the user if they're offline
                user.unloadIfOffline();
                return displayName;
            }
        },

        /**
         * Formats the given <code>arg</code> as a class to get
         * its name.
         * <p>
         * Style:
         * <pre>
         * - If the style is empty or contains the '-simple'
         *   argument, then the returned text will use the
         *   class' simple name.
         *
         * - If the style contains the '-long- argument,
         *   then the returned text will use the class'
         *   full name
         * </pre>
         * <p>
         * This will test if the given argument is itself
         * a class, or is a regular object, if the <code>arg</code>
         * is a class, it uses that, if it's a regular object,
         * it uses the class of that object.
         * <p>
         * If the arg is null, then {@link Text#valueOf(Object)}
         * is returned instead
         */
        CLASS {
            @Override
            public Component resolveArgument(Object arg, String style) {
                if (arg == null) {
                    return Text.valueOf(null);
                }

                Class c = arg instanceof Class<?> ? (Class) arg : arg.getClass();
                boolean simple = style.isBlank() || style.contains("-simple");

                // Ensure that only a valid input has been
                // given
                Validate.isTrue(
                        simple || style.contains("-long"),
                        "Invalid style: '%s'", style
                );

                return Component.text(
                        simple ? c.getSimpleName() : c.getName()
                );
            }
        },

        /**
         * Formats the given <code>arg</code> argument into
         * an item's display name.
         * <p>
         * Style: <pre>
         * - If the style is blank or contains the arg
         *   '-amount', then the returned display name
         *   will have the item's amount prepended onto
         *   the text
         *
         * - If the style contains the '-!amount' arg
         *   then the amount will not be prepended onto
         *   the display name
         * </pre>
         * <p>
         * If the item is not an {@link ItemStack} or is null,
         * this will call {@link Text#valueOf(Object)}
         */
        ITEM {
            @Override
            public Component resolveArgument(Object arg, String style) {
                // Make sure we're given an item stack
                // If not, just return a default value
                if (!(arg instanceof ItemStack item)) {
                    return Text.valueOf(arg);
                }

                // Format name with or without the item quantity
                // prepended onto it.
                boolean withAmount = style.isBlank() || !style.contains("-!amount");
                return withAmount ? Text.itemAndAmount(item) : Text.itemDisplayName(item);
            }
        },

        /**
         * Formats a <code>arg</code> into a time component using
         * {@link PeriodFormat}. An example of this type's output
         * might be: '4 days, 3 hours and 14 seconds'
         * <p>
         * Style: <pre>
         * - If the style is empty then the formatter will simply
         *   return the given number with {@link PeriodFormat#of(long)}
         *
         * - If the style contains the '-biggest' argument, then
         *   this will call {@link PeriodFormat#retainBiggest()}
         *   before returning the formatted result
         *
         * - If the style contains the '-timestamp' argument,
         *   then the argument will be treated as a time stamp
         *   and the time value will be calculated as the
         *   difference between the given timestamp and the
         *   current time
         * </pre>
         * <p>
         * If the given argument is not a number or is null,
         * {@link Text#valueOf(Object)} is returned instead.
         */
        TIME {
            @Override
            public Component resolveArgument(Object arg, String style) {
                // Not a number, we can't format so return default value
                if (!(arg instanceof Number number)) {
                    return Text.valueOf(arg);
                }

                // Format given time
                long time = number.longValue();
                PeriodFormat format;

                if (style.contains("-timestamp")) {
                    format = PeriodFormat.timeStamp(time);
                } else {
                    format = PeriodFormat.of(time);
                }

                if (style.contains("-biggest")) {
                    format = format.retainBiggest();
                }

                return format.asComponent();
            }
        },

        /**
         * Formats a {@link Key} into a component by either returning
         * the key's value or the key's complete string representation,
         * depending on if the key's namespace is the same as {@link Keys#FTC_KEY_PARSER}'s
         * namespace.
         * <p>
         * If the given <code>arg</code> is either null or not a {@link Key}
         * then {@link Text#valueOf(Object)} is returned.
         */
        KEY {
            @Override
            public Component resolveArgument(Object arg, String style) {
                if (!(arg instanceof Key key)) {
                    return Text.valueOf(arg);
                }

                // Test if namespace is FTC's, if it is,
                // return the key's value only
                if (key.namespace().equals(Keys.argumentType().getDefaultNamespace())) {
                    return Component.text(key.value());
                }

                return Component.text(key.asString());
            }
        },

        /**
         * Formats a <code>arg</code> to be a pretty location message.
         * <p>
         * Style: <pre>
         * - If the style is empty, the returned format will
         *   not show the location's world, and will not have
         *   a '/tp_exact' click event.
         *
         * - The '-w' or '-world' argument tells the formatter
         *   to show the location's world.
         * - The '-c' or '-clickable' tells the formatter to
         *   add a click event to the result.
         * </pre>
         * <p>
         * If the given argument is not a {@link Location} or {@link WorldVec3i}
         * object, or is null, then {@link Text#valueOf(Object)} is returned
         */
        LOCATION {
            @Override
            public Component resolveArgument(Object arg, String style) {
                boolean includeWorld = style.contains("-w");
                boolean clickable = style.contains("-c");

                if (arg instanceof Location l) {
                    return location(includeWorld, clickable, l);
                }

                if (arg instanceof WorldVec3i vec3i) {
                    return location(includeWorld, clickable, vec3i.toLocation());
                }

                return Text.valueOf(arg);
            }

            private Component location(boolean world, boolean clickable, Location l) {
                if (clickable) {
                    return Text.clickableLocation(l, world);
                }

                return Text.prettyLocation(l, world);
            }
        };

        /**
         * Resolves the given argument into a component using
         * this format type's formatting
         * @param arg The argument to format
         * @param style An optional string style provided in the argument
         * @return The argument as a formatted component
         */
        public abstract Component resolveArgument(Object arg, String style);
    }
}