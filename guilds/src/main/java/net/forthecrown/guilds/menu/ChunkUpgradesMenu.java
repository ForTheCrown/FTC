package net.forthecrown.guilds.menu;

import net.forthecrown.guilds.unlockables.UnlockableChunkUpgrade;
import net.forthecrown.menu.page.MenuPage;
import net.forthecrown.user.User;
import net.forthecrown.utils.context.Context;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.menu.MenuBuilder;
import net.forthecrown.menu.MenuNode;
import net.forthecrown.menu.Menus;
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
    UpgradesMenu.addAll(builder, UnlockableChunkUpgrade.values());
  }

  @Override
  protected MenuNode createHeader() {
    return this;
  }

  @Override
  public @Nullable ItemStack createItem(@NotNull User user, @NotNull Context context) {
    return ItemStacks.builder(Material.BEACON)
        .setName("&eChunk upgrades")
        .addLore("&7Effects applied in guild chunks")
        .build();
  }
}