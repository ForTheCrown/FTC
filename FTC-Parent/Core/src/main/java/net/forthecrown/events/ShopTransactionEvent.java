package net.forthecrown.events;

import net.forthecrown.core.*;
import net.forthecrown.core.chat.Announcer;
import net.forthecrown.core.chat.ChatFormatter;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.economy.Balances;
import net.forthecrown.economy.BrokenShopException;
import net.forthecrown.economy.shops.ShopInventory;
import net.forthecrown.economy.shops.ShopType;
import net.forthecrown.economy.shops.SignShop;
import net.forthecrown.events.custom.SignShopUseEvent;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.enums.Branch;
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

public class ShopTransactionEvent implements Listener, ExceptionedEvent<SignShopUseEvent> {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onShopUse(SignShopUseEvent event) throws CrownException {
        Events.handleSignShop(event, this);
    }

    private void sendInvMessage(CrownUser owner, SignShop shop){
        if(shop.getType().isAdmin) return;

        //If no good, then no go
        if ((shop.getType() != ShopType.BUY_SHOP || !shop.getInventory().isEmpty()) && (shop.getType() != ShopType.SELL_SHOP || !shop.getInventory().isFull()))
            return;

        Location l = shop.getLocation();
        Component specification = Component.translatable("shops." + (shop.getType().buyType ? "out" : "full"));
        Component builder = Component.translatable("shops.stockWarning", ChatFormatter.prettyLocationMessage(l, false), specification);

        owner.sendMessage(builder);
    }

    @Override
    public void execute(SignShopUseEvent event1){
        Events.handleSignShop(event1, event -> {
            Player player = event.getPlayer();
            CrownUser customer = event.getUser();
            CrownUser owner = event.getOwner();

            SignShop shop = event.getShop();
            ShopInventory shopInv = shop.getInventory();

            ItemStack example = shopInv.getExampleItem();

            if(example == null){
                shop.setOutOfStock(true);
                event.setCancelled(true);
                shop.update();
                throw new BrokenShopException(player);
            }

            //WorldGuard checks
            Branch allowedOwner = BranchFlag.queryFlag(shop.getLocation(), CrownWgFlags.SHOP_OWNERSHIP_FLAG);
            Branch allowedUser = BranchFlag.queryFlag(shop.getLocation(), CrownWgFlags.SHOP_USAGE_FLAG);
            if(allowedOwner != null && owner.getBranch() != Branch.DEFAULT && !shop.getType().isAdmin && allowedOwner != owner.getBranch()){
                event.setCancelled(true);
                throw CrownException.translatable(customer, "shops.wrongOwner", NamedTextColor.GRAY, Component.text(allowedOwner.getName()));
            }
            if(allowedUser != null && customer.getBranch() != Branch.DEFAULT && allowedUser != customer.getBranch()){
                event.setCancelled(true);
                throw CrownException.translatable(customer, "shops.wrongUser", NamedTextColor.GRAY, Component.text(allowedUser.getName()));
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
                        event.setCancelled(true);
                        throw FtcExceptionProvider.inventoryFull();
                    }
                    if(event.getCustomerBalance() < shop.getPrice()){
                        event.setCancelled(true);
                        throw FtcExceptionProvider.cannotAfford(shop.getPrice());
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
                            .append(customer.nickDisplayName().color(NamedTextColor.GOLD))
                            .append(Component.text(" bought "))
                            .append(ChatFormatter.itemMessage(shopInv.getExampleItem()).color(NamedTextColor.YELLOW))
                            .append(Component.text(" from you for "))
                            .append(Balances.formatted(shop.getPrice()).color(NamedTextColor.GOLD))
                            .build();

                    owner.sendMessage(ownerMessage);

                case ADMIN_BUY_SHOP: //This has some of the same if statements as the last case, but if they overlap, it has to happen, don't know how to do it better
                    if(!customerHasSpace){
                        event.setCancelled(true);
                        throw FtcExceptionProvider.inventoryFull();
                    }
                    if(event.getCustomerBalance() < shop.getPrice()){
                        event.setCancelled(true);
                        throw FtcExceptionProvider.cannotAfford(shop.getPrice());
                    }

                    Component boughtMessage = Component.text("You bought ")
                            .color(NamedTextColor.GRAY)
                            .append(ChatFormatter.itemMessage(shopInv.getExampleItem()).color(NamedTextColor.YELLOW))
                            .append(Component.text(" for "))
                            .append(Balances.formatted(shop.getPrice()).color(NamedTextColor.GOLD));

                    playerInv.addItem(example); //adds the item to player's inventory
                    event.setCustomerBalance(event.getCustomerBalance() - shop.getPrice());

                    if(shop.getType().isAdmin && CrownCore.logAdminShopUsage()){
                        Announcer.log(
                                Level.INFO,
                                customer.getName() + " bought " + example.getAmount() + " " + ChatFormatter.getItemNormalName(example) + " at an admin shop, location: " + shop.getName());
                    } else if(CrownCore.logNormalShopUsage()){
                        Announcer.log(Level.INFO,
                                customer.getName() + " bought " + example.getAmount() + " " + ChatFormatter.getItemNormalName(example) + " at a shop, location: " + shop.getName());
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
                            .append(ChatFormatter.itemMessage(shopInv.getExampleItem()).color(NamedTextColor.YELLOW))
                            .append(Component.text(" for "))
                            .append(Balances.formatted(shop.getPrice()).color(NamedTextColor.GOLD))
                            .build();

                    customer.sendMessage(customerMsg);

                    if(shop.getType() == ShopType.SELL_SHOP) {
                        Component ownerMsg = Component.text()
                                .color(NamedTextColor.GRAY)
                                .append(customer.nickDisplayName().color(NamedTextColor.GOLD))
                                .append(Component.text(" sold "))
                                .append(ChatFormatter.itemMessage(shopInv.getExampleItem()).color(NamedTextColor.YELLOW))
                                .append(Component.text(" to you for "))
                                .append(Balances.formatted(shop.getPrice()).color(NamedTextColor.GOLD))
                                .build();

                        event.setOwnerBalance(event.getOwnerBalance() - shop.getPrice());
                        shopInv.addItem(example);

                        event.getOwner().sendMessage(ownerMsg);
                    }

                    if(shop.getType().isAdmin && CrownCore.logAdminShopUsage()){
                        Announcer.log(Level.INFO,
                                customer.getName() + " sold " + example.getAmount() + " " + ChatFormatter.getItemNormalName(example) + " at an admin shop, location: " + shop.getName());
                    } else if(CrownCore.logNormalShopUsage()){
                        Announcer.log(Level.INFO,
                                customer.getName() + " sold " + example.getAmount() + " " + ChatFormatter.getItemNormalName(example) + " at a shop, location: " + shop.getName());
                    }

                    event.addCustomerBalance(shop.getPrice());
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + shop.getType());
            }

            shopInv.checkStock();
            sendInvMessage(owner, shop);
        });
    }
}