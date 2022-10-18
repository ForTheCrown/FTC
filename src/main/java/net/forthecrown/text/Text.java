package net.forthecrown.text;

import io.papermc.paper.adventure.PaperAdventure;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.commands.admin.CommandTeleportExact;
import net.forthecrown.core.Worlds;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.AbstractCommand;
import net.forthecrown.grenadier.types.args.ArgsArgument;
import net.forthecrown.grenadier.types.args.Argument;
import net.forthecrown.text.format.ComponentFormat;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.kyori.adventure.text.*;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.translation.Translatable;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TextComponentTagVisitor;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;

/**
 * Utility functions relating to Components, mostly string converters lol
 */
public final class Text {
    private Text() {}

    /* ----------------------------- CONSTANTS ------------------------------ */

    /** The server's number formatter, uses the US locale :\ */
    public static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.US);
    /**
     * The user-friendly date time format to show to users, an example
     * of this type's output is: "22 Aug 2022 20:33 UTC"
     */
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("d LLL yyyy HH:mm z");
    /** A simple style that has the italic text decoration disabled */
    @NotNull public static final
    Style NON_ITALIC = Style.style()
            .decoration(TextDecoration.ITALIC, false)
            .build();

    public static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .character('&')
            .extractUrls()
            .hexColors()
            .build();

    public static final LegacyComponentSerializer SECTION_LEGACY = LegacyComponentSerializer.builder()
            .extractUrls()
            .hexColors()
            .build();

    /* ----------------------------- UTILITY METHODS ------------------------------ */

    /**
     * Renders the given text to a string with section
     * symbols to denote color codes.
     * @param text The text to render
     * @return The rendered string
     */
    public static String toString(Component text) {
        return SECTION_LEGACY.serialize(text);
    }

    /**
     * Renders the given component to a plain string
     * @param text The text to render
     * @return The plain string version of the given text
     */
    public static String plain(Component text) {
        return PlainTextComponentSerializer.plainText().serialize(text);
    }

    /**
     * Converts the given string to a text that
     * can be placed onto items with {@link #renderString(String)}
     * and {@link #wrapForItems(Component)}
     * @param str The string to render
     * @return The rendered string
     */
    public static Component stringToItemText(String str) {
        return wrapForItems(renderString(str));
    }

    /**
     * Wraps the given text so that when it's placed onto
     * either an item lore or item name, it will be a
     * non-italic white text, instead of a purple italic
     * text
     * @param text The text to wrap
     * @return The wrapped text
     */
    public static Component wrapForItems(Component text) {
        return Component.text()
                .decoration(TextDecoration.ITALIC, false)
                .color(NamedTextColor.WHITE)
                .append(text)
                .build();
    }

    /**
     * Renders any emotes, color codes and links in
     * the given text by converting the given text
     * to a string and then calling {@link #renderString(String)}
     * on it.
     * @param c The text to render
     * @return The formatted message
     */
    public static Component render(Component c) {
        return renderString(toString(c));
    }

    /**
     * Renders the string, formatting all color codes,
     * links and emotes in the string
     * @param s The string to render
     * @return The rendered message
     */
    public static Component renderString(String s) {
        return ChatParser.TOTAL_RENDERER.render(s);
    }

    /**
     * Renders the given string with the
     * permissions of the given user
     * @param permissible The permissible rendering the messagee
     * @param s The string to render
     * @return The formatted message
     */
    public static Component renderString(Permissible permissible, String s) {
        return ChatParser.renderString(permissible, s);
    }

    /**
     * Formats the given tag.
     * <p>
     * Uses the vanilla tag formatter to display
     * the tag and format it
     * @param tag The tag to format
     * @param allowIndentation Whether to allow indentation in the formatting,
     *                    this makes the resulting tag more readable
     * @return The formatted tag
     */
    public static Component displayTag(Tag tag, boolean allowIndentation) {
        String indent = allowIndentation ? "  " : "";

        var visitor = new TextComponentTagVisitor(indent, 0);
        var text = visitor.visit(tag);

        return PaperAdventure.asAdventure(text);
    }

    /**
     * Tests if the given text is a dash-clear text. Aka,
     * if it's a plain text equal to "-clear"
     * @param text The text to test
     * @return True, if it's a clear text, false otherwise
     */
    public static boolean isDashClear(Component text) {
        return Messages.DASH_CLEAR.equals(text);
    }

    /**
     * Creates a text with a gradient color
     * <p>
     * If the given input is null, then this method
     * returns null.
     * <p>
     * If the input is less than 2 characters long,
     * then the input is returned with the <code>start</code>
     * color.
     *
     * @param input The input
     * @param start The starting color, on the left
     * @param end The end color, on the right
     * @return The text colored as a gradient
     */
    public static TextComponent gradient(String input, TextColor start, TextColor end) {
        if (input == null) {
            return null;
        }

        int length = input.length();
        char[] chars = input.toCharArray();

        // Not enough space for gradient
        if (length < 2) {
            return Component.text(input).color(start);
        }

        var builder = Component.text();

        for (int i = 0; i < length; i++) {
            var c = chars[i];
            float progress = ((float) i) / length;

            builder.append(
                    Component.text(c, TextColor.lerp(progress, start, end))
            );
        }

        return builder.build();
    }

    /**
     * Gets an items display name.
     * <p>
     * If the item has a custom name, returns that, otherwise
     * it'll return a translatable component for
     * the item's type.
     * <p>
     * It should be noted that this function works differently
     * to {@link ItemStack#displayName()} as this is made to
     * function more as a chat-friendly version of that function,
     * meaning it doesn't automatically apply any colors to the
     * item's display name or force italics, if they've been
     * disabled.
     *
     * @param item The item to get the display name for
     * @return The item's display name
     */
    public static Component itemDisplayName(ItemStack item) {
        net.minecraft.world.item.ItemStack nms = CraftItemStack.asNMSCopy(item);
        Component hoverName = PaperAdventure.asAdventure(nms.getHoverName());

        if(nms.hasCustomHoverName()
                && hoverName.decoration(TextDecoration.ITALIC) == TextDecoration.State.NOT_SET
        ) {
            hoverName = hoverName.decorate(TextDecoration.ITALIC);
        }

        return hoverName
                .hoverEvent(item);
    }

    /**
     * Takes an enum input like OAK_SIGN and returns "Oak Sign"
     * @param anum The enum to normalize
     * @return The normalized input.
     */
    public static String prettyEnumName(@NotNull Enum<?> anum) {
        return WordUtils.capitalizeFully(anum.name().replaceAll("_", " "));
    }

    /**
     * Creates a pretty location message for easy readability.
     * <p>
     * Note: Does not use exact decimal cords, rather uses block cords
     * @param l The location to format for
     * @param includeWorld Whether to include the world's name in the message
     * @return The formatted easily readable location message
     */
    public static TextComponent prettyLocation(Location l, boolean includeWorld) {
        return text(
                String.format("X: %s Y: %s Z: %s%s",
                        l.getBlockX(),
                        l.getBlockY(),
                        l.getBlockZ(),
                        includeWorld ? " world: " + formatWorldName(l.getWorld()) : ""
                )
        );
    }

    /**
     * Creates a location message that when clicked teleports you to the location.
     * @see #prettyLocation(Location, boolean)
     * @param l The location to fomrat for
     * @param includeWorld Whether to include the world in the message
     * @return The formatted and clickable message
     */
    public static TextComponent clickableLocation(Location l, boolean includeWorld){
        return prettyLocation(l, includeWorld)
                .hoverEvent(text("Click to teleport!"))
                .clickEvent(CommandTeleportExact.createLocationClick(l));
    }

    /**
     * Formats an item's name and amount into a message, eg: "12 Oak Sign".
     * <p></p>
     * If you wanna figure out how to pluralize this mess, have fun
     * @param itemStack The itemstack to format for
     * @param amount The amount to show
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
     * Same as {@link #itemAndAmount(ItemStack, int)} except uses the item's amount
     * @param item The item to format for
     * @return The formatted message with the item's amount.
     */
    public static TextComponent itemAndAmount(ItemStack item) {
        return itemAndAmount(item, item.getAmount());
    }

    /**
     * Gets a display name from the given {@link CommandSource}
     * object.
     * <p>
     * If the source is a player it will return {@link User#displayName()},
     * else it just returns the default {@link CommandSource#displayName()}
     * @param source The source to get the display name of
     * @return The source's display name
     */
    public static Component sourceDisplayName(CommandSource source) {
        if (source.isPlayer()) {
            return Users.get(source.asOrNull(Player.class))
                    .displayName();
        }

        return source.displayName();
    }

    /**
     * Creates a non italic style with the given text color
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
     * @param time The time stamp to format
     * @return The formatted date
     * @see #formatDate(Date)
     */
    public static Component formatDate(long time) {
        return formatDate(new Date(time));
    }

    /**
     * Formats the given date with the {@link #DATE_FORMAT} format
     * <p>
     * This is functionally identical to
     * <code>Component.text(DATE_FORMAT.format(date))</code>
     * @param date The date to format
     * @return The formatted date
     */
    public static Component formatDate(Date date) {
        return text(DATE_FORMAT.format(date));
    }

    /**
     * Formats the given world name.
     * <p>
     * If the given world is the over world,
     * it returns "Overworld", or if the input
     * is the resource world, then "Resource World"
     * is returned, otherwise it replaces all
     * underscores in the world's name and replaces
     * them with spaces, it then capitalizes the
     * first letter of each word in the resulting
     * string.
     *
     * @param world The world's name to format
     * @return The formatted world name
     */
    public static String formatWorldName(World world) {
        if (world.equals(Worlds.overworld())) {
            return "Overworld";
        }

        if (world.equals(Worlds.resource())) {
            return "Resource World";
        }

        return WordUtils.capitalizeFully(world.getName()
                .replaceAll("world_", "")
                .replaceAll("_world", "")
                .replaceAll("_", " ")
        );
    }

    /* ----------------------------- FORMATTERS ------------------------------ */

    /**
     * Formats a given component in the same way as {@link java.text.MessageFormat}.
     * If the given string contains any {@link ChatEmotes} or color codes, they will
     * be formatted before the arguments of the format are formatted
     * <p>
     * Delegate method for {@link #format(String, Style, Object...)} with an
     * empty {@link Style}
     *
     * @param format The message format to use
     * @param args Any optional arguments to use in formatting
     * @return The formatted component
     * @see #format(String, Style, Object...)
     */
    public static Component format(String format, Object... args) {
        return format(format, Style.empty(), args);
    }

    /**
     * Formats the given component in the same way as {@link java.text.MessageFormat}.
     * If the given string contains any {@link ChatEmotes} or color codes, they will
     * be formatted before the arguments of the format are formatted
     * <p>
     * Delegate method for {@link #format(String, Style, Object...)} wit the style
     * set to the given color
     *
     * @param format The message format to use
     * @param color The color to apply to the base component
     * @param args Any optional arguments to use in formatting
     * @return The formatted component
     * @see #format(String, Style, Object...)
     */
    public static Component format(String format, TextColor color, Object... args) {
        return format(format, Style.style(color), args);
    }

    /**
     * Formats the given component in the same style as {@link java.text.MessageFormat} and
     * Adventure APIs {@link TranslatableComponent}s.
     * If the given string contains any {@link ChatEmotes} or color codes, they will
     * be formatted before the arguments of the format are formatted
     * <p>
     * Delegate method for {@link #format(Component, Object...)}
     * @param format The message format, eg: '{0} had a good day today!'
     * @param style The style to apply to the base component
     * @param args Any optional arguments to use in formatting
     * @return The formatted component
     * @see #format(Component, Object...)
     */
    public static Component format(String format, Style style, Object... args) {
        return format(renderString(format).style(style), args);
    }

    /**
     * Formats the given component in the same style as {@link java.text.MessageFormat}
     * and Adventure APIs {@link TranslatableComponent}s.
     * <p>
     * Uses {@link ComponentFormat} to format the given input.
     * @param format The message to format, eg: '{0} had a good day today! Did you have {0} day today too?'
     * @param args Any optional arguments to use in formatting
     * @return The formatted component, this will return
     *         <code>format</code> if args are null or empty
     * @see ComponentFormat
     */
    public static Component format(Component format, Object... args) {
        // Arguments are null or empty, we've got nothing
        // to format, return the base format
        if (args == null || args.length <= 0) {
            return format;
        }

        // Construct the component format and
        // get the formatted text from that
        return new ComponentFormat(format, args).asComponent();
    }

    /* ----------------------------------------------------------- */

    /**
     * Gets the component value of the arg object
     * <p>
     * If the given value is a {@link ComponentLike} or
     * {@link Component} then the argument itself is
     * returned.
     * <p>
     * Then this method tests if the given argument is either
     * a {@link Translatable} object or a {@link net.kyori.adventure.text.KeybindComponent.KeybindLike}
     * object, if it is, it returns a component respective to
     * its type.
     * <p>
     * If the argument is null, then a "null" text component
     * is returned.
     * <p>
     * Otherwise {@link Text#renderString(String)} is used
     * to render the object's {@link String#valueOf(Object)}
     * result to a component, meaning this method, if given a
     * string with color codes or emotes, will translate them
     * @param arg The arg object
     * @return The component value of the given argument
     */
    public static @NotNull Component valueOf(@Nullable Object arg) {
        if (arg == null) {
            return Messages.NULL;
        }

        // This basically is a component
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

        // Some kind of an object, we don't what it is,
        // maybe a string or who knows, so just call
        // String#valueOf(Object) on it and translate any
        // resulting color codes, links and emotes into text
        return renderString(String.valueOf(arg));
    }

    /* ----------------------------- ARG JOINERS ------------------------------ */

    public static ArgJoiner argJoiner(AbstractCommand command) {
        return argJoiner("/" + command.getName());
    }

    public static ArgJoiner argJoiner(String prefix) {
        return new ArgJoiner(prefix);
    }

    @RequiredArgsConstructor
    public static
    class ArgJoiner {
        @Getter
        private final String prefix;
        private final Map<Argument, String> values = new HashMap<>();

        public ArgJoiner add(Argument argument, String val) {
            values.put(argument, val);
            return this;
        }

        public <T> ArgJoiner add(Argument<T> argument, T val) {
            if (val == null || argument == null) {
                return this;
            }

            return add(argument, String.valueOf(val));
        }

        @Override
        public String toString() {
            var joiner = new StringJoiner(" ");
            joiner.add(prefix);

            for (var s: values.entrySet()) {
                joiner
                        .add(s.getKey().getName())
                        .add(ArgsArgument.EQUALS_SEPARATOR + "")
                        .add(s.getValue());
            }

            return joiner.toString();
        }

        public ClickEvent joinClickable() {
            return ClickEvent.runCommand(toString());
        }
    }
}