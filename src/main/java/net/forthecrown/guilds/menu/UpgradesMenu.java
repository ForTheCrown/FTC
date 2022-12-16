package net.forthecrown.guilds.menu;

import lombok.Getter;
import net.forthecrown.guilds.unlockables.Unlockable;
import net.forthecrown.guilds.unlockables.UnlockableSetting;
import net.forthecrown.guilds.unlockables.Upgradable;
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

import java.util.Arrays;

@Getter
public class UpgradesMenu extends MenuPage {
    private final GuildRanksMenu ranksMenu;
    private final ChunkUpgradesMenu chunkMenu;
    private final CosmeticsMenu cosmeticsMenu;

    public UpgradesMenu(MenuPage parent) {
        super(parent);

        ranksMenu = new GuildRanksMenu(this);
        chunkMenu = new ChunkUpgradesMenu(this);
        this.cosmeticsMenu = new CosmeticsMenu(this);

        initMenu(Menus.builder(Menus.MAX_INV_SIZE, "Guild Upgrades"), true);
    }

    @Override
    protected void createMenu(MenuBuilder builder) {
        builder.add(22, ranksMenu);
        builder.add(29, chunkMenu);
        builder.add(24, cosmeticsMenu);

        addAll(UnlockableSetting.values(), builder);
        addAll(Upgradable.values(), builder);
    }

    static void addAll(Unlockable[] values, MenuBuilder builder) {
        Arrays.stream(values).forEach(unlockable -> builder.add(unlockable.getSlot(), unlockable.toInvOption()));
    }

    @Override
    protected MenuNode createHeader() {
        return this;
    }

    @Override
    public @Nullable ItemStack createItem(@NotNull User user, @NotNull InventoryContext context) {
        return ItemStacks.builder(Material.GOLDEN_APPLE)
                .setName("&eGuild upgrades")
                .addLore("&7Upgrades that can be unlocked with Guild Exp.")
                .build();
    }
}