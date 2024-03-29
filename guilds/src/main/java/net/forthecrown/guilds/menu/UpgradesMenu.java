package net.forthecrown.guilds.menu;

import java.util.Arrays;
import lombok.Getter;
import net.forthecrown.guilds.unlockables.Unlockable;
import net.forthecrown.guilds.unlockables.UnlockableSetting;
import net.forthecrown.guilds.unlockables.Upgradable;
import net.forthecrown.menu.MenuBuilder;
import net.forthecrown.menu.MenuNode;
import net.forthecrown.menu.Menus;
import net.forthecrown.menu.page.MenuPage;
import net.forthecrown.user.User;
import net.forthecrown.utils.context.Context;
import net.forthecrown.utils.inventory.ItemStacks;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class UpgradesMenu extends MenuPage {

  private final GuildRanksMenu ranksMenu;
  private final ChunkUpgradesMenu chunkMenu;
  private final CosmeticsMenu cosmeticsMenu;
  private final GuildDiscordMenu discordMenu;
  private final MultiplierMenu multiplierMenu;

  public UpgradesMenu(MenuPage parent) {
    super(parent);

    ranksMenu = new GuildRanksMenu(this);
    chunkMenu = new ChunkUpgradesMenu(this);
    cosmeticsMenu = new CosmeticsMenu(this);
    discordMenu = new GuildDiscordMenu(this);
    multiplierMenu = new MultiplierMenu(this);

    initMenu(Menus.builder(Menus.MAX_INV_SIZE, "Guild Upgrades"), true);
  }

  @Override
  protected void createMenu(MenuBuilder builder) {
    builder.add(22, ranksMenu);
    builder.add(29, chunkMenu);
    builder.add(24, cosmeticsMenu);
    builder.add(3, 4, discordMenu);
    builder.add(5, 4, multiplierMenu);

    addAll(builder, UnlockableSetting.values());
    addAll(builder, Upgradable.values());
  }

  static void addAll(MenuBuilder builder, Unlockable... values) {
    Arrays.stream(values)
        .forEach(unlockable -> builder.add(unlockable.getSlot(), unlockable.toInvOption()));
  }

  @Override
  protected MenuNode createHeader() {
    return this;
  }

  @Override
  public @Nullable ItemStack createItem(@NotNull User user, @NotNull Context context) {
    return ItemStacks.builder(Material.GOLDEN_APPLE)
        .setName("&eGuild upgrades")
        .addLore("&7Upgrades that can be unlocked with Guild Exp.")
        .build();
  }
}