package net.forthecrown.utils.text;

import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.SECTION_CHAR;

import java.util.function.Predicate;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import lombok.Getter;
import net.forthecrown.core.Permissions;
import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.Nullable;

/**
 * A chat renderer is an object which takes a given input string and translates,
 * or renders, it into a component form based off of given 'flag' parameters.
 * <p>
 * Flags are simply integer values masked together, these flags determine things
 * about the renderer such as if the renderer should translate color codes or
 * emotes.
 * <p>
 * A renderer can be tuned to the permissions of a specific user with
 * {@link #of(Permissible)}, however, because I'd prefer to keep open the
 * possibility of swapping out this class or completely rewriting it, I heavily
 * recommend you use {@link Text#renderString(Permissible, String)} or
 * {@link Text#renderString(String)} instead of directly referring to this
 * class.
 */
@Getter
public class ChatParser {

  /* --------------------------- FLAG CONSTANTS --------------------------- */

  /**
   * Flag for determining if color codes should be translated.
   * <p>
   * Please note that this flag only applies to ampersand ('&') color codes,
   * section character color codes will always be translated regardless of
   * renderer instance
   */
  public static final int FLAG_COLOR_CODES = 0x1;

  /**
   * Flag for determining if chat emotes should be translated
   *
   * @see ChatEmotes
   */
  public static final int FLAG_EMOTES = 0x2;

  /**
   * Flag for determining how links are translated.
   * <p>
   * If this flag is set, then URLs are translated into their literals with an
   * added {@link net.kyori.adventure.text.event.ClickEvent}, if this flag is
   * not set, then links will be turned cyan and given an underline, like
   * hyperlinks on the internet.
   * <p>
   * Note: If the {@link #FLAG_LINKS} flag is not set, this flag has no effect
   * as it only affects the translation process if links are being translated at
   * all.
   */
  public static final int FLAG_CLEAN_LINKS = 0x4;

  /**
   * Flag for determining if translated text should have link texts translated
   * into clickable text links. If unset, links are just left as plain text.
   * <p>
   * Note: this flag also is required for the hyperlink function, that is the
   * '[name](url)' function.
   */
  public static final int FLAG_LINKS = 0x8;

  /**
   * Flag for determining if the given input should have its case ignored.
   * <p>
   * If this flag is not set, then the given input string will be tested if it's
   * more than 50% upper case, if it is, it's made lowercase with an uppercase
   * starting letter and a '!' appended to it.
   * <p>
   * If it is set, the input's case will not be changed.
   */
  public static final int FLAG_IGNORE_CASE = 0x10;

  /**
   * Flag for determining whether gradients tokens should be rendered.
   *
   * @see net.forthecrown.utils.text.function.GradientFunction
   */
  public static final int FLAG_GRADIENTS = 0x20;

  /**
   * Flag which allows the use of the '@Player' text functions
   *
   * @see net.forthecrown.utils.text.function.PlayerFunction
   */
  public static final int FLAG_PLAYER_TAGGING = 0x40;

  /**
   * Flag which allows the use of '&lt t:5466456>' functions
   *
   * @see net.forthecrown.utils.text.function.TimeFunction
   */
  public static final int FLAG_TIMESTAMPS = 0x80;

  /**
   * A combination of flags for simply translating the colors of a given input
   * string, case will be ignored and links will not be given a cyan color and
   * underline
   *
   * @see #FLAG_CLEAN_LINKS
   * @see #FLAG_COLOR_CODES
   * @see #FLAG_IGNORE_CASE
   * @see #FLAG_LINKS
   */
  public static final int COLOR_FLAGS
      = FLAG_LINKS
      | FLAG_COLOR_CODES
      | FLAG_CLEAN_LINKS
      | FLAG_IGNORE_CASE;

  /**
   * A constant value with all flags set
   *
   * @see #COLOR_FLAGS
   * @see #FLAG_EMOTES
   */
  public static final int ALL_FLAGS
      = COLOR_FLAGS
      | FLAG_EMOTES
      | FLAG_GRADIENTS
      | FLAG_PLAYER_TAGGING
      | FLAG_TIMESTAMPS;

  /* ----------------------------- CONSTANTS ------------------------------ */

  /**
   * A regex pattern for all color codes including hex codes
   * <p>
   * This is used by {@link #replaceColorCodes(String)} to replace all ampersand
   * color codes with section codes
   */
  private static final Pattern COLOR_CHAR_PATTERN
      = Pattern.compile("(\\\\|)&((#[0-9a-fA-F]{6})|([0-9a-fA-FK-Ok-orRxX]))");

  /**
   * A renderer which renders input with the {@link #ALL_FLAGS} flags
   */
  public static final ChatParser TOTAL_RENDERER = new ChatParser(ALL_FLAGS);

  /**
   * A renderer which renders input with the {@link #COLOR_FLAGS} flags
   */
  public static final ChatParser COLOR_RENDERER
      = new ChatParser(FLAG_COLOR_CODES);

  /* -------------------------- INSTANCE FIELDS --------------------------- */

  /**
   * This renderer's parameters
   */
  private final int flags;

  /**
   * This renderer's legacy serializer
   */
  private final LegacyComponentSerializer serializer;

  /* ---------------------------- CONSTRUCTOR ----------------------------- */

  public ChatParser(int flags) {
    this.flags = flags;

    var builder = LegacyComponentSerializer.builder();

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
   * The resulting renderer will be {@link #TOTAL_RENDERER} if the sender is
   * null or if the sender has all the permissions required, those permissions
   * are as follows:
   * <p>
   * {@link Permissions#CHAT_COLORS} for chat colors
   * <p>
   * {@link Permissions#CHAT_EMOTES} for emotes
   * <p>
   * {@link Permissions#CHAT_IGNORE_CASE} to ignore upper case filtering
   * <p>
   * {@link Permissions#CHAT_LINKS} to enable translating links into click
   * events
   * <p>
   * {@link Permissions#CHAT_CLEAN_LINKS} to allow links to blend in with text,
   * otherwise all links are translated into cyan-colored underlined texts.
   * <p>
   * {@link Permissions#CHAT_GRADIENTS} to translate gradients
   *
   * @param sender The sender to create the renderer for
   * @return The created parser
   */
  public static ChatParser of(Permissible sender) {
    if (sender == null) {
      return TOTAL_RENDERER;
    }

    return of(sender::hasPermission);
  }

  /**
   * Creates a renderer tailored to the given sender.
   * <p>
   * The resulting renderer will be {@link #TOTAL_RENDERER} if the sender is
   * null or if the sender has all the permissions required, those permissions
   * are as follows:
   * <p>
   * {@link Permissions#CHAT_COLORS} for chat colors
   * <p>
   * {@link Permissions#CHAT_EMOTES} for emotes
   * <p>
   * {@link Permissions#CHAT_IGNORE_CASE} to ignore upper case filtering
   * <p>
   * {@link Permissions#CHAT_LINKS} to enable translating links into click
   * events
   * <p>
   * {@link Permissions#CHAT_CLEAN_LINKS} to allow links to blend in with text,
   * otherwise all links are translated into cyan-colored underlined texts.
   * <p>
   * {@link Permissions#CHAT_GRADIENTS} to translate gradients
   *
   * @param sender The sender to create the renderer for
   * @return The created parser
   */
  public static ChatParser of(User sender) {
    if (sender == null) {
      return TOTAL_RENDERER;
    }

    return of(sender::hasPermission);
  }

  private static ChatParser of(Predicate<Permission> predicate) {
    return of(
        predicate.test(Permissions.CHAT_COLORS),
        predicate.test(Permissions.CHAT_EMOTES),
        predicate.test(Permissions.CHAT_IGNORE_CASE),
        predicate.test(Permissions.CHAT_GRADIENTS),
        predicate.test(Permissions.CHAT_LINKS),
        predicate.test(Permissions.CHAT_CLEAN_LINKS),
        predicate.test(Permissions.CHAT_PLAYER_TAGGING),
        predicate.test(Permissions.CHAT_TIMESTAMPS)
    );
  }

  /**
   * Creates a renderer with the given parameters, note that
   * {@link #FLAG_CLEAN_LINKS} will always be set to true.
   * <p>
   * If all given parameters are true then {@link #TOTAL_RENDERER} is returned,
   * if only
   * <code>colors</code> and <code>ignoreCase</code> is true, then
   * {@link #COLOR_RENDERER} is
   * returned.
   *
   * @param colors     Whether to render ampersand color codes
   * @param emotes     True, to translate chat emotes, false otherwise
   * @param ignoreCase True, to ignore case filtering, false otherwise
   * @param gradients  True, to allow translation of gradient texts
   * @param links      True, to translate links, false to leave links as plain
   *                   text
   * @param cleanLinks {@link #FLAG_CLEAN_LINKS}
   * @param tagging    {@link #FLAG_PLAYER_TAGGING}
   * @param timestamps {@link #FLAG_TIMESTAMPS}
   * @return The created renderer
   */
  public static ChatParser of(boolean colors,
                              boolean emotes,
                              boolean ignoreCase,
                              boolean gradients,
                              boolean links,
                              boolean cleanLinks,
                              boolean tagging,
                              boolean timestamps
  ) {
    int flags
        = (colors ? FLAG_COLOR_CODES : 0)
        | (emotes ? FLAG_EMOTES : 0)
        | (ignoreCase ? FLAG_IGNORE_CASE : 0)
        | (gradients ? FLAG_GRADIENTS : 0)
        | (links ? FLAG_LINKS : 0)
        | (cleanLinks ? FLAG_CLEAN_LINKS : 0)
        | (tagging ? FLAG_PLAYER_TAGGING : 0)
        | (timestamps ? FLAG_TIMESTAMPS : 0);

    if (flags == COLOR_FLAGS) {
      return COLOR_RENDERER;
    }

    if (flags == ALL_FLAGS) {
      return TOTAL_RENDERER;
    }

    return new ChatParser(flags);
  }

  /**
   * Shorthand method for calling {@link #of(Permissible)} and then
   * {@link #render(String)} with the result.
   *
   * @param sender  The 'sender' of the input to format
   * @param message The message to format
   * @return The formatted input
   * @see #of(Permissible)
   * @see #render(String)
   */
  public static Component renderString(@Nullable Permissible sender,
                                       @Nullable String message
  ) {
    return of(sender).render(message);
  }

  /* ------------------------------ METHODS ------------------------------- */

  public ChatParser addFlags(int flags) {
    return new ChatParser(flags | this.flags);
  }

  /**
   * Renders the given input into a component using this renderer's flags.
   * <p>
   * If this method is given null, then null is returned, if the given input is
   * blank, then blank is returned
   *
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

    return TextFunctions.render(result, flags);
  }

  /**
   * Tests if the given flags have all been set.
   * <p>
   * 'all' because you can give this method an input of several flags combined
   * with the OR operator '\'
   *
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
        ++upperCaseCount;
      }

      // More than half the characters are uppercase
      // return filtered input
      if (upperCaseCount > half) {
        return StringUtils.capitalize(s.toLowerCase()) + "!!";
      }
    }

    return s;
  }

  /**
   * Replaces color codes in the given string
   */
  public static String replaceColorCodes(String s) {
    return COLOR_CHAR_PATTERN
        .matcher(s)
        .replaceAll(ChatParser::replaceCode);
  }

  /**
   * Replaces the first character of the given result's group with a section
   * character
   */
  private static String replaceCode(MatchResult result) {
    var group = result.group();

    if (group.startsWith("\\")) {
      return group;
    }

    return SECTION_CHAR + group.substring(1);
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