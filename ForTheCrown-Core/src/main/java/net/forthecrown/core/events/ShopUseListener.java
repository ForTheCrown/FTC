package net.forthecrown.core.events;

import net.forthecrown.core.customevents.SignShopUseEvent;
import net.forthecrown.core.enums.ShopType;
import net.forthecrown.core.exceptions.InvalidCommandExecution;
import net.forthecrown.core.files.FtcUser;
import net.forthecrown.core.files.SignShop;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ShopUseListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onShopUse(SignShopUseEvent event){
        //just the variables
        Player player = event.getPlayer();
        SignShop shop = event.getShop();
        FtcUser customer = event.getCustomer();

        if(shop.getExampleItem() == null){
            if(shop.getShopInventory().getContents()[0] == null){
                shop.setOutOfStock(true);
                throw new InvalidCommandExecution(customer, "");
            } else shop.setExampleItem(shop.getShopInventory().getContents()[0]);
        }

        Inventory playerInv = player.getInventory();
        Inventory shopInv = shop.getShopInventory();

        boolean shopHasSpace = shop.getShopInventory().firstEmpty() != -1;
        boolean customerHasSpace = playerInv.firstEmpty() != -1;

        int tempInt = shop.getExampleItem().getAmount();

        //Some of these if statements overlap, but for a reason
        switch (shop.getType()){
            case BUY_SHOP:
                if(shop.isOutOfStock()){
                    customer.sendMessage("&7This shop is out of stock");
                    event.setCancelled(true);
                    return;
                }
                if(!customerHasSpace){
                    customer.sendMessage("&7Your inventory is full");
                    event.setCancelled(true);
                    return;
                }
                if(event.getCustomerBalance() < shop.getPrice()){
                    customer.sendMessage("&7You can't afford this!");
                    event.setCancelled(true);
                    return;
                }

                //this is basically the same thing in the SellShopEvents class, but worse lol
                for(ItemStack stack : shopInv){ //loops through the shop's inventory and removes the equivalent amount of items as dictated by the exampleItem in signShop
                    if(stack == null) continue;
                    if(stack.getType() != shop.getExampleItem().getType()) continue;
                    if(tempInt == 0) break;

                    if(stack.getAmount() >= tempInt){ //if the amount of the stack is greater than or equal to the tempInt
                        stack.setAmount(stack.getAmount() - tempInt); //lessen the stack
                        if(stack.getAmount() <= 0) shopInv.remove(stack); //remove it if the amount is 0
                        tempInt = 0;
                        break;
                    }

                    if(stack.getAmount() < tempInt){
                        tempInt -= stack.getAmount();
                        shopInv.remove(stack);
                    }
                }
                if(tempInt != 0){ //if there isn't enough items in the shop's inventory
                    customer.sendMessage("&7The Shop does not have enough stock");
                    shop.setOutOfStock(true);
                    event.setCancelled(true);
                    return;
                }
                if(isInvEmpty(shopInv)) shop.setOutOfStock(true); //if the inv is now empty, set it to be out of stock, dw setOutOfStock checks if the type is admin shop or not

                shop.setShopInventory(shopInv); //sets the changed inventory to be the shops inventory
                event.addOwnerBalance(shop.getPrice());

                try { //tries telling the owner someone bought from them, if they're online
                    final String longAF = ChatColor.GOLD + player.getName() +
                            ChatColor.GRAY + " bought " +
                            ChatColor.YELLOW + shop.getExampleItem().getAmount() + " " +
                            shop.getExampleItem().getType().toString().toLowerCase().replaceAll("_", " ") +
                            ChatColor.GRAY + " from you for " + ChatColor.GOLD + shop.getPrice() + " Rhines";

                    Bukkit.getPlayer(shop.getOwner()).sendMessage(longAF);
                } catch (Exception ignored){}

            case ADMIN_BUY_SHOP: //This has some of the same if statements as the last case, but if they overlap, it has to happen, don't know how to do it better
                if(!customerHasSpace){
                    customer.sendMessage("&7Your inventory is full");
                    event.setCancelled(true);
                    return;
                }
                if(event.getCustomerBalance() < shop.getPrice()){
                    customer.sendMessage("&7You can't afford this!");
                    event.setCancelled(true);
                    return;
                }

                playerInv.addItem(shop.getExampleItem()); //adds the item to player's inventory

                event.setCustomerBalance(event.getCustomerBalance() - shop.getPrice());

                String customerMsg1 = ChatColor.GRAY + "You bought " +
                        ChatColor.YELLOW + shop.getExampleItem().getAmount() + " " +
                        shop.getExampleItem().getType().toString().toLowerCase().replaceAll("_", " ") +
                        ChatColor.GRAY + " for " + ChatColor.GOLD + shop.getPrice() + " Rhines";

                player.sendMessage(customerMsg1);

                break;

            case SELL_SHOP: //again some overlap
                if(shop.isOutOfStock()){
                    customer.sendMessage("&cThis shop is currently unusable. &7Please tell the owner to remake the shop");
                    return;
                }

                if(event.getOwnerBalance() < shop.getPrice()){
                    customer.sendMessage("&7The owner of this shop is not able to afford this");
                    event.setCancelled(true);
                    return;
                }
                if(!shopHasSpace){
                    customer.sendMessage("&7This shop is full!");
                    event.setCancelled(true);
                    return;
                }

            case ADMIN_SELL_SHOP:

                ItemStack[] playerContents = playerInv.getContents();
                List<ItemStack> toRemove = new ArrayList<>(); //honestly, this was the only way I could do this without modifying the players inv in realtime and then seeing they didn't have enough items
                for(ItemStack stack : playerContents){
                    if(stack == null) continue;
                    if(stack.getType() != shop.getExampleItem().getType()) continue;
                    if(!stack.getItemMeta().getDisplayName().contains(shop.getExampleItem().getItemMeta().getDisplayName())) continue;

                    if(stack.getAmount() >= tempInt){
                        stack.setAmount(stack.getAmount() - tempInt);
                        if(stack.getAmount() <= 0) toRemove.add(stack);
                        tempInt = 0;
                        break;
                    }

                    if(stack.getAmount() <= tempInt){
                        tempInt -= stack.getAmount();
                        toRemove.add(stack);
                    }
                }
                if(tempInt != 0){
                    customer.sendMessage("&7You don't have enough items in your inventory!");
                    event.setCancelled(true);
                    return;
                }

                for(ItemStack stack : toRemove){
                    playerInv.remove(stack);
                }

                final String customerMsg = ChatColor.GRAY + "You sold " +
                        ChatColor.YELLOW + shop.getExampleItem().getAmount() + " " +
                        shop.getExampleItem().getType().toString().toLowerCase().replaceAll("_", " ") +
                        ChatColor.GRAY + " for " + ChatColor.GOLD + shop.getPrice() + " Rhines";

                player.sendMessage(customerMsg);

                if(shop.getType() == ShopType.SELL_SHOP) {
                    event.setOwnerBalance(event.getOwnerBalance() - shop.getPrice());

                    Inventory inv = shop.getShopInventory();
                    inv.addItem(shop.getExampleItem());
                    shop.setShopInventory(inv);

                    try {
                        final String loooonnng = ChatColor.GOLD + player.getName() + ChatColor.GRAY +
                                " sold " + ChatColor.YELLOW + shop.getExampleItem().getAmount() + " " + shop.getExampleItem().getType().toString().replaceAll("_", " ").toLowerCase() +
                                ChatColor.GRAY + " to you for " + ChatColor.GOLD + shop.getPrice() + " Rhines";

                        Bukkit.getPlayer(shop.getOwner()).sendMessage(loooonnng);
                    } catch (Exception ignored){}
                }

                event.addCustomerBalance(shop.getPrice());
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + shop.getType());
        }
    }

    private boolean isInvEmpty(Inventory inv){
        for(ItemStack stack : inv){
            if(stack != null) return false;
        }
        return true;
    }
}
