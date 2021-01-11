package net.forthecrown.core.economy.events;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.economy.Economy;
import net.forthecrown.core.economy.files.Balances;
import net.forthecrown.core.economy.files.SignShop;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SignShopInteractEvent implements Listener {

    private final Map<UUID, SignShop> dopfguijh = new HashMap<>();

    @EventHandler
    public void onSignShopUser(PlayerInteractEvent event){
        if(event.getClickedBlock() == null) return;
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if(!(event.getClickedBlock().getState() instanceof Sign)) return;

        Sign sign = (Sign) event.getClickedBlock().getState();
        if(!sign.getLine(0).contains("=[Buy]=") && !sign.getLine(0).contains("=[Sell]=")) return;
        if(!sign.getLine(3).contains("Price: ")) return;

        SignShop shop;
        try {
            shop = Economy.getSignShop(event.getClickedBlock().getLocation());
        } catch (NullPointerException e){
            e.printStackTrace();
            return;
        }
        Player player = event.getPlayer();

        if(player.isSneaking() && (shop.getOwner() == player.getUniqueId() || player.hasPermission("ftc.admin"))){
            player.openInventory(shop.getShopInventory());
            dopfguijh.put(player.getUniqueId(), shop);
            return;
        }

        if(shop.isOutOfStock()){
            player.sendMessage("This shop is out of stock");
            return;
        }

        Balances bals = Economy.getBalances();

        switch (shop.getType()){
            case ADMIN_SELL_SHOP:
                useAdminSell(player, bals,  shop);
                break;
            case SELL_SHOP:
                useSell(player, bals, shop);
                break;
            case ADMIN_BUY_SHOP:
                useAdminBuy(player, bals, shop);
                break;
            case BUY_SHOP:
                useBuy(player, bals, shop);
                break;
        }
    }

    @EventHandler
    public void onInvClose(InventoryCloseEvent event){
        if(event.getInventory().getType() != InventoryType.CHEST) return;
        if(!dopfguijh.containsKey(event.getPlayer().getUniqueId())) return;
        Player player = (Player) event.getPlayer();

        Inventory inv = event.getInventory();
        SignShop shop = dopfguijh.get(player.getUniqueId());
        dopfguijh.remove(player.getUniqueId());
        ItemStack[] invalidItems = shop.setItems(inv.getStorageContents());

        if(invalidItems != null && invalidItems.length > 0){
            for (ItemStack stack : invalidItems){
                if(player.getInventory().firstEmpty() == -1) player.getLocation().getWorld().dropItemNaturally(player.getLocation(), stack);
                else player.getInventory().setItem(player.getInventory().firstEmpty(), stack);
            }
            player.sendMessage(FtcCore.getPrefix() + ChatColor.GRAY + "Unable to add items to shop!");
        }
    }

    private void useAdminBuy(Player player, Balances bals, SignShop shop){
        boolean customerHasSpace = player.getInventory().firstEmpty() != -1;
        int customerBal = bals.getBalance(player.getUniqueId());

        if(!customerHasSpace){
            player.sendMessage("Your inventory is full!");
            return;
        }
        if(customerBal < shop.getPrice()){
            player.sendMessage("You poor");
            return;
        }

        player.getInventory().setItem(player.getInventory().firstEmpty(), shop.getExampleItem().clone());

        customerBal -= shop.getPrice();
        bals.setBalance(player.getUniqueId(), customerBal);
        player.sendMessage(ChatColor.GRAY + "You bought " + ChatColor.YELLOW + shop.getExampleItem().getAmount() + " " + shop.getExampleItem().getType().toString().replaceAll("_", " ").toLowerCase() + ChatColor.GRAY + " for " + shop.getPrice());
    }

    private void useAdminSell(Player player, Balances bals, SignShop shop){
        Inventory playerInv = player.getInventory();
        int customerBal = bals.getBalance(player.getUniqueId());
        int tempInt = shop.getExampleItem().getAmount();

        for(ItemStack stack : playerInv) {
            if(stack == null) continue;
            if(tempInt == 0) break;
            if(stack.getType() != shop.getExampleItem().getType()) continue;

            if (stack.getAmount() >= tempInt) {
                stack.setAmount(stack.getAmount() - tempInt);
                if(stack.getAmount() <= 0) playerInv.remove(stack);
                tempInt = 0;
                break;
            }

            if (stack.getAmount() < tempInt) {
                tempInt -= stack.getAmount();
                playerInv.remove(stack);
            }
        }
        if(tempInt != 0){
            player.sendMessage("You do not have enough items to sell");
            return;
        }

        customerBal += shop.getPrice();
        bals.setBalance(player.getUniqueId(), customerBal);
        player.sendMessage(ChatColor.GRAY + "You sold " + ChatColor.YELLOW + shop.getExampleItem().getAmount() + " " + shop.getExampleItem().getType().toString().replaceAll("_", " ").toLowerCase() + ChatColor.GRAY + " for " + shop.getPrice());
    }

    private void useBuy(Player player, Balances bals, SignShop shop){
        boolean hasSpace = player.getInventory().firstEmpty() != -1;

        int customerBal = bals.getBalance(player.getUniqueId());
        int ownerBal = bals.getBalance(shop.getOwner());
        int tempInt = shop.getExampleItem().getAmount();

        if(!hasSpace){
            player.sendMessage("Your inventory is full!");
            return;
        }
        if(customerBal < shop.getPrice()){
            player.sendMessage("You poor");
            return;
        }

        for(ItemStack stack : shop.getShopInventory()) {
            if(stack == null) continue;
            if(tempInt == 0) break;

            if (stack.getAmount() == tempInt) {
                shop.getShopInventory().remove(stack);
                tempInt = 0;
                break;
            }

            if (stack.getAmount() > tempInt) {
                stack.setAmount(stack.getAmount() - tempInt);
                tempInt = 0;
                break;
            }

            if (stack.getAmount() < tempInt) {
                tempInt -= stack.getAmount();
                shop.getShopInventory().remove(stack);
            }
        }
        if(tempInt != 0){
            player.sendMessage("You do not have enough items to sell");
            return;
        }

        if(isInvEmpty(shop.getShopInventory())){
            shop.setOutOfStock(true);
        }

        customerBal -= shop.getPrice();
        ownerBal += shop.getPrice();
        bals.setBalance(shop.getOwner(), ownerBal);
        bals.setBalance(player.getUniqueId(), customerBal);

        player.getInventory().setItem(player.getInventory().firstEmpty(), shop.getExampleItem().clone());
        player.sendMessage(ChatColor.GRAY + "You bought " + ChatColor.YELLOW + shop.getExampleItem().getAmount() + " " + shop.getExampleItem().getType().toString().replaceAll("_", " ").toLowerCase() + ChatColor.GRAY + " for " + shop.getPrice());
    }

    private void useSell(Player player, Balances bals, SignShop shop){
        boolean hasSpace = player.getInventory().firstEmpty() != -1;
        boolean shopHasSpace = shop.getShopInventory().firstEmpty() != -1;

        String prefix = FtcCore.getPrefix();
        Inventory playerInv = player.getInventory();

        int customerBal = bals.getBalance(player.getUniqueId());
        int ownerBal = bals.getBalance(shop.getOwner());
        int tempInt = shop.getExampleItem().getAmount();

        if(!shopHasSpace){
            player.sendMessage("The shop is full!");
            return;
        }
        if(ownerBal < shop.getPrice()){
            player.sendMessage("The shop owner is not able to afford this");
            return;
        }

        for(ItemStack stack : playerInv) {
            if(stack == null) continue;
            if(tempInt == 0) break;
            if(stack.getType() != shop.getExampleItem().getType()) continue;

            if (stack.getAmount() >= tempInt) {
                stack.setAmount(stack.getAmount() - tempInt);
                if(stack.getAmount() <= 0) playerInv.remove(stack);
                tempInt = 0;
                break;
            }

            if (stack.getAmount() < tempInt) {
                tempInt -= stack.getAmount();
                playerInv.remove(stack);
            }
        }
        if(tempInt != 0){
            player.sendMessage("You do not have enough items to sell");
            return;
        }

        customerBal += shop.getPrice();
        ownerBal -= shop.getPrice();
        bals.setBalance(shop.getOwner(), ownerBal);
        bals.setBalance(player.getUniqueId(), customerBal);

        shop.getShopInventory().setItem(shop.getShopInventory().firstEmpty(), shop.getExampleItem().clone());
        player.sendMessage(ChatColor.GRAY + "You sold " + ChatColor.YELLOW + shop.getExampleItem().getAmount() + " " + shop.getExampleItem().getType().toString().replaceAll("_", " ").toLowerCase() + ChatColor.GRAY + " for " + shop.getPrice());
    }

    private boolean isInvEmpty(Inventory inv){
        for(ItemStack stack : inv){
            if(stack != null) return false;
        }
        return true;
    }
}