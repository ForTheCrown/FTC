package net.forthecrown.events.dynamic;

import net.forthecrown.core.Crown;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.economy.Economy;
import net.forthecrown.economy.pirates.BlackMarketUtils;
import net.forthecrown.economy.pirates.merchants.MaterialMerchant;
import net.forthecrown.pirates.Pirates;
import net.forthecrown.user.data.SellAmount;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.UUID;

public class BmSellItemListener extends AbstractInvListener implements Listener {

    private final MaterialMerchant merchant;
    public BmSellItemListener(Player player, MaterialMerchant merchant) {
        super(player);
        this.merchant = merchant;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if(!event.getWhoClicked().equals(player)) return;

        if(event.isShiftClick()) event.setCancelled(true);
        if(event.getClickedInventory() instanceof PlayerInventory) return;

        event.setCancelled(true);

        ItemStack item = event.getCurrentItem();
        if(BlackMarketUtils.isInvalidItem(item, event.getView())) return;

        Material toSell = item.getType();

        PlayerInventory playerInventory = player.getInventory();

        int sellAmount = user.getSellAmount().getValue();
        int finalSell = sellAmount;

        ItemStack toSellItem = new ItemStack(toSell, finalSell);

        if(!playerInventory.containsAtLeast(toSellItem, sellAmount)){
            user.sendMessage(Component.translatable("economy.sellshop.noItems", NamedTextColor.GRAY));
            return;
        }

        if(user.getSellAmount() == SellAmount.ALL){
            finalSell = 0;
            for (ItemStack i: playerInventory){
                if(i == null) continue;
                if(i.getType() != toSell) continue;

                finalSell += i.getAmount();
                playerInventory.removeItemAnySlot(i);
            }
        } else playerInventory.removeItemAnySlot(toSellItem);

        UUID uuid = player.getUniqueId();
        int toPay = merchant.getItemPrice(toSell) * finalSell;

        int newEarned = toPay + merchant.getEarned(toSell);
        if(newEarned > Pirates.getPirateEconomy().getMaxEarnings()){
            int difference = newEarned - Pirates.getPirateEconomy().getMaxEarnings();
            toPay = toPay - difference;
        }
        merchant.setEarned(toSell, newEarned);

        Component s = FtcFormatter.itemDisplayName(toSellItem);

        Economy bals = Crown.getEconomy();
        bals.add(uuid, toPay, false);

        Component message = Component.text("sold ")
                .color(NamedTextColor.GRAY)
                .append(Component.text(finalSell).color(NamedTextColor.YELLOW))
                .append(Component.text(" "))
                .append(s.color(NamedTextColor.YELLOW))
                .append(Component.text(" for "))
                .append(FtcFormatter.rhines(toPay).color(NamedTextColor.GOLD));

        user.sendMessage(
                Component.text("You ")
                        .color(NamedTextColor.GRAY)
                        .append(message)
        );

        Bukkit.getConsoleSender().sendMessage(
                Component.text()
                        .append(user.name())
                        .append(Component.space())
                        .append(message)
                        .build()
        );

        player.openInventory(merchant.createInventory(user));
    }
}
