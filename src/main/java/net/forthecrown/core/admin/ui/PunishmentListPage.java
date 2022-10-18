package net.forthecrown.core.admin.ui;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.admin.PunishEntry;
import net.forthecrown.core.admin.PunishType;
import net.forthecrown.core.admin.Punishment;
import net.forthecrown.text.Text;
import net.forthecrown.text.writer.TextWriters;
import net.forthecrown.user.User;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.MenuNodeItem;
import net.forthecrown.utils.inventory.menu.context.ClickContext;
import net.forthecrown.utils.inventory.menu.context.InventoryContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static net.forthecrown.core.admin.ui.AdminUI.ENTRY;
import static net.forthecrown.core.admin.ui.AdminUI.PUNISHMENT;
import static net.forthecrown.text.Text.nonItalic;

class PunishmentListPage extends ListUiPage<Punishment> {
    private final boolean current;

    public PunishmentListPage(AdminUiPage parent, boolean current) {
        super(
                Component.text((current ? "Current" : "Past") + " punishments"),
                parent
        );

        this.current = current;
    }

    @Override
    protected MenuNodeItem createMenuButton() {
        return (user, context) -> {
            var builder = ItemStacks.builder(Material.CHEST)
                    .setName((current ? "Current" : "Past") + " punishments");

            if (current) {
                builder.addLore("&7Currently active punishments");
            } else {
                builder.addLore("&7Punishments this user has")
                        .addLore("&7been given in the past");
            }

            if (getList(context.get(ENTRY)).isEmpty()) {
                builder.addLore("&cNo entries to show");
            }

            return builder.build();
        };
    }

    @Override
    protected List<Punishment> getList(PunishEntry entry) {
        return current ? entry.getCurrent() : entry.getPast();
    }

    @Override
    protected ItemStack getItem(Punishment punish, PunishEntry punishEntry) {
        return createItem(punish, current);
    }

    static ItemStack createItem(Punishment punish, boolean current) {
        var builder = ItemStacks.builder(typeToMaterial(punish.getType()));

        builder.setName(
                Text.format("{0} {1}",
                        nonItalic(NamedTextColor.WHITE),
                        current ? "Active" : "Past",
                        punish.getType().presentableName()
                )
        );

        var writer = TextWriters.loreWriter();
        punish.writeDisplay(writer);

        builder.addLore(writer.getLore());

        return builder.build();
    }

    static Material typeToMaterial(PunishType type) {
        return switch (type) {
            case BAN -> Material.IRON_AXE;
            case JAIL -> Material.IRON_BARS;
            case MUTE -> Material.BARRIER;
            case SOFT_MUTE -> Material.STRUCTURE_VOID;
            case IP_BAN -> Material.DIAMOND_AXE;
            case KICK -> Material.NETHERITE_BOOTS;
        };
    }

    // On entry click
    @Override
    protected void onClick(Punishment entry, int index, User user, InventoryContext context) {
        if (!current) {
            return;
        }

        var node = new PardonPage(this, entry);
        node.getInventory().open(user, context);
    }

    // On page open
    @Override
    public void onClick(User user, InventoryContext context, ClickContext click) throws CommandSyntaxException {
        super.onClick(user, context, click);

        if (current) {
            context.set(PUNISHMENT, null);
        }
    }
}