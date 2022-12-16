package net.forthecrown.guilds.unlockables.nameformat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.GuildNameFormat;
import net.forthecrown.guilds.GuildPermission;
import net.forthecrown.guilds.unlockables.Unlockable;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

import static net.forthecrown.guilds.menu.GuildMenus.GUILD;
import static net.forthecrown.utils.text.Text.nonItalic;
import static net.kyori.adventure.text.Component.text;

@RequiredArgsConstructor
@Getter
public enum UnlockableColorType implements Unlockable {
    DEFAULT(20, 0, GuildNameFormat.Color.DEFAULT),
    COLORS2(21, 500, GuildNameFormat.Color.ALTERNATE),
    COLORS3(22, 500, GuildNameFormat.Color.GRADIENT_2COLORS),
    COLORS4(23, 1000, GuildNameFormat.Color.GRADIENT_3COLORS),
    COLORS5(24, 1000, GuildNameFormat.Color.GRADIENT_4COLORS),
    ;

    @Getter
    private final int slot, expRequired;
    private final GuildNameFormat.Color color;

    @Override
    public GuildPermission getPerm() {
        return GuildPermission.CAN_CHANGE_GUILD_COSMETICS;
    }

    @Override
    public String getKey() {
        return name().toLowerCase();
    }

    @Override
    public Component getName() {
        return text("Name format color");
    }

    @Override
    public MenuNode toInvOption() {
        return MenuNode.builder()
                .setItem((user, context) -> {
                    Guild guild = context.getOrThrow(GUILD);

                    var item = ItemStacks.builder(Material.MOJANG_BANNER_PATTERN)
                            .setName(color.getPreview(
                                    guild.getName(),
                                    guild.getSettings().getPrimaryColor().getTextColor(),
                                    guild.getSettings().getSecondaryColor().getTextColor()))
                            .setFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_POTION_EFFECTS)
                            .build();

                    ItemMeta meta = item.getItemMeta();
                    List<Component> lore = meta.lore();

                    if (lore == null) {
                        lore = new ArrayList<>();
                    }

                    if (isUnlocked(guild)) {
                        boolean active = guild.getSettings()
                                .getNameFormat()
                                .getColor() == color;

                        // Add click-to-active or is-active line
                        lore.add(active
                                ? text("Active")
                                .color(NamedTextColor.YELLOW)
                                .decoration(TextDecoration.ITALIC, false)
                                : text("Click to activate")
                                .color(NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false)
                        );

                        if (active) {
                            meta.addEnchant(
                                    Enchantment.BINDING_CURSE,
                                    1,
                                    true
                            );
                        }
                    } else {
                        var style = nonItalic(NamedTextColor.GRAY);

                        // Add progress lore lines
                        lore.add(getProgressComponent(guild).style(style));
                        lore.add(Component.empty());
                        lore.add(getClickComponent().style(style));
                        lore.add(getShiftClickComponent().style(style));
                    }

                    meta.lore(lore);
                    item.setItemMeta(meta);

                    return item;
                })

                .setRunnable((user, context, c) -> onClick(user, c, context, () -> {
                    Guild guild = context.getOrThrow(GUILD);

                    // If not active
                    if (guild.getSettings().getNameFormat().getColor() != color) {
                        guild.getSettings().getNameFormat().setColor(color);
                    }
                }))

                .build();
    }
}