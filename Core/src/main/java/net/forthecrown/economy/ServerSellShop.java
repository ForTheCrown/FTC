package net.forthecrown.economy;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.inventory.FtcInventory;
import net.forthecrown.inventory.FtcItems;
import net.forthecrown.inventory.builder.*;
import net.forthecrown.inventory.builder.options.CordedInventoryOption;
import net.forthecrown.inventory.builder.options.InventoryRunnable;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

//Mmmm yes, internal classes
public final class ServerSellShop {
    private ServerSellShop() {}

    public static final BuiltInventory SERVER_SELL_SHOP = new InventoryBuilder(54)
            .title(Component.text("Server sell shop"))

            .add(new SellAllOption())
            .add(new SellShopInfoOption())

            .onClick(new SellShopPlaceListener())
            .onClose(new SellShopCloseListener())

            .build();

    public static final InventoryPos SELL_ALL_POS = new InventoryPos(8, 0);
    public static final InventoryPos INFO_POS = new InventoryPos(8, 1);
}

class SellShopCloseListener implements InventoryCloseAction {
    @Override
    public void onClose(Player player, FtcInventory inventory, InventoryCloseEvent.Reason reason) {
        PlayerInventory pInv = player.getInventory();
        int firstEmpty = pInv.firstEmpty();
        Location pLoc = player.getLocation();
        World world = player.getWorld();

        for (int i = 0; i < inventory.getSize(); i++) {
            if(i == ServerSellShop.SELL_ALL_POS.getSlot() || i == ServerSellShop.INFO_POS.getSlot()) continue;

            ItemStack item = inventory.getItem(i);
            if(FtcItems.isEmpty(item)) continue;

            if(firstEmpty == -1) world.dropItem(pLoc, item);
            else pInv.addItem(item);

            firstEmpty = pInv.firstEmpty();
        }
    }
}

class SellShopPlaceListener implements InventoryRunnable {
    @Override
    public void onClick(CrownUser user, ClickContext context) throws CommandSyntaxException {
    }
}

class SellShopInfoOption implements CordedInventoryOption {
    @Override
    public InventoryPos getPos() {
        return ServerSellShop.INFO_POS;
    }

    @Override
    public void place(FtcInventory inventory, CrownUser user) {

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

    }

    @Override
    public void onClick(CrownUser user, ClickContext context) throws CommandSyntaxException {

    }
}
