package net.forthecrown.core.events;

import net.forthecrown.core.Cooldown;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.ComponentUtils;
import net.forthecrown.core.api.ShopInventory;
import net.forthecrown.core.api.SignShop;
import net.forthecrown.core.customevents.SignShopUseEvent;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class SignShopInteractEvent implements Listener {

    @EventHandler
    public void onSignShopUser(PlayerInteractEvent event){
        if(Cooldown.contains(event.getPlayer())) return;
        if(event.getClickedBlock() == null) return;
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if(!(event.getClickedBlock().getState() instanceof Sign)) return;

        Sign sign = (Sign) event.getClickedBlock().getState();

        String line0 = ComponentUtils.getString((TextComponent) sign.line(0));
        String line3 = ComponentUtils.getString((TextComponent) sign.line(3));

        if(!line0.contains("=[Buy]=")
                && !line0.contains("=[Sell]=")
                && !line0.contains("-[Sell]-")
                && !line0.contains("-[Buy]-")) return;
        if(!line3.contains("Price: ")) return;

        Cooldown.add(event.getPlayer(), 6);

        SignShop shop = FtcCore.getShop(event.getClickedBlock().getLocation());
        if(shop == null) return;

        Player player = event.getPlayer();

        sign.getBlock().getBlockData();

        //This does nothing rn, will be useful in the future
        if(!sign.getPersistentDataContainer().has(FtcCore.SHOP_KEY, PersistentDataType.STRING))
            sign.getPersistentDataContainer().set(FtcCore.SHOP_KEY, PersistentDataType.STRING, "SignShop");

        //checks if they're the owner and if they're sneaking, then opens the shop inventory to edit it
        if(player.isSneaking() && (shop.getOwner().equals(player.getUniqueId()) || player.hasPermission("ftc.admin"))){
            player.openInventory(shop.getInventory());
            FtcCore.getInstance().getServer().getPluginManager().registerEvents(new SignShopInteractSubClass(player, shop), FtcCore.getInstance());
            return;
        }

        FtcCore.getInstance().getServer().getPluginManager().callEvent(new SignShopUseEvent(shop, FtcCore.getUser(player), player, FtcCore.getBalances()));
    }

    public class SignShopInteractSubClass implements Listener {

        private final SignShop shop;
        private final Player player;

        public SignShopInteractSubClass(Player player, SignShop shop){
            this.player = player;
            this.shop = shop;
        }

        @EventHandler
        public void onInvClose(InventoryCloseEvent event) { //items added
            if(!event.getPlayer().equals(player)) return;

            Inventory inv = event.getInventory();
            ShopInventory shopInv = shop.getInventory();
            ItemStack[] contents = inv.getContents().clone();
            final ItemStack example = shopInv.getExampleItem();

            HandlerList.unregisterAll(this);

            shopInv.clear();

            for (ItemStack item : contents){
                if(item == null) continue;

                if(!item.getType().equals(example.getType())){
                    player.getInventory().addItem(item);
                    continue;
                }

                shopInv.addItem(item);
            }
            shopInv.performStockCheck();
        }
    }
}