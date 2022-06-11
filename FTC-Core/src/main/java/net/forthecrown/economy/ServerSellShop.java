package net.forthecrown.economy;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.inventory.FtcInventory;
import net.forthecrown.inventory.ItemStacks;
import net.forthecrown.inventory.builder.*;
import net.forthecrown.inventory.builder.options.CordedInventoryOption;
import net.forthecrown.inventory.builder.options.InventoryRunnable;
import net.forthecrown.user.CrownUser;
import net.forthecrown.inventory.ItemStackBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import static net.forthecrown.core.chat.FtcFormatter.nonItalic;

/**
 * This was meant to be the planned version of the sellshop where
 * you could just fill up a random empty inventory and sell whatever
 * was inside. Not finished though.
 * @deprecated Not finished
 */
@Deprecated
public final class ServerSellShop {
    private ServerSellShop() {}

    public static final InventoryPos SELL_ALL_POS = new InventoryPos(8, 0);
    public static final InventoryPos INFO_POS = new InventoryPos(8, 1);

    public static final BuiltInventory SERVER_SELL_SHOP = new InventoryBuilder(54)
            .title(Component.text("Server sell shop"))

            .add(new SellAllOption())
            .add(new SellShopInfoOption())

            .onClick(new SellShopPlaceListener())
            .onClose(new SellShopCloseListener())

            .build();

    static boolean isLegalSlot(int slot) {
        return slot != SELL_ALL_POS.getSlot() && slot != INFO_POS.getSlot();
    }
}

class SellShopCloseListener implements InventoryCloseAction {
    @Override
    public void onClose(Player player, FtcInventory inventory, InventoryCloseEvent.Reason reason) {
        PlayerInventory pInv = player.getInventory();
        int firstEmpty = pInv.firstEmpty();
        Location pLoc = player.getLocation();
        World world = player.getWorld();

        for (int i = 0; i < inventory.getSize(); i++) {
            if(!ServerSellShop.isLegalSlot(i)) continue;

            ItemStack item = inventory.getItem(i);
            if(ItemStacks.isEmpty(item)) continue;

            if(firstEmpty == -1) world.dropItem(pLoc, item);
            else pInv.addItem(item);

            firstEmpty = pInv.firstEmpty();
        }
    }
}

class SellShopPlaceListener implements InventoryRunnable {
    @Override
    public void onClick(CrownUser user, ClickContext context) throws CommandSyntaxException {
        if(!ServerSellShop.isLegalSlot(context.getSlot())) return;
        context.setCancelEvent(false);
    }
}

class SellShopInfoOption implements CordedInventoryOption {
    @Override
    public InventoryPos getPos() {
        return ServerSellShop.INFO_POS;
    }

    @Override
    public void place(FtcInventory inventory, CrownUser user) {
        inventory.setItem(
                getSlot(),
                new ItemStackBuilder(Material.KNOWLEDGE_BOOK, 1)
                        .setName(Component.text("Material info").style(nonItalic(NamedTextColor.GOLD)))
                        .addLore(Component.text("See what you can and can't sell").style(nonItalic(NamedTextColor.YELLOW)))
                        .build()
        );
    }

    @Override
    public void onClick(CrownUser user, ClickContext context) throws CommandSyntaxException {

    }
}

class SellAllOption implements CordedInventoryOption {
    @Override
    public InventoryPos getPos() {
        return ServerSellShop.SELL_ALL_POS;
    }

    @Override
    public void place(FtcInventory inventory, CrownUser user) {
        inventory.setItem(
                getPos(),
                new ItemStackBuilder(Material.GREEN_CONCRETE)
                        .setName(Component.text("Sell all").style(nonItalic(NamedTextColor.AQUA)))
                        .addLore(Component.text("Sell all materials in the inventory.").style(nonItalic(NamedTextColor.WHITE)))
                        .build()
        );
    }

    @Override
    public void onClick(CrownUser user, ClickContext context) throws CommandSyntaxException {
    }
}