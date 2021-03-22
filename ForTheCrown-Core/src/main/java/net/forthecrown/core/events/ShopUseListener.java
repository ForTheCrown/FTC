package net.forthecrown.core.events;

import net.forthecrown.core.BranchFlag;
import net.forthecrown.core.utils.CrownUtils;
import net.forthecrown.core.CrownWorldGuard;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Announcer;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.api.ShopInventory;
import net.forthecrown.core.api.SignShop;
import net.forthecrown.core.customevents.SignShopUseEvent;
import net.forthecrown.core.enums.Branch;
import net.forthecrown.core.enums.ShopType;
import net.forthecrown.core.exceptions.BrokenShopException;
import net.forthecrown.core.exceptions.CrownException;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Level;

public class ShopUseListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onShopUse(SignShopUseEvent event){
        //just the variables
        Player player = event.getPlayer();
        CrownUser customer = event.getCustomer();
        CrownUser owner = event.getOwner();

        SignShop shop = event.getShop();
        ShopInventory shopInv = shop.getInventory();

        ItemStack example = shopInv.getExampleItem();

        if(example == null){
            shop.setOutOfStock(true);
            event.setCancelled(true);
            throw new BrokenShopException(player);
        }

        //WorldGuard checks
        Branch allowedOwner = BranchFlag.queryFlag(shop.getLocation(), CrownWorldGuard.SHOP_OWNERSHIP_FLAG);
        Branch allowedUser = BranchFlag.queryFlag(shop.getLocation(), CrownWorldGuard.SHOP_USAGE_FLAG);
        if(allowedOwner != null && owner.getBranch() != Branch.DEFAULT && shop.getType() != ShopType.ADMIN_BUY_SHOP && shop.getType() != ShopType.ADMIN_SELL_SHOP && allowedOwner != owner.getBranch()){
            event.setCancelled(true);
            throw new CrownException(customer, "&7The owner of this shop is not allowed to operate here! (" + allowedOwner.getName() + " only)");
        }
        if(allowedUser != null && customer.getBranch() != Branch.DEFAULT && allowedUser != customer.getBranch()){
            event.setCancelled(true);
            throw new CrownException(customer, "&7Only " + allowedUser.getName() + " are allowed to use shops here!");
        }

        Inventory playerInv = player.getInventory();

        boolean shopHasSpace = !shopInv.isFull();
        boolean customerHasSpace = playerInv.firstEmpty() != -1;

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

                if(shopInv.containsAtLeast(example, example.getAmount())) shopInv.removeItem(example);
                else{
                    shop.setOutOfStock(true);
                    event.setCancelled(true);
                    throw new CrownException(player, "&7This shop is out of stock");
                }

                event.addOwnerBalance(shop.getPrice());

                final String longAF = ChatColor.GOLD + player.getName() +
                        ChatColor.GRAY + " bought " +
                        ChatColor.YELLOW + example.getAmount() + " " +
                        CrownUtils.getItemNormalName(example) +
                        ChatColor.GRAY + " from you for " + ChatColor.GOLD + CrownUtils.decimalizeNumber(shop.getPrice()) + " Rhines";

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

                String customerMsg1 = ChatColor.GRAY + "You bought " +
                        ChatColor.YELLOW + example.getAmount() + " " +
                        CrownUtils.getItemNormalName(example) +
                        ChatColor.GRAY + " for " + ChatColor.GOLD + CrownUtils.decimalizeNumber(shop.getPrice()) + " Rhines";

                playerInv.addItem(example); //adds the item to player's inventory
                event.setCustomerBalance(event.getCustomerBalance() - shop.getPrice());

                if(shop.getType().isAdmin() && FtcCore.logAdminShopUsage()){
                    Announcer.log(Level.INFO,
                            customer.getName() + " bought " + example.getAmount() + " " + CrownUtils.getItemNormalName(example) + " at an admin shop, location: " + shop.getName());
                } else if(FtcCore.logNormalShopUsage()){
                    Announcer.log(Level.INFO,
                            customer.getName() + " bought " + example.getAmount() + " " + CrownUtils.getItemNormalName(example) + " at a shop, location: " + shop.getName());
                }

                customer.sendMessage(customerMsg1);
                break;

            case SELL_SHOP: //again some overlap
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

                if(playerInv.containsAtLeast(example, example.getAmount())) playerInv.removeItem(example);
                else {
                    customer.sendMessage("&7You don't have enough items in your inventory!");
                    event.setCancelled(true);
                    return;
                }

                final String customerMsg = ChatColor.GRAY + "You sold " +
                        ChatColor.YELLOW + example.getAmount() + " " +
                        CrownUtils.getItemNormalName(example) +
                        ChatColor.GRAY + " for " + ChatColor.GOLD + CrownUtils.decimalizeNumber(shop.getPrice()) + " Rhines";

                customer.sendMessage(customerMsg);

                if(shop.getType() == ShopType.SELL_SHOP) {

                    final String ownerMsg = ChatColor.GOLD + player.getName() + ChatColor.GRAY +
                            " sold " + ChatColor.YELLOW + example.getAmount() + " " +
                            CrownUtils.getItemNormalName(example) +
                            ChatColor.GRAY + " to you for " + ChatColor.GOLD + CrownUtils.decimalizeNumber(shop.getPrice()) + " Rhines";

                    event.setOwnerBalance(event.getOwnerBalance() - shop.getPrice());
                    shopInv.addItem(example);

                    event.getOwner().sendMessage(ownerMsg);
                }

                if(shop.getType().isAdmin() && FtcCore.logAdminShopUsage()){
                    Announcer.log(Level.INFO,
                            customer.getName() + " sold " + example.getAmount() + " " + CrownUtils.getItemNormalName(example) + " at an admin shop, location: " + shop.getName());
                } else if(FtcCore.logNormalShopUsage()){
                    Announcer.log(Level.INFO,
                            customer.getName() + " sold " + example.getAmount() + " " + CrownUtils.getItemNormalName(example) + " at a shop, location: " + shop.getName());
                }

                event.addCustomerBalance(shop.getPrice());
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + shop.getType());
        }
        shopInv.checkStock();
    }
}