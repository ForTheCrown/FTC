package net.forthecrown.utils.text;

import lombok.Getter;
import net.forthecrown.core.Messages;
import net.forthecrown.core.Permissions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.lang.StringUtils;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.Nullable;

import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.SECTION_CHAR;

/**
 * A chat renderer is an object which takes a given input string
 * and translates, or renders, it into a component form based off
 * of given 'flag' parameters.
 * <p>
 * Flags are simply integer values masked together, these flags
 * determine things about the renderer such as if the renderer
 * should translate color codes or emotes.
 * <p>
 * A renderer can be tuned to the permissions of a specific user
 * with {@link #of(Permissible)}, however, because I'd prefer to
 * keep open the possibility of swapping out this class or completely
 * rewriting it, I heavily recommend you use {@link Text#renderString(Permissible, String)}
 * or {@link Text#renderString(String)} instead of directly referring
 * to this class.
 */
@Getter
public class ChatParser {

    /* --------------------------- FLAG CONSTANTS --------------------------- */

    /**
     * Flag for determining if color codes should be translated.
     * <p>
     * Please note that this flag only applies to ampersand ('&')
     * color codes, section character color codes will always be
     * translated regardless of renderer instance
     */
    public static final int FLAG_COLOR_CODES    = 0x1;

    /**
     * Flag for determining if color codes should be translated
     * @see ChatEmotes
     */
    public static final int FLAG_EMOTES         = 0x2;

    /**
     * Flag for determining how links are translated.
     * <p>
     * If this flag is set, then URLs are translated into
     * their literals with an added {@link net.kyori.adventure.text.event.ClickEvent},
     * if this flag is not set, then links will be turned
     * cyan and given an underline, like hyperlinks on the
     * internet.
     * <p>
     * Note: If the {@link #FLAG_LINKS} flag is not set, this
     * flag has no effect as it only affects the translation
     * process if links are being translated at all.
     */
    public static final int FLAG_CLEAN_LINKS    = 0x4;

    /**
     * Flag for determining if translated text should have link
     * texts translated into clickable text links. If unset, links
     * are just left as plain text
     */
    public static final int FLAG_LINKS          = 0x8;

    /**
     * Flag for determining if the given input should have its
     * case ignored.
     * <p>
     * If this flag is not set, then the given
     * input string will be made lowercase with an uppercase
     * starting letter and a '!' appended to it. If it is set,
     * the input's case will not be changed.
     */
    public static final int FLAG_IGNORE_CASE    = 0x10;

    /**
     * Flag for determining whether gradients tokens should be
     * rendered.
     * @see #GRADIENT_PATTERN
     */
    public static final int FLAG_GRADIENTS      = 0x20;

    /**
     * A combination of flags for simply translating the colors of
     * a given input string, case will be ignored and links will
     * not be given a cyan color and underline
     * @see #FLAG_CLEAN_LINKS
     * @see #FLAG_COLOR_CODES
     * @see #FLAG_IGNORE_CASE
     * @see #FLAG_LINKS
     */
    public static final int COLOR_FLAGS         = FLAG_LINKS | FLAG_COLOR_CODES
                                                             | FLAG_CLEAN_LINKS
                                                             | FLAG_IGNORE_CASE;

    /**
     * A constant value with all flags set
     * @see #COLOR_FLAGS
     * @see #FLAG_EMOTES
     */
    public static final int ALL_FLAGS           = COLOR_FLAGS | FLAG_EMOTES
                                                              | FLAG_GRADIENTS;

    /* ----------------------------- CONSTANTS ------------------------------ */

    /**
     * A regex pattern for all color codes including hex codes
     * <p>
     * This is used by {@link #replaceColorCodes(String)} to replace
     * all ampersand color codes with section codes
     */
    private static final Pattern COLOR_CHAR_PATTERN = Pattern.compile("&((#[0-9a-fA-F]{6})|([0-9a-fA-FK-Ok-orRxX]))");

    /**
     * Pattern used to render gradient tokens
     * <p>
     * A gradient token would look like so: '&lt gradient=green, blue: Insert Text Here >'
     * @see #replaceGradients(Component)
     */
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("<gradient=([a-zA-Z0-9_# ])+,([a-zA-Z0-9_# ])+:(.)+?>");

    /** A renderer which renders input with the {@link #ALL_FLAGS} flags */
    public static final ChatParser TOTAL_RENDERER = new ChatParser(ALL_FLAGS);

    /** A renderer which renders input with the {@link #COLOR_FLAGS} flags */
    public static final ChatParser COLOR_RENDERER = new ChatParser(FLAG_COLOR_CODES);

    /* -------------------------- INSTANCE FIELDS --------------------------- */

    /** This renderer's parameters */
    private final int flags;

    /** This renderer's legacy serializer */
    private final LegacyComponentSerializer serializer;

    /* ---------------------------- CONSTRUCTOR ----------------------------- */

    public ChatParser(int flags) {
        this.flags = flags;

        var builder = LegacyComponentSerializer.builder();

        if (hasFlags(FLAG_LINKS)) {
            if (hasFlags(FLAG_CLEAN_LINKS)) {
                builder.extractUrls();
            } else {
                builder.extractUrls(Messages.CHAT_URL);
            }
        }

        if (hasFlags(FLAG_COLOR_CODES)) {
            builder.character(SECTION_CHAR)
                    .hexColors();
        }

        this.serializer = builder.build();
    }

    /* ------------------------ STATIC CONSTRUCTORS ------------------------- */

    /**
     * Creates a renderer tailored to the given sender.
     * <p>
     * The resulting renderer will be {@link #TOTAL_RENDERER}
     * if the sender is null or if the sender has all the
     * permissions required, those permissions are as follows:
     * <p>
     * {@link Permissions#CHAT_COLORS} for chat colors
     * <p>
     * {@link Permissions#CHAT_EMOTES} for emotes
     * <p>
     * {@link Permissions#CHAT_IGNORE_CASE} to ignore upper case filtering
     * @param sender The sender to create the renderer for
     * @return
     */
    public static ChatParser of(Permissible sender) {
        if (sender == null) {
            return TOTAL_RENDERER;
        }

        return of(
                sender.hasPermission(Permissions.CHAT_COLORS),
                sender.hasPermission(Permissions.CHAT_EMOTES),
                sender.hasPermission(Permissions.CHAT_IGNORE_CASE),
                sender.hasPermission(Permissions.CHAT_GRADIENTS),
                sender.hasPermission(Permissions.CHAT_LINKS),
                sender.hasPermission(Permissions.CHAT_CLEAN_LINKS)
        );
    }

    /**
     * Creates a renderer with the given parameters, note that
     * {@link #FLAG_CLEAN_LINKS} will always be set to true.
     * <p>
     * If all given parameters are true then {@link #TOTAL_RENDERER}
     * is returned, if only <code>colors</code> and <code>ignoreCase</code>
     * is true, then {@link #COLOR_RENDERER} is returned.
     *
     * @param colors Whether to render ampersand color codes
     * @param emotes True, to translate chat emotes, false otherwise
     * @param ignoreCase True, to ignore case filtering, false otherwise
     * @param gradients True, to allow translation of gradient texts, {@link #GRADIENT_PATTERN}
     * @param links True, to translate links, false to leave links as plain text
     * @param cleanLinks {@link #FLAG_CLEAN_LINKS}
     * @return The created renderer
     */
    public static ChatParser of(boolean colors,
                                boolean emotes,
                                boolean ignoreCase,
                                boolean gradients,
                                boolean links,
                                boolean cleanLinks
    ) {
        int flags = (colors ? FLAG_COLOR_CODES : 0)
                | (emotes ? FLAG_EMOTES : 0)
                | (ignoreCase ? FLAG_IGNORE_CASE : 0)
                | (gradients ? FLAG_GRADIENTS : 0)
                | (links ? FLAG_LINKS : 0)
                | (cleanLinks ? FLAG_CLEAN_LINKS : 0);

        if (flags == COLOR_FLAGS) {
            return COLOR_RENDERER;
        }

        if (flags == ALL_FLAGS) {
            return TOTAL_RENDERER;
        }

        return new ChatParser(flags);
    }

    /**
     * Shorthand method for calling {@link #of(Permissible)} and
     * then {@link #render(String)} with the result.
     *
     * @param sender The 'sender' of the input to format
     * @param message The message to format
     * @return The formatted input
     * @see #of(Permissible)
     * @see #render(String)
     */
    public static Component renderString(@Nullable Permissible sender, @Nullable String message) {
        return of(sender).render(message);
    }

    /* ------------------------------ METHODS ------------------------------- */

    public ChatParser addFlags(int flags) {
        return new ChatParser(flags | this.flags);
    }

    /**
     * Renders the given input into a component using this
     * renderer's flags.
     * <p>
     * If this method is given null, then null is returned, if
     * the given input is blank, then blank is returned
     * @param input The input render
     * @return The rendered component.
     */
    public Component render(@Nullable String input) {
        if (input == null) {
            return null;
        }

        if (input.isBlank()) {
            return Component.text(input);
        }

        if (!hasFlags(FLAG_IGNORE_CASE)) {
            input = checkCase(input);
        }

        if (hasFlags(FLAG_COLOR_CODES)) {
            input = replaceColorCodes(input);
        }

        Component result = serializer.deserialize(input);

        if (hasFlags(FLAG_EMOTES)) {
            result = ChatEmotes.format(result);
        }

        if (hasFlags(FLAG_GRADIENTS)) {
            result = replaceGradients(result);
        }

        return result;
    }

    /**
     * Tests if the given flags have all been set.
     * <p>
     * 'all' because you can give this method an input
     * of several flags combined with the OR operator '\'
     * @param flags The flags to test
     * @return True, if all given flags have been set, false otherwise
     */
    public boolean hasFlags(int flags) {
        return (this.flags & flags) == flags;
    }

    private static String checkCase(String s) {
        if (s.length() <= 8) {
            return s;
        }

        int upperCaseCount = 0;
        int half = s.length() / 2;

        for (int i = 0; i < s.length(); i++) {
            if (Character.isUpperCase(s.charAt(i))) {
                upperCaseCount++;
            }

            // More than half the characters are uppercase
            // return filtered input
            if (upperCaseCount > half) {
                return StringUtils.capitalize(s.toLowerCase()) + "!!";
            }
        }

        return s;
    }

    /** Replaces color codes in the given string */
    public static String replaceColorCodes(String s) {
        return COLOR_CHAR_PATTERN
                .matcher(s)
                .replaceAll(ChatParser::replaceCode);
    }

    /** Replaces the first character of the given result's group with a secion character */
    private static String replaceCode(MatchResult result) {
        return SECTION_CHAR + result.group().substring(1);
    }

    /** Formats all gradients in the given text using {@link #GRADIENT_PATTERN} */
    public static Component replaceGradients(Component original) {
        return original.replaceText(
                TextReplacementConfig.builder()
                        .match(GRADIENT_PATTERN)
                        .replacement((result, builder) -> replaceGradient(result))
                        .build()
        );
    }

    /** Replaces a single gradient instance in the match result */
    private static Component replaceGradient(MatchResult result) {
        String group = result.group()
                // Crop '<gradient=' and '>'
                .substring("<gradient=".length(), result.group().length() - 1)
                .trim();

        int separator = group.indexOf(',');
        int paramsEnd = group.indexOf(':');

        // I don't think this could happen because a
        // regex pattern is being used, but still
        if (separator == -1 || paramsEnd == -1) {
            return Component.text(result.group());
        }

        String firstColorName = group.substring(0, separator).trim();
        String secondColorName = group.substring(separator + 1, paramsEnd).trim();

        TextColor startColor = getColor(firstColorName);
        TextColor endColor = getColor(secondColorName);

        // Invalid color names, return input
        if (startColor == null || endColor == null) {
            return Component.text(result.group());
        }

        return Text.gradient(
                group.substring(paramsEnd + 1).trim(),
                startColor, endColor
        );
    }

    private static TextColor getColor(String s) {
        if (s.startsWith("0x")) {
            // Replace color codes so next if statement
            // picks up that this is a hex color code
            s = "#" + s.substring(2);
        }

        if (s.startsWith("#")) {
            return TextColor.fromHexString(s);
        }

        // Not a hex code -> get by color name
        return NamedTextColor.NAMES.value(s);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChatParser parser)) {
            return false;
        }
        return getFlags() == parser.getFlags();
    }

    @Override
    public int hashCode() {
        return getFlags();
    }
}