package net.forthecrown.guilds.menu;

import net.forthecrown.guilds.unlockables.UnlockableChunkUpgrade;
import net.forthecrown.user.User;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.MenuBuilder;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.inventory.menu.Menus;
import net.forthecrown.utils.inventory.menu.context.InventoryContext;
import net.forthecrown.utils.inventory.menu.page.MenuPage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChunkUpgradesMenu extends MenuPage {
    public ChunkUpgradesMenu(MenuPage parent) {
        super(parent);
        initMenu(
                Menus.builder(Menus.MAX_INV_SIZE, "Chunk upgrades"),
                true
        );
    }

    @Override
    protected void createMenu(MenuBuilder builder) {
        UpgradesMenu.addAll(UnlockableChunkUpgrade.values(), builder);
    }

    @Override
    protected MenuNode createHeader() {
        return this;
    }

    @Override
    public @Nullable ItemStack createItem(@NotNull User user, @NotNull InventoryContext context) {
        return ItemStacks.builder(Material.BEACON)
                .setName("&eChunk upgrades")
                .addLore("&7Effects applied in guild chunks")
                .build();
    }
}