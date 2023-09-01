package net.forthecrown.text;

import static net.forthecrown.text.Messages.NULL;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;

import com.google.common.base.Strings;
import io.papermc.paper.adventure.PaperAdventure;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import net.forthecrown.Worlds;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.paper.PaperNbt;
import net.forthecrown.text.format.ComponentFormat;
import net.forthecrown.text.format.FormatBuilder;
import net.forthecrown.text.parse.ChatParseFlag;
import net.forthecrown.text.parse.ChatParser;
import net.forthecrown.text.parse.TextContext;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.KeybindComponent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.Translatable;
import net.kyori.adventure.util.HSVLike;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utility functions relating to Components, mostly string converters lol
 */
public final class Text {
  private Text() {}

  /* ----------------------------- CONSTANTS ------------------------------ */

  /**
   * The server's number formatter, uses the US locale :\
   */
  public static final NumberFormat NUMBER_FORMAT
      = NumberFormat.getInstance(Locale.US);

  /**
   * The user-friendly date time format to show to users, an example of this
   * type's output is: "22 Aug 2022 20:33 UTC"
   */
  public static final SimpleDateFormat DATE_FORMAT
      = new SimpleDateFormat("d LLL yyyy HH:mm z");

  public static final DateTimeFormatter DATE_TIME_FORMATTER
      = DateTimeFormatter.ofPattern(DATE_FORMAT.toPattern());

  /**
   * A simple style that has the italic text decoration disabled
   */
  public static final Style NON_ITALIC = Style.style()
      .decoration(TextDecoration.ITALIC, false)
      .build();

  public static final LegacyComponentSerializer LEGACY
      = LegacyComponentSerializer.builder()
      .character('&')
      .extractUrls()
      .hexColors()
      .build();

  public static final LegacyComponentSerializer SECTION_LEGACY
      = LegacyComponentSerializer.builder()
      .extractUrls()
      .hexColors()
      .build();

  public static final ComponentFlattener FLATTENER = ComponentFlattener.basic()
      .toBuilder()
      .complexMapper(TranslatableComponent.class, (component, consumer) -> {
        var translator = GlobalTranslator.translator();
        var format = translator.translate(component.key(), Locale.ENGLISH);

        // This is a weird bug, the keys exist but not on the server's side??
        // IDK, this is to stop an infinite recursion error from happening
        if (format == null) {
          consumer.accept(text(component.key()));

          for (Component child : component.children()) {
            consumer.accept(child);
          }

          return;
        }

        consumer.accept(GlobalTranslator.render(component, Locale.ENGLISH));
      })
      .unknownMapper(component -> {
        throw new IllegalArgumentException(
            String.format("Don't know how to flatten: %s", component)
        );
      })
      .build();

  public static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.builder()
      .flattener(FLATTENER)
      .build();

  /* ----------------------------- UTILITY METHODS ------------------------------ */

  /**
   * Renders the given text to a string with section symbols to denote color
   * codes.
   *
   * @param text The text to render
   * @return The rendered string
   */
  public static String toString(Component text) {
    return SECTION_LEGACY.serialize(text);
  }

  /**
   * Renders the given component to a plain string
   *
   * @param text The text to render
   * @return The plain string version of the given text
   */
  @Contract("null -> null")
  public static String plain(@Nullable ComponentLike text) {
    if (text == null) {
      return null;
    }
    return PLAIN.serialize(text.asComponent());
  }

  /**
   * Translates a given text to a discord-formatted string.
   * <p>
   * Since discord doesn't support colored text, this will be an uncoloured
   * text, all hover/click events will be ignored
   *
   * @param text The text to translate
   * @return The flattened result
   */
  public static String toDiscord(Component text) {
    Objects.requireNonNull(text);
    return new DiscordRenderer().flatten(text);
  }

  /**
   * Converts the given string to a text that can be placed onto items with
   * {@link #renderString(String)} and {@link #wrapForItems(Component)}
   *
   * @param str The string to render
   * @return The rendered string
   */
  public static Component stringToItemText(String str) {
    return wrapForItems(renderString(str));
  }

  /**
   * Wraps the given text so that when it's placed onto either an item lore or
   * item name, it will be a non-italic white text, instead of a purple italic
   * text
   *
   * @param text The text to wrap
   * @return The wrapped text
   */
  public static Component wrapForItems(Component text) {
    var color = text.color();
    var italicState = text.decoration(TextDecoration.ITALIC);

    if (color == null) {
      text = text.color(NamedTextColor.WHITE);
    }

    if (italicState == State.NOT_SET) {
      text = text.decoration(TextDecoration.ITALIC, false);
    }

    return text;
  }

  /**
   * Renders the string, formatting all color codes, links and emotes in the
   * string
   *
   * @param s The string to render
   * @return The rendered message
   */
  public static Component renderString(String s) {
    return ChatParser.parser().parse(s, TextContext.totalRender());
  }

  /**
   * Renders the given string with the permissions of the given user
   *
   * @param permissible The permissible rendering the message
   * @param s           The string to render
   * @return The formatted message
   */
  public static Component renderString(Permissible permissible, String s) {
    return ChatParser.parser().parse(s, TextContext.create(permissible, null));
  }

  /**
   * Renders the given string with the permissions of the given user
   *
   * @param permissible The permissible rendering the message
   * @param s           The string to render
   * @return The formatted message
   */
  public static Component renderString(User permissible, String s) {
    return ChatParser.parser().parse(s, TextContext.create(permissible, null));
  }

  public static ViewerAwareMessage parseToViewerAware(Permissible permissible, String s) {
    Set<ChatParseFlag> flags = ChatParseFlag.allApplicable(permissible);

    return viewer -> {
      TextContext context = TextContext.of(flags, viewer);
      return ChatParser.parser().parse(s, context);
    };
  }

  /**
   * Formats the given tag.
   * <p>
   * Uses the vanilla tag formatter to display the tag and format it
   *
   * @param tag              The tag to format
   * @param allowIndentation Whether to allow indentation in the formatting,
   *                         this makes the resulting tag more readable
   * @return The formatted tag
   */
  public static Component displayTag(BinaryTag tag, boolean allowIndentation) {
    String indent = allowIndentation ? "  " : "";

    var text = PaperNbt.asComponent(tag, indent, true);

    return text.clickEvent(ClickEvent.copyToClipboard(tag.toNbtString()))
        .hoverEvent(text("Click to copy raw NBT!"));
  }

  /**
   * Tests if the given text is a dash-clear text. Aka, if it's a plain text
   * equal to "-clear"
   *
   * @param text The text to test
   * @return True, if it's a clear text, false otherwise
   */
  public static boolean isDashClear(Component text) {
    return plain(text).equals("-clear");
  }

  public static TextComponent gradient(String input, TextColor... colors) {
    return gradient(input, false, colors);
  }

  /**
   * Creates a text with a gradient color
   * <p>
   * If the given input is null, then this method returns null.
   * <p>
   * If the input is less than 2 characters long, then the input is returned
   * with the
   * <code>start</code> color.
   *
   * @param input  The input
   * @param colors The colors to create the gradient with
   * @return The text colored as a gradient
   */
  public static TextComponent gradient(String input, boolean hsv, TextColor... colors) {
    if (input == null) {
      return null;
    }

    if (colors.length < 1) {
      return text(input);
    }

    if (colors.length == 1) {
      return text(input, colors[0]);
    }

    int length = input.length();
    char[] chars = input.toCharArray();

    // Not enough space for gradient
    if (length < 2) {
      return Component.text(input, colors[0]);
    }

    var builder = Component.text();

    for (int i = 0; i < length; i++) {
      char c = chars[i];
      float progress = (float) i / length;
      TextColor color = _lerp(hsv, progress, colors);

      builder.append(text(c, color));
    }

    return builder.build();
  }

  public static TextColor rgbLerp(float progress, TextColor... colors) {
    return lerp(false, progress, colors);
  }

  public static TextColor hsvLerp(float progress, TextColor... colors) {
    return lerp(true, progress, colors);
  }

  /**
   * Linearly interpolates between an array of colors.
   * <p>
   * If the color array is empty, null is returned, if the length is 1, the only
   * color is returned, otherwise, interpolation goes on as normal
   *
   * @param progress The progress to interpolate, from 0 to 1
   * @param colors   The colors to interpolate
   * @return The interpolated color
   */
  public static TextColor lerp(boolean hsv, float progress, TextColor... colors) {
    if (colors.length == 0) {
      return null;
    } else if (colors.length == 1) {
      return colors[0];
    }

    if (progress <= 0) {
      return colors[0];
    }

    if (progress >= 1) {
      return colors[colors.length - 1];
    }

    return _lerp(hsv, progress, colors);
  }

  private static TextColor _lerp(boolean hsv, float progress, TextColor... colors) {
    if (colors.length == 2) {
      return lerp2(hsv, progress, colors[0], colors[1]);
    }

    final int maxIndex = colors.length - 1;

    // I couldn't figure this part out myself, so I copied this gist:
    // https://gist.github.com/Tetr4/3c7a4eddb78ae537c995
    //  - Jules

    int firstIndex = (int) (progress * maxIndex);
    float firstStep = (float) firstIndex / maxIndex;
    float localStep = (progress - firstStep) * maxIndex;

    TextColor c1 = colors[firstIndex];
    TextColor c2 = colors[firstIndex + 1];

    return lerp2(hsv, localStep, c1, c2);
  }

  /** Interpolates 2 colors */
  private static TextColor lerp2(boolean hsv, float p, TextColor c1, TextColor c2) {
    if (!hsv) {
      return TextColor.lerp(p, c1, c2);
    }

    HSVLike hsv1 = c1.asHSV();
    HSVLike hsv2 = c2.asHSV();

    HSVLike result = HSVLike.hsvLike(
        // min + (step * (max - min))
        hsv1.h() + p * (hsv2.h() - hsv1.h()),
        hsv1.s() + p * (hsv2.s() - hsv1.s()),
        hsv1.v() + p * (hsv2.v() - hsv1.v())
    );

    return TextColor.color(result);
  }

  /**
   * Gets an items display name.
   * <p>
   * If the item has a custom name, returns that, otherwise it'll return a
   * translatable component for the item's type.
   * <p>
   * It should be noted that this function works differently to
   * {@link ItemStack#displayName()} as this is made to function more as a
   * chat-friendly version of that function, meaning it doesn't automatically
   * apply any colors to the item's display name or force italics, if they've
   * been disabled.
   *
   * @param item The item to get the display name for
   * @return The item's display name
   */
  public static Component itemDisplayName(ItemStack item) {
    net.minecraft.world.item.ItemStack nms = CraftItemStack.asNMSCopy(item);
    Component hoverName = PaperAdventure.asAdventure(nms.getHoverName());

    if (nms.hasCustomHoverName() && hoverName.decoration(TextDecoration.ITALIC) == State.NOT_SET) {
      hoverName = hoverName.decorate(TextDecoration.ITALIC);
    }

    return hoverName.hoverEvent(item);
  }

  /**
   * Takes an enum input like OAK_SIGN and returns "Oak Sign"
   *
   * @param anum The enum to normalize
   * @return The normalized input.
   */
  public static String prettyEnumName(@NotNull Enum<?> anum) {
    return capitalizeFully(anum.name().replaceAll("_", " "));
  }

  /**
   * Creates a pretty location message for easy readability.
   * <p>
   * Note: Does not use exact decimal cords, rather uses block cords
   *
   * @param l            The location to format for
   * @param includeWorld Whether to include the world's name in the message
   * @return The formatted easily readable location message
   */
  public static TextComponent prettyLocation(Location l, boolean includeWorld) {
    return text(
        String.format("%sx %sy %sz%s",
            l.getBlockX(),
            l.getBlockY(),
            l.getBlockZ(),
            includeWorld ? " world: " + formatWorldName(l.getWorld()) : ""
        )
    );
  }

  /**
   * Creates a location message that when clicked teleports you to the
   * location.
   *
   * @param l            The location to format for
   * @param includeWorld Whether to include the world in the message
   * @return The formatted and clickable message
   * @see #prettyLocation(Location, boolean)
   */
  public static TextComponent clickableLocation(
      Location l,
      boolean includeWorld
  ) {
    return prettyLocation(l, includeWorld)
        .hoverEvent(text("Click to teleport!"))

        .clickEvent(ClickEvent.runCommand(
            "/tp_exact x=%s y=%s z=%s yaw=%s pitch=%s world=%s".formatted(
                l.getX(), l.getY(), l.getZ(),
                l.getYaw(),
                l.getPitch(),
                l.getWorld().getName()
            )
        ));
  }

  /**
   * Formats an item's name and amount into a message, eg: "12 Oak Sign".
   *
   * @param itemStack The item stack to format for
   * @param amount    The amount to show
   * @return The formatted message
   */
  public static TextComponent itemAndAmount(ItemStack itemStack, int amount) {
    return text()
        .hoverEvent(itemStack)
        .append(text(amount))
        .append(space())
        .append(itemDisplayName(itemStack))
        .build();
  }

  /**
   * Same as {@link #itemAndAmount(ItemStack, int)} except uses the item's
   * amount
   *
   * @param item The item to format for
   * @return The formatted message with the item's amount.
   */
  public static TextComponent itemAndAmount(ItemStack item) {
    return itemAndAmount(item, item.getAmount());
  }

  /**
   * Creates a non italic style with the given text color
   *
   * @param color The color to create with
   * @return The created style
   */
  public static Style nonItalic(TextColor color) {
    return Style.style(color).decoration(TextDecoration.ITALIC, false);
  }

  /**
   * Formats the given time stamp into a date
   * <p>
   * Delegate method for {@link #formatDate(Date)}
   *
   * @param time The time stamp to format
   * @return The formatted date
   * @see #formatDate(Date)
   */
  public static Component formatDate(long time) {
    return formatDate(new Date(time));
  }

  /**
   * Formats a number, by adding decimals, commas and so on, making the given
   * number more human-readable
   *
   * @param number The number to format
   * @return The formatted number
   */
  public static Component formatNumber(Number number) {
    return text(NUMBER_FORMAT.format(number));
  }

  /**
   * Formats the given date with the {@link #DATE_FORMAT} format
   * <p>
   * This is functionally identical to
   * <code>Component.text(DATE_FORMAT.format(date))</code>
   *
   * @param date The date to format
   * @return The formatted date
   */
  public static Component formatDate(Date date) {
    return text(DATE_FORMAT.format(date));
  }

  public static Component formatDate(Instant instant) {
    return text(DATE_FORMAT.format(Date.from(instant)));
  }

  /**
   * Formats the given world name.
   * <p>
   * If the given world is the over world, it returns "Overworld", or if the
   * input is the resource world, then "Resource World" is returned, otherwise
   * it replaces all underscores in the world's name and replaces them with
   * spaces, it then capitalizes the first letter of each word in the resulting
   * string.
   *
   * @param world The world's name to format
   * @return The formatted world name
   */
  public static String formatWorldName(World world) {
    if (world.equals(Worlds.overworld())) {
      return "Overworld";
    }

    return capitalizeFully(world.getName()
        .replaceAll("world_", "")
        .replaceAll("_world", "")
        .replaceAll("_", " ")
    );
  }

  public static String capitalizeFully(String str) {
    String[] words = str.toLowerCase().split("\\s");
    StringBuilder builder = new StringBuilder();

    for (int i = 0; i < words.length; i++) {
      String word = words[i];

      if (Strings.isNullOrEmpty(word)) {
        builder.append(" ");
        continue;
      }

      char first = word.charAt(0);
      char upper = Character.toUpperCase(first);

      if (i != 0) {
        builder.append(" ");
      }

      builder.append(upper);
      builder.append(word.substring(1));
    }

    return builder.toString();
  }

  public static String conditionalPlural(double amount) {
    if (amount == 1) {
      return "";
    }

    return "s";
  }

  public static boolean isEmpty(ComponentLike value) {
    return value == null
        || Objects.equals(value.asComponent(), Component.empty())
        || plain(value.asComponent()).isEmpty();
  }

  public static Component sourceDisplayName(CommandSource source) {
    return sourceDisplayName(source, null);
  }

  public static Component sourceDisplayName(CommandSource source, Audience viewer) {
    if (source.isPlayer()) {
      return Users.get(source.asPlayerOrNull()).displayName(viewer);
    }
    return source.displayName();
  }

  public static TextComponent chatWidthBorder(Component title) {
    // Font pixels aren't real pixels, (in actuality, max chat width is 640px)
    final int maxChatWidth = 320;

    if (isEmpty(title)) {
      return border(maxChatWidth, 0);
    }

    Component withSpaces = text().append(space(), title, space()).build();
    int titleLength = TextInfo.length(withSpaces);

    return border(maxChatWidth, titleLength);
  }

  public static TextComponent border(int maxWidthPx, int titleLengthPx) {
    final char borderChar = ' ';
    final int borderWidth = 4; // ' ' is 3 px, but +1 for space between chars

    if (titleLengthPx <= 0) {
      final int size = maxWidthPx / borderWidth;
      return text(String.valueOf(borderChar).repeat(size))
          .decorate(TextDecoration.STRIKETHROUGH);
    }

    int borderSize = (maxWidthPx - titleLengthPx) / 2;
    int borderChars = borderSize / borderWidth;

    return text(String.valueOf(borderChar).repeat(borderChars))
        .decorate(TextDecoration.STRIKETHROUGH);
  }

  /* ----------------------------- FORMATTERS ------------------------------ */

  public static ViewerAwareMessage vformat(String format, Object... args) {
    return FormatBuilder.builder().setFormat(format).setArguments(args).asViewerAware();
  }

  public static ViewerAwareMessage vformat(String format, TextColor color, Object... args) {
    return FormatBuilder.builder().setFormat(format, color).setArguments(args).asViewerAware();
  }

  public static ViewerAwareMessage vformat(String format, Style style, Object... args) {
    return FormatBuilder.builder().setFormat(format, style).setArguments(args).asViewerAware();
  }

  /**
   * Formats a given component in the same way as
   * {@link java.text.MessageFormat}. If the given string contains any
   * {@link ChatEmotes} or color codes, they will be formatted before the
   * arguments of the format are formatted
   * <p>
   * Delegate method for {@link #format(String, Style, Object...)} with an empty
   * {@link Style}
   *
   * @param format The message format to use
   * @param args   Any optional arguments to use in formatting
   * @return The formatted component
   * @see #format(String, Style, Object...)
   */
  public static Component format(String format, Object... args) {
    return FormatBuilder.builder().setFormat(format).setArguments(args).asComponent();
  }

  /**
   * Formats the given component in the same way as
   * {@link java.text.MessageFormat}. If the given string contains any
   * {@link ChatEmotes} or color codes, they will be formatted before the
   * arguments of the format are formatted
   * <p>
   * Delegate method for {@link #format(String, Style, Object...)} wit the style
   * set to the given color
   *
   * @param format The message format to use
   * @param color  The color to apply to the base component
   * @param args   Any optional arguments to use in formatting
   * @return The formatted component
   * @see #format(String, Style, Object...)
   */
  public static Component format(String format, TextColor color, Object... args) {
    return FormatBuilder.builder().setFormat(format, color).setArguments(args).asComponent();
  }

  /**
   * Formats the given component in the same style as
   * {@link java.text.MessageFormat} and Adventure APIs
   * {@link TranslatableComponent}s. If the given string contains any
   * {@link ChatEmotes} or color codes, they will be formatted before the
   * arguments of the format are formatted
   * <p>
   * Delegate method for {@link #format(Component, Object...)}
   *
   * @param format The message format, eg: '{0} had a good day today!'
   * @param style  The style to apply to the base component
   * @param args   Any optional arguments to use in formatting
   * @return The formatted component
   * @see #format(Component, Object...)
   */
  public static Component format(String format, Style style, Object... args) {
    return FormatBuilder.builder().setFormat(format, style).setArguments(args).asComponent();
  }

  /**
   * Formats the given component in the same style as
   * {@link java.text.MessageFormat} and Adventure APIs
   * {@link TranslatableComponent}s.
   * <p>
   * Uses {@link ComponentFormat} to format the given input.
   *
   * @param format The message to format, eg: '{0} had a good day today! Did you
   *               have {0} day today too?'
   * @param args   Any optional arguments to use in formatting
   * @return The formatted component, this will return
   * <code>format</code> if args are null or empty
   * @see ComponentFormat
   */
  public static Component format(Component format, Object... args) {
    return FormatBuilder.builder().setFormat(format).setArguments(args).asComponent();
  }

  /* ----------------------------------------------------------- */

  /**
   * Gets the component value of the arg object
   * <p>
   * If the given value is a {@link ComponentLike} or {@link Component} then the
   * argument itself is returned.
   * <p>
   * Then this method tests if the given argument is either a
   * {@link Translatable} object or a
   * {@link KeybindComponent.KeybindLike} object, if it
   * is, it returns a component respective to its type.
   * <p>
   * If the argument is null, then a "null" text component is returned.
   * <p>
   * Otherwise {@link Text#renderString(String)} is used to render the object's
   * {@link String#valueOf(Object)} result to a component, meaning this method,
   * if given a string with color codes or emotes, will translate them
   *
   * @param arg The arg object
   * @return The component value of the given argument
   */
  public static @NotNull Component valueOf(@Nullable Object arg) {
    return valueOf(arg, null);
  }

  /**
   * Gets the component value of the arg object
   * <p>
   * If the given value is a {@link ComponentLike} or {@link Component} then the
   * argument itself is returned.
   * <p>
   * Then this method tests if the given argument is either a
   * {@link Translatable} object or a
   * {@link KeybindComponent.KeybindLike} object, if it
   * is, it returns a component respective to its type.
   * <p>
   * If the argument is null, then a "null" text component is returned.
   * <p>
   * Otherwise {@link Text#renderString(String)} is used to render the object's
   * {@link String#valueOf(Object)} result to a component, meaning this method,
   * if given a string with color codes or emotes, will translate them
   *
   * @param arg The arg object
   * @param viewer Viewer of the message
   *
   * @return The component value of the given argument
   */
  public static @NotNull Component valueOf(@Nullable Object arg, Audience viewer) {
    if (arg == null) {
      return NULL;
    }

    if (arg instanceof ViewerAwareMessage awareMessage) {
      return awareMessage.create(viewer);
    }

    if (arg instanceof ComponentLike like) {
      return like.asComponent();
    }

    // Can be translated, thus it should be
    if (arg instanceof Translatable translatable) {
      return Component.translatable(translatable);
    }

    // Key bind, I don't know when on god's green
    // earth this would ever be used, but it might,
    // so I put it here lol
    if (arg instanceof KeybindComponent.KeybindLike like) {
      return Component.keybind(like);
    }

    // Some kind of object, we don't know what it is,
    // maybe a string or who knows, so just call
    // String#valueOf(Object) on it and translate any
    // resulting color codes, links and emotes into text
    ChatParser parser = ChatParser.parser();
    return parser.parse(String.valueOf(arg), TextContext.totalRender(viewer));
  }

  /* ------------------------- REGEX OPERATIONS -------------------------- */

  /**
   * Tests if the given component contains the given pattern value.
   *
   * @param component The text to test
   * @param pattern   The pattern to test
   * @return True, if the component contains the given text
   * @throws NullPointerException If either the component or pattern were null
   */
  public static boolean contains(@NotNull Component component,
                                 @NotNull Pattern pattern
  ) throws NullPointerException {
    Objects.requireNonNull(component, "Component was null");
    Objects.requireNonNull(pattern, "Pattern was null");

    var plain = plain(component);

    return pattern.matcher(plain)
        .find();
  }

  /**
   * Tests if the given component completely matches the given pattern
   *
   * @param component The text to test
   * @param pattern   The pattern to test
   * @return True, if the entire text matches the pattern, false otherwise
   * @throws NullPointerException If either the component or pattern were null
   */
  public static boolean matches(@NotNull Component component,
                                @NotNull Pattern pattern
  ) throws NullPointerException {
    Objects.requireNonNull(component, "Component was null");
    Objects.requireNonNull(pattern, "Pattern was null");

    var plain = plain(component);

    return pattern.matcher(plain)
        .matches();
  }

  /**
   * Recursively splits the given text using the given pattern.
   * <p>
   * As well as splitting, this will, in effect, also flatten the
   * component-children tree. As it flattens, it will attempt to retain the
   * visual look of the components by merging their styles.
   *
   * @param pattern   The Pattern the component will be split with
   * @param component The text to split
   * @return The split result, will have 1 entry if the text was not split at
   * all
   * @throws NullPointerException If either the regex or component parameters
   *                              were null
   */
  public static @NotNull List<Component> split(@NotNull @RegExp String pattern,
                                               @NotNull Component component
  ) throws NullPointerException {
    Objects.requireNonNull(pattern);

    return split(Pattern.compile(pattern), component);
  }

  /**
   * Recursively splits the given text using the given pattern.
   * <p>
   * As well as splitting, this will, in effect, also flatten the
   * component-children tree. As it flattens, it will attempt to retain the
   * visual look of the components by merging their styles.
   *
   * @param pattern   The Pattern the component will be split with
   * @param component The text to split
   * @return The split result, will have 1 entry if the text was not split at
   * all
   * @throws NullPointerException If either the regex or component parameters
   *                              were null
   */
  public static @NotNull List<Component> split(@NotNull Pattern pattern,
                                               @NotNull Component component
  ) throws NullPointerException {
    Objects.requireNonNull(pattern, "Pattern was null");
    Objects.requireNonNull(component, "Component was null");

    if (!contains(component, pattern)) {
      return ObjectLists.singleton(component);
    }

    return new TextSplitter(pattern).split(component);
  }
}