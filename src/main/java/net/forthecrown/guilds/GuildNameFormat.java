package net.forthecrown.guilds;

import com.google.gson.JsonElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.forthecrown.core.registry.FtcKeyed;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.utils.io.JsonWrapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.function.Supplier;

import static net.kyori.adventure.text.Component.text;

@Getter
@Setter
@AllArgsConstructor
public class GuildNameFormat {
    // todo serialize/deserialize
    public static final String
            BRACKETS_KEY = "brackets",
            COLORS_KEY = "colors",
            STYLE_KEY = "style";

    public static final int
            OPENING_BRACKET = 0,
            GUILD_NAME = 1,
            CLOSING_BRACKET = 2;

    private Bracket bracket;
    @Getter @Setter
    private Color color;
    @Getter @Setter
    private Stylee style;

    public static GuildNameFormat createDefault() {
        return new GuildNameFormat(
                Bracket.DEFAULT,
                Color.DEFAULT,
                Stylee.DEFAULT
        );
    }

    public Component apply(Guild guild) {
        String name = guild.getName();
        TextColor primary = guild.getSettings()
                .getPrimaryColor()
                .getTextColor();

        TextColor secondary = guild.getSettings()
                .getSecondaryColor()
                .getTextColor();

        String[] completeName = {
                bracket.getOpening(),
                name,
                bracket.getClosing()
        };

        return color.apply(
                completeName,
                primary,
                secondary,
                style.getBracketStyle(),
                style.getNameStyle()
        );
    }

    public boolean isDefault() {
        return bracket == Bracket.DEFAULT
                && style == Stylee.DEFAULT
                && color == Color.DEFAULT;
    }

    /* --------------------------- SERIALIZATION ---------------------------- */

    public JsonElement serialize() {
        var json = JsonWrapper.create();

        if (bracket != Bracket.DEFAULT) {
            json.add(BRACKETS_KEY,
                     Bracket.REGISTRY.writeJson(bracket).orElseThrow()
            );
        }

        if (color != Color.DEFAULT) {
            json.add(COLORS_KEY,
                    Color.REGISTRY.writeJson(color).orElseThrow()
            );
        }

        if (style != Stylee.DEFAULT) {
            json.add(STYLE_KEY,
                    Stylee.REGISTRY.writeJson(style).orElseThrow()
            );
        }

        return json.getSource();
    }

    public void deserialize(JsonElement element) {
        var json = JsonWrapper.wrap(element.getAsJsonObject());

        setBracket(
                Bracket.REGISTRY.readJson(json.get(BRACKETS_KEY))
                        .orElse(Bracket.DEFAULT)
        );

        setColor(
                Color.REGISTRY.readJson(json.get(COLORS_KEY))
                        .orElse(Color.DEFAULT)
        );

        setStyle(
                Stylee.REGISTRY.readJson(json.get(STYLE_KEY))
                        .orElse(Stylee.DEFAULT)
        );
    }

    /* ---------------------------- SUB CLASSES ----------------------------- */

    @RequiredArgsConstructor
    @Getter
    public enum Bracket implements FtcKeyed {
        DEFAULT("default", "[", "]"),
        ROUND("round", "(", ")"),
        ANGLE("angle", "<", ">"),
        SQUARE_SPECIAL1("specialSquare_0", "|[", "]|"),
        SQUARE_SPECIAL2("specialSquare_1", "=[", "]="),
        ;

        static final Registry<Bracket>
                REGISTRY = Registries.ofEnum(Bracket.class);

        @Getter
        private final String key, opening, closing;

        public Component getOpeningBracket(TextColor color) {
            return getOpeningBracket(Style.style(color));
        }

        public Component getOpeningBracket(Style style) {
            return text(opening, style);
        }

        public Component getClosingBracket(TextColor color) {
            return getClosingBracket(Style.style(color));
        }

        public Component getClosingBracket(Style style) {
            return text(closing, style);
        }

        // Default color, default style, preview brackets
        public Component getPreview(String guildName, TextColor primary, TextColor secondary) {
            return getOpeningBracket(secondary)
                    .append(text(guildName, primary))
                    .append(getClosingBracket(secondary));
        }
    }

    @RequiredArgsConstructor
    @Getter
    public enum Color implements FtcKeyed {
        DEFAULT("default") {
            @Override
            public Component apply(String[] fullName,
                                   TextColor primary,
                                   TextColor secondary,
                                   Style bracket,
                                   Style text
            ) {
                return text()
                        .append(text(
                                fullName[OPENING_BRACKET],
                                bracket.color(secondary)
                        ))
                        .append(text(
                                fullName[GUILD_NAME],
                                text.color(primary)
                        ))
                        .append(text(
                                fullName[CLOSING_BRACKET],
                                bracket.color(secondary)
                        ))
                        .build();
            }
        },

        ALTERNATE("alternate") {
            @Override
            public Component apply(String[] fullName,
                                   TextColor primary,
                                   TextColor secondary,
                                   Style bracket,
                                   Style text
            ) {
                // Supplier that flips the color used everytime it's called
                Supplier<TextColor> colorProvider = new Supplier<>() {
                    boolean isPrimary = false;

                    @Override
                    public TextColor get() {
                        isPrimary = !isPrimary;
                        return isPrimary ? primary : secondary;
                    }
                };

                var builder = text();

                builder.append(text(
                        fullName[OPENING_BRACKET],
                        bracket.color(colorProvider.get())
                ));

                for (var c: fullName[GUILD_NAME].toCharArray()) {
                    builder.append(text(
                            c,
                            text.color(colorProvider.get())
                    ));
                }

                builder.append(text(
                        fullName[CLOSING_BRACKET],
                        bracket.color(colorProvider.get())
                ));

                return builder.build();
            }
        },

        GRADIENT_2COLORS("gradient2") {
            @Override
            public Component apply(String[] fullName,
                                   TextColor primary,
                                   TextColor secondary,
                                   Style bracket,
                                   Style text
            ) {
                return Guilds.createNameGradient(
                        1,
                        fullName,
                        primary,
                        secondary,
                        bracket,
                        text
                );
            }
        },
        GRADIENT_3COLORS("gradient3") {
            @Override
            public Component apply(String[] fullName,
                                   TextColor primary,
                                   TextColor secondary,
                                   Style bracket,
                                   Style text
            ) {
                return Guilds.createNameGradient(
                        2,
                        fullName,
                        primary,
                        secondary,
                        bracket,
                        text
                );
            }
        },
        GRADIENT_4COLORS("gradient4") {
            @Override
            public Component apply(String[] fullName,
                                   TextColor primary,
                                   TextColor secondary,
                                   Style bracket,
                                   Style text
            ) {
                return Guilds.createNameGradient(
                        3,
                        fullName,
                        primary,
                        secondary,
                        bracket,
                        text
                );
            }
        },
        ;

        static final Registry<Color>
                REGISTRY = Registries.ofEnum(Color.class);

        @Getter
        private final String key;

        // Default brackets, default style, preview color
        public Component getPreview(String guildName,
                                    TextColor primary,
                                    TextColor secondary
        ) {
            return apply(
                    new String[]{
                            Bracket.DEFAULT.getOpening(),
                            guildName,
                            Bracket.DEFAULT.getClosing()
                    },

                    primary,
                    secondary,
                    Stylee.DEFAULT.getBracketStyle(),
                    Stylee.DEFAULT.getNameStyle()
            );
        }

        /**
         * Formats a guild's display name
         *
         * @param fullName The full name array containing the opening bracket,
         *                 the guild's name, and the closing bracket. Use the
         *                 {@link #CLOSING_BRACKET}, {@link #GUILD_NAME} and
         *                 {@link #OPENING_BRACKET} constants to access their
         *                 respective values.
         *
         * @param primary The guild's primary color.
         * @param secondary The guild's secondary color.
         * @param bracket The style to use for the brackets
         * @param text The style to use for the guild's name itself
         *
         * @return The formatted name
         */
        public abstract Component apply(String[] fullName,
                                        TextColor primary,
                                        TextColor secondary,
                                        Style bracket,
                                        Style text
        );
    }


    private static final Style noStyle = Style.style().build();

    @RequiredArgsConstructor
    @Getter
    public enum Stylee implements FtcKeyed {
        DEFAULT("default",
                noStyle,
                noStyle),
        FATB("boldBrackets",
                Style.style(TextDecoration.BOLD),
                noStyle),
        ITALIC("italic",
                Style.style(TextDecoration.ITALIC),
                Style.style(TextDecoration.ITALIC)),
        ITALIC_FATB("boldBracketsItalic",
                Style.style(TextDecoration.ITALIC, TextDecoration.BOLD),
                Style.style(TextDecoration.ITALIC)),
        FAT_STRIKED_B("strikedBoldBrackets",
                Style.style(TextDecoration.STRIKETHROUGH, TextDecoration.BOLD),
                noStyle),
        ;

        static final Registry<Stylee>
                REGISTRY = Registries.ofEnum(Stylee.class);

        private final String key;
        private final Style bracketStyle, nameStyle;

        // Default brackets, default color, preview styles
        public Component getPreview(String guildName, TextColor primary, TextColor secondary) {
            return text()
                    .append(text(Bracket.DEFAULT.getOpening(), bracketStyle.color(secondary)))
                    .append(text(guildName, nameStyle.color(primary)))
                    .append(text( Bracket.DEFAULT.getClosing(), bracketStyle.color(secondary)))
                    .build();
        }
    }

}