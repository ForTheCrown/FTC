package net.forthecrown.guilds.unlockables;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.GuildColor;
import net.forthecrown.guilds.GuildPermission;
import net.forthecrown.user.User;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;

import static net.forthecrown.guilds.menu.GuildMenus.GUILD;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

@Getter
public enum UnlockableColor {
    WHITE(13, GuildColor.WHITE),
    BLACK(20, GuildColor.BLACK),
    GRAY(21, GuildColor.GRAY),
    LIGHT_GRAY(22, GuildColor.LIGHT_GRAY),
    BROWN(23, GuildColor.BROWN),
    PINK(24, GuildColor.PINK),
    CYAN(29, GuildColor.CYAN),
    LIGHT_BLUE(30, GuildColor.LIGHT_BLUE),
    BLUE(31, GuildColor.BLUE),
    PURPLE(32, GuildColor.PURPLE),
    MAGENTA(33, GuildColor.MAGENTA),
    GREEN(38, GuildColor.GREEN),
    LIME(39, GuildColor.LIME),
    YELLOW(40, GuildColor.YELLOW),
    ORANGE(41, GuildColor.ORANGE),
    RED(42, GuildColor.RED),
    ;

    @Getter
    static final UnlockableOption[] primaries;

    @Getter
    static final UnlockableOption[] secondaries;

    static {
        var values = values();

        primaries = new UnlockableOption[values.length];
        secondaries = new UnlockableOption[values.length];

        for (var v: values) {
            primaries[v.ordinal()] = v.getPrimaryOption();
            secondaries[v.ordinal()] = v.getSecondaryOption();
        }
    }

    private final int slot, expRequired;
    private final GuildColor color;

    private final UnlockableOption primaryOption;
    private final UnlockableOption secondaryOption;

    UnlockableColor(int slot, GuildColor color) {
        this.slot = slot;
        this.expRequired = 200;
        this.color = color;

        this.primaryOption = new UnlockableOption(this, true);
        this.secondaryOption = new UnlockableOption(this, false);
    }

    static void registerAll(Registry<Unlockable> unlockables) {
        for (var v: values()) {
            var p = v.getPrimaryOption();
            var e = v.getSecondaryOption();

            unlockables.register(p.getKey(), p);
            unlockables.register(e.getKey(), e);
        }
    }

    private static void setColor(boolean primary,
                                 Guild guild,
                                 User user,
                                 GuildColor color
    ) {
        if (primary) {
            guild.getSettings().setPrimaryColor(color);
        } else {
            guild.getSettings().setSecondaryColor(color);
        }

        guild.sendMessage(
                Text.format("&6{0, user}&r has changed the guild's &6{2}&r color to {1}.",
                        NamedTextColor.YELLOW,
                        user,
                        text(color.toText(), color.getTextColor()),
                        primary ? "primary" : "secondary"
                )
        );
    }

    @Getter
    @RequiredArgsConstructor
    private static class UnlockableOption implements Unlockable {
        private final UnlockableColor color;
        private final boolean primary;

        @Override
        public GuildPermission getPerm() {
            return GuildPermission.CAN_CHANGE_GUILD_COSMETICS;
        }

        @Override
        public String getKey() {
            return (primary ? "primary_color/" : "secondary_color/") + color.name();
        }

        @Override
        public Component getName() {
            return Text.format("{0} color: {1}",
                    primary ? "Primary" : "Secondary",
                    color.getColor().toText()
            );
        }

        @Override
        public int getSlot() {
            return color.getSlot();
        }

        @Override
        public int getExpRequired() {
            return color.getExpRequired();
        }

        @Override
        public MenuNode toInvOption() {
            return MenuNode.builder()
                    .setItem((user, context) -> {
                        var color = this.color.color;

                        var builder = ItemStacks.builder(color.toWool())
                                .setName(text(color.toText(), color.getTextColor()))
                                .setFlags(ItemFlag.HIDE_ENCHANTS);

                        var guild = context.getOrThrow(GUILD);

                        if (isUnlocked(guild)) {
                            boolean active = primary
                                    ? guild.getSettings().getPrimaryColor() == color
                                    : guild.getSettings().getSecondaryColor() == color;

                            if (active) {
                                builder.addEnchant(Enchantment.BINDING_CURSE, 1);
                                builder.addLore("&6Currently Selected");
                            } else {
                                builder.addLore("&7Click to select");
                            }
                        } else {
                            builder
                                    .addLore(getProgressComponent(guild))
                                    .addLoreRaw(empty())
                                    .addLore(getClickComponent())
                                    .addLore(getShiftClickComponent());
                        }

                        return builder.build();
                    })

                    .setRunnable((user, context, click) -> {
                        onClick(user, click, context, () -> {
                            var guild = context.getOrThrow(GUILD);
                            click.shouldReloadMenu(true);

                            setColor(
                                    primary,
                                    guild,
                                    user,
                                    color.color
                            );
                        });
                    })

                    .build();
        }
    }
}