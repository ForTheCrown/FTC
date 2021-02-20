package net.forthecrown.core.events;

import net.forthecrown.core.CrownUtils;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.api.SignShop;
import net.forthecrown.core.customevents.SignShopUseEvent;
import net.forthecrown.core.enums.ShopType;
import net.forthecrown.core.exceptions.BrokenShopException;
import net.forthecrown.core.exceptions.CrownException;
import net.md_5.bungee.api.ChatColor;
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
        CrownUser customer = event.getCustomer();
        ItemStack example = shop.getStock().getExampleItem();

        if(example == null){
            shop.setOutOfStock(true);
            throw new BrokenShopException(player);
        }

        Inventory playerInv = player.getInventory();

        boolean shopHasSpace = shop.getShopInventory().firstEmpty() != -1;
        boolean customerHasSpace = playerInv.firstEmpty() != -1;

        int tempInt = example.getAmount();

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

                if(shop.getStock().containsExampleItem())shop.getStock().removeExampleItemAmount();
                else{
                    shop.setOutOfStock(true);
                    throw new CrownException(player, "&7Shop does not have enough stock");
                }

                if(shop.getStock().isEmpty()) shop.setOutOfStock(true);
                //shop.setShopInventory(shopInv); //sets the changed inventory to be the shops inventory
                event.addOwnerBalance(shop.getPrice());

                final String longAF = ChatColor.GOLD + player.getName() +
                        ChatColor.GRAY + " bought " +
                        ChatColor.YELLOW + example.getAmount() + " " +
                        CrownUtils.capitalizeWords(example.getType().toString().toLowerCase().replaceAll("_", " ")) +
                        ChatColor.GRAY + " from you for " + ChatColor.GOLD + shop.getPrice() + " Rhines";

                event.getOwner().sendMessage(longAF);

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

                playerInv.addItem(example.clone()); //adds the item to player's inventory

                event.setCustomerBalance(event.getCustomerBalance() - shop.getPrice());

                String customerMsg1 = ChatColor.GRAY + "You bought " +
                        ChatColor.YELLOW + example.getAmount() + " " +
                        CrownUtils.capitalizeWords(example.getType().toString().toLowerCase().replaceAll("_", " ")) +
                        ChatColor.GRAY + " for " + ChatColor.GOLD + shop.getPrice() + " Rhines";

                customer.sendMessage(customerMsg1);

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
                    if(!stack.isSimilar(example)) continue;
                    if(!stack.getItemMeta().getDisplayName().contains(example.getItemMeta().getDisplayName())) continue;

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
                    playerInv.removeItem(stack);
                }

                final String customerMsg = ChatColor.GRAY + "You sold " +
                        ChatColor.YELLOW + example.getAmount() + " " +
                        example.getType().toString().toLowerCase().replaceAll("_", " ") +
                        ChatColor.GRAY + " for " + ChatColor.GOLD + shop.getPrice() + " Rhines";

                customer.sendMessage(customerMsg);

                if(shop.getType() == ShopType.SELL_SHOP) {
                    event.setOwnerBalance(event.getOwnerBalance() - shop.getPrice());
                    shop.getStock().add(example.clone());

                    final String loooonnng = ChatColor.GOLD + player.getName() + ChatColor.GRAY +
                            " sold " + ChatColor.YELLOW + example.getAmount() + " " +
                            example.getType().toString().replaceAll("_", " ").toLowerCase() +
                            ChatColor.GRAY + " to you for " + ChatColor.GOLD + shop.getPrice() + " Rhines";

                    event.getOwner().sendMessage(loooonnng);
                }

                event.addCustomerBalance(shop.getPrice());
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + shop.getType());
        }
    }
}

//this is basically the same thing in the SellShopEvents class, but worse lol
                /*
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
                }*/