package net.forthecrown.core.admin.ui;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import net.forthecrown.text.Text;
import net.forthecrown.text.writer.TextWriters;
import net.forthecrown.user.User;
import net.forthecrown.user.UserFormat;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.*;
import net.forthecrown.utils.inventory.menu.context.ClickContext;
import net.forthecrown.utils.inventory.menu.context.InventoryContext;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

abstract class AdminUiPage implements MenuNode {
    static final Slot HEAD_SLOT = Slot.of(4);

    @Getter
    private final Menu inventory;

    @Getter
    private final AdminUiPage parent;

    MenuNodeItem item;

    public AdminUiPage(Component title, int size, AdminUiPage parent) {
        this.parent = parent;

        var builder = Menus.builder(size, title);

        this.item = MenuNodeItem.of(user -> {
            return ItemStacks.builder(Material.PAPER)
                    .setName("&e< Go back")
                    .addLore("&7Go back to the previous page")
                    .build();
        });

        if (hasBorder()) {
            builder.addBorder(
                    ItemStacks.builder(Material.GRAY_STAINED_GLASS_PANE)
                            .setName(" ")
                            .build()
            );
        }

        if (parent != null) {
            builder.add(0, parent);
        }

        builder.add(HEAD_SLOT,
                MenuNode.builder()
                        .setItem((user, context) -> {
                            var punished = context.get(AdminUI.ENTRY).getUser();

                            var item = ItemStacks.headBuilder()
                                    .setProfile(punished)
                                    .setName(punished.displayName().style(Text.NON_ITALIC));

                            var writer = TextWriters.loreWriter();
                            UserFormat format = UserFormat.create(user).disableHover();
                            UserFormat.applyProfileStyle(writer);
                            format.format(writer);

                            writer.newLine();
                            item.addLore(writer.getLore());

                            return item.build();
                        })
                        .build()
        );

        createMenu(builder);

        this.inventory = builder.build();
    }

    protected abstract void createMenu(MenuBuilder builder);

    protected boolean hasBorder() {
        return true;
    }

    @Override
    public void onClick(User user, InventoryContext context, ClickContext click) throws CommandSyntaxException {
        getInventory().open(user, context);
    }

    @Override
    public ItemStack createItem(@NotNull User user, @NotNull InventoryContext context) {
        return item.createItem(user, context);
    }
}