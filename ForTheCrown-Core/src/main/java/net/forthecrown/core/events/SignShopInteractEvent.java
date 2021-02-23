package net.forthecrown.core.events;

import net.forthecrown.core.Cooldown;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.api.SignShop;
import net.forthecrown.core.customevents.SignShopUseEvent;
import net.md_5.bungee.api.ChatColor;
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

public class SignShopInteractEvent implements Listener {

    @EventHandler
    public void onSignShopUser(PlayerInteractEvent event){
        if(Cooldown.contains(event.getPlayer())) return;
        if(event.getClickedBlock() == null) return;
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if(!(event.getClickedBlock().getState() instanceof Sign)) return;

        Sign sign = (Sign) event.getClickedBlock().getState();
        if(!sign.getLine(0).contains("=[Buy]=") && !sign.getLine(0).contains("=[Sell]=") && !sign.getLine(0).contains("-[Sell]-") && !sign.getLine(0).contains("-[Buy]-")) return;
        if(!sign.getLine(3).contains(ChatColor.DARK_GRAY + "Price: ")) return;

        Cooldown.add(event.getPlayer(), 6);

        SignShop shop;
        try {
            shop = FtcCore.getShop(event.getClickedBlock().getLocation());
        } catch (Exception e){
            e.printStackTrace();
            return;
        }
        Player player = event.getPlayer();

        //checks if they're the owner and if they're sneaking, then opens the shop inventory to edit it

        if(player.isSneaking() && (shop.getOwner().equals(player.getUniqueId()) || player.hasPermission("ftc.admin"))){
            player.openInventory(shop.getInventory());
            FtcCore.getInstance().getServer().getPluginManager().registerEvents(new SignShopInteractSubClass(player, shop), FtcCore.getInstance());
            return;
        }

        Balances bals = FtcCore.getBalances();
        CrownUser user = FtcCore.getUser(player.getUniqueId());

        FtcCore.getInstance().getServer().getPluginManager().callEvent(new SignShopUseEvent(shop, user, player, bals));
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
            ItemStack[] contents = inv.getContents().clone();
            final ItemStack example = shop.getInventory().getExampleItem();

            HandlerList.unregisterAll(this);

            shop.getInventory().clear();

            for (ItemStack item : contents){
                if(item == null) continue;

                if(!item.getType().equals(example.getType())){
                    player.getInventory().addItem(item);
                    continue;
                }

                shop.getInventory().addItem(item);
            }
        }
    }
}