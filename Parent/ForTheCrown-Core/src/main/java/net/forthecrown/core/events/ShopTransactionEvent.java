package net.forthecrown.core.events;

import net.forthecrown.core.BranchFlag;
import net.forthecrown.core.CrownWorldGuard;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.*;
import net.forthecrown.core.events.customevents.SignShopUseEvent;
import net.forthecrown.core.enums.Branch;
import net.forthecrown.core.enums.ShopType;
import net.forthecrown.core.exceptions.BrokenShopException;
import net.forthecrown.core.exceptions.CrownException;
import net.forthecrown.core.utils.CrownUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Level;

public class ShopTransactionEvent implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onShopUse(SignShopUseEvent event) throws CrownException {
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

                Component ownerMessage = Component.text()
                        .color(NamedTextColor.GRAY)
                        .append(customer.name()
                                .color(NamedTextColor.GOLD)
                                .hoverEvent(customer)
                                .clickEvent(customer.asClickEvent())
                        )
                        .append(Component.text(" bought "))
                        .append(Component.text(example.getAmount() + " " + CrownUtils.getItemNormalName(example)).color(NamedTextColor.YELLOW))
                        .append(Component.text(" from you for "))
                        .append(Balances.formatted(shop.getPrice()).color(NamedTextColor.GOLD))
                        .build();

                owner.sendMessage(ownerMessage);

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

                Component boughtMessage = Component.text("You bought ")
                        .color(NamedTextColor.GRAY)
                        .append(Component.text(example.getAmount() + " " + CrownUtils.getItemNormalName(example)).color(NamedTextColor.YELLOW))
                        .append(Component.text(" for "))
                        .append(Balances.formatted(shop.getPrice()).color(NamedTextColor.GOLD));

                playerInv.addItem(example); //adds the item to player's inventory
                event.setCustomerBalance(event.getCustomerBalance() - shop.getPrice());

                if(shop.getType().isAdmin() && FtcCore.logAdminShopUsage()){
                    Announcer.log(Level.INFO,
                            customer.getName() + " bought " + example.getAmount() + " " + CrownUtils.getItemNormalName(example) + " at an admin shop, location: " + shop.getName());
                } else if(FtcCore.logNormalShopUsage()){
                    Announcer.log(Level.INFO,
                            customer.getName() + " bought " + example.getAmount() + " " + CrownUtils.getItemNormalName(example) + " at a shop, location: " + shop.getName());
                }

                customer.sendMessage(boughtMessage);
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

                Component customerMsg = Component.text()
                        .color(NamedTextColor.GRAY)
                        .append(Component.text("You sold "))
                        .append(Component.text(example.getAmount() + " " + CrownUtils.getItemNormalName(example)).color(NamedTextColor.YELLOW))
                        .append(Component.text(" for "))
                        .append(Balances.formatted(shop.getPrice()).color(NamedTextColor.GOLD))
                        .build();

                customer.sendMessage(customerMsg);

                if(shop.getType() == ShopType.SELL_SHOP) {
                    Component ownerMsg = Component.text()
                            .color(NamedTextColor.GRAY)
                            .append(customer.name()
                                    .color(NamedTextColor.GOLD)
                                    .hoverEvent(customer)
                                    .clickEvent(customer.asClickEvent())
                            )
                            .append(Component.text(" sold "))
                            .append(Component.text(example.getAmount() + " " + CrownUtils.getItemNormalName(example)))
                            .append(Component.text(" to you for "))
                            .append(Balances.formatted(shop.getPrice()).color(NamedTextColor.GOLD))
                            .build();

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
        sendInvMessage(owner, shop);
    }

    private void sendInvMessage(CrownUser owner, SignShop shop){
        if(shop.getType().isAdmin()) return;

        //If no good, then no go
        if ((shop.getType() != ShopType.BUY_SHOP || !shop.getInventory().isEmpty()) && (shop.getType() != ShopType.SELL_SHOP || !shop.getInventory().isFull()))
            return;

        Location l = shop.getLocation();
        Component builder = Component.text("Your shop at ")
                .color(NamedTextColor.GRAY)
                .append(CrownUtils.prettyLocationMessage(l, false).color(NamedTextColor.YELLOW))
                .append(Component.text(shop.getType() == ShopType.BUY_SHOP ? " is out of stock" : " is full"));

        owner.sendMessage(builder);
    }
}