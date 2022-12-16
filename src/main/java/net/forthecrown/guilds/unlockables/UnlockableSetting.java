package net.forthecrown.guilds.unlockables;

import lombok.Getter;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.guilds.GuildPermission;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

import static net.forthecrown.guilds.menu.GuildMenus.GUILD;

@Getter
public enum UnlockableSetting implements Unlockable {
    PUBLIC(20, 1000,
            ItemStacks.builder(Material.WRITABLE_BOOK)
                    .setName(Component.text("Toggle Public/Private", NamedTextColor.YELLOW))
                    .build(),
            GuildPermission.CAN_CHANGE_PUBLIC
    ),

    VISIT(21, 1000,
            ItemStacks.builder(Material.IRON_BOOTS)
                    .setName(Component.text("Toggle Visit", NamedTextColor.YELLOW))
                    .setFlags(ItemFlag.HIDE_ATTRIBUTES)
                    .build(),
            GuildPermission.CAN_CHANGE_VISIT
    ),

    BANNER(23, 1000,
            ItemStacks.builder(Material.WHITE_BANNER)
                    .setName(Component.text("Guild Banner", NamedTextColor.YELLOW))
                    .setFlags(ItemFlag.HIDE_POTION_EFFECTS)
                    .build(),
            GuildPermission.CAN_CHANGE_BANNER
    ),
    ;

    private final int slot, expRequired;
    private final ItemStack item;
    private final GuildPermission perm;

    UnlockableSetting(int slot, int expRequired, ItemStack item, GuildPermission perm) {
        this.slot = slot;
        this.expRequired = expRequired;
        this.item = item;
        this.perm = perm;
    }

    @Override
    public String getKey() {
        return name().toLowerCase();
    }

    @Override
    public Component getName() {
        return Text.itemDisplayName(item);
    }

    // YOU DON'T FUGGIN SAY, I'm not fixing this, do it yourself
    //   - Jules
    @Override // This has to be done better
    public MenuNode toInvOption() {
        if (this == BANNER) {
            return MenuNode.builder()
                    .setItem((user, context) -> {
                        var guild = context.get(GUILD);
                        var i = item.clone();
                        ItemMeta meta = i.getItemMeta();
                        List<Component> lore = new ArrayList<>();

                        // Banner is editable or not yet unlocked
                        if (isUnlocked(guild)) {
                            // Get patterns of guild banner
                            i.setType(guild.getSettings().getBanner().getType());
                            BannerMeta bannerMeta = (BannerMeta) guild.getSettings().getBanner().getItemMeta();
                            ((BannerMeta) meta).setPatterns(bannerMeta.getPatterns());

                            lore.add(Component.text("Drag a banner item from your inventory", NamedTextColor.GRAY)
                                    .decoration(TextDecoration.ITALIC, false));
                            lore.add(Component.text("onto this item to change the banner.", NamedTextColor.GRAY)
                                    .decoration(TextDecoration.ITALIC, false));
                        } else {
                            lore.add(Component.text("Allows editing the guild banner.", NamedTextColor.GRAY)
                                    .decoration(TextDecoration.ITALIC, false));
                            lore.add(getProgressComponent(guild));
                            lore.add(Component.empty());
                            lore.add(getClickComponent());
                            lore.add(getShiftClickComponent());
                        }

                        meta.lore(lore);
                        i.setItemMeta(meta);

                        return i;
                    })

                    .setRunnable((user, context, click) -> {
                        onClick(user, click, context, () -> {
                            ItemStack cursor = click.getCursorItem();

                            if (ItemStacks.isEmpty(cursor)
                                    || cursor.getItemMeta() == null
                            ) {
                                user.sendMessage(
                                        Component.text(
                                                "Drag a banner item from your inventory onto the option to change the banner.",
                                                NamedTextColor.GRAY
                                        )
                                );
                            } else {
                                var guild = context.getOrThrow(GUILD);

                                if (!(click.getCursorItem().getItemMeta() instanceof BannerMeta)) {
                                    throw Exceptions.NOT_A_BANNER;
                                }

                                guild.getSettings().setBanner(click.getCursorItem());

                                click.getInventory().setItem(slot, (ItemStack) null);
                                click.shouldReloadMenu(true);
                                click.cancelEvent(false);
                            }
                        });
                    })

                    .build();

        } else if (this == PUBLIC) {
            return MenuNode.builder()
                    .setItem((user, context) -> {
                        var i = item.clone();
                        var guild = context.get(GUILD);

                        ItemMeta meta = i.getItemMeta();
                        List<Component> lore = new ArrayList<>();

                        // Toggle is available or not yet unlocked
                        if (isUnlocked(guild)) {
                            lore.add(Component.text("Click to toggle invitation required to join.", NamedTextColor.GRAY)
                                    .decoration(TextDecoration.ITALIC, false));
                            lore.add(Component.text("Guild is currently ")
                                    .color(NamedTextColor.GRAY)
                                    .decoration(TextDecoration.ITALIC, false)
                                    .append(Component.text(guild.getSettings().isPublic() ? "Public" : "Private", NamedTextColor.WHITE))
                                    .append(Component.text(".")));
                        } else {
                            lore.add(Component.text("Allows toggling invitation required to join.", NamedTextColor.GRAY)
                                    .decoration(TextDecoration.ITALIC, false));
                            lore.add(getProgressComponent(guild));
                            lore.add(Component.empty());
                            lore.add(getClickComponent());
                            lore.add(getShiftClickComponent());
                        }

                        meta.lore(lore);
                        i.setItemMeta(meta);

                        return i;
                    })

                    .setRunnable((user, context, click) -> {
                        onClick(user, click, context, () -> {
                            // Actual toggling of setting
                            var guild = context.get(GUILD);

                            boolean toggle = !guild.getSettings().isPublic();
                            guild.getSettings().setPublic(toggle);

                            guild.sendMessage(user.displayName().color(NamedTextColor.YELLOW)
                                    .append(Component.text(" has made the guild ", NamedTextColor.GRAY)
                                            .append(Component.text(toggle ? "Public" : "Private", NamedTextColor.WHITE))
                                            .append(Component.text("."))));
                        });
                    })

                    .build();
        } else {
            return MenuNode.builder()
                    .setItem((user, context) -> {
                        var i = item.clone();
                        var guild = context.getOrThrow(GUILD);

                        ItemMeta meta = i.getItemMeta();
                        List<Component> lore = new ArrayList<>();

                        // Toggle is available or not yet unlocked
                        if (isUnlocked(guild)) {
                            lore.add(Component.text("Click to toggle allowing everyone to /visit the guild.", NamedTextColor.GRAY)
                                    .decoration(TextDecoration.ITALIC, false));
                            lore.add(Component.text("Guild's visit location is currently ", NamedTextColor.GRAY)
                                    .decoration(TextDecoration.ITALIC, false)
                                    .append(Component.text(guild.getSettings().allowsVisit() ? "Public" : "Private", NamedTextColor.WHITE))
                                    .append(Component.text(".")));
                        } else {
                            lore.add(Component.text("Allows toggling /visit to everyone or only members.", NamedTextColor.GRAY)
                                    .decoration(TextDecoration.ITALIC, false));
                            lore.add(getProgressComponent(guild));
                            lore.add(Component.empty());
                            lore.add(getClickComponent());
                            lore.add(getShiftClickComponent());
                        }

                        meta.lore(lore);
                        i.setItemMeta(meta);

                        return i;
                    })

                    .setRunnable((user, context, click) -> {
                        onClick(user, click, context, () -> {
                            // Actual toggling of setting
                            var guild = context.getOrThrow(GUILD);

                            boolean toggle = !guild.getSettings().allowsVisit();
                            guild.getSettings().allowsVisit(toggle);

                            guild.sendMessage(user.displayName().color(NamedTextColor.YELLOW)
                                    .append(Component.text(" has made the guild's visit location ", NamedTextColor.GRAY)
                                            .append(Component.text(toggle ? "Public" : "Private", NamedTextColor.WHITE))
                                            .append(Component.text("."))));
                        });
                    })

                    .build();
        }
    }
}