package net.forthecrown.economy.selling;

import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.inventory.builder.options.InventoryOption;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.enums.SellAmount;
import net.forthecrown.utils.ItemStackBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;

public class SellAmountItem implements InventoryOption {

    private final SellAmount sellAmount;
    private final int slot;

    public SellAmountItem(SellAmount sellAmount, int slot) {
        this.sellAmount = sellAmount;
        this.slot = slot;
    }

    public SellAmount getSellAmount() {
        return sellAmount;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public void place(Inventory inventory, CrownUser user) {
        ItemStackBuilder builder = new ItemStackBuilder(Material.BLACK_STAINED_GLASS_PANE, sellAmount.value)
                .setName(Component.text(sellAmount.text).style(FtcFormatter.nonItalic(NamedTextColor.WHITE)))
                .addLore(Component.text("Set the amount of items you").style(FtcFormatter.nonItalic(NamedTextColor.GRAY)))
                .addLore(Component.text("will sell per click").style(FtcFormatter.nonItalic(NamedTextColor.GRAY)))
                .setFlags(ItemFlag.HIDE_ENCHANTS);

        if(sellAmount == user.getSellAmount()) builder.addEnchant(Enchantment.CHANNELING, 1);

        inventory.setItem(getSlot(), builder.build());
    }

    @Override
    public void onClick(CrownUser user, ClickContext context) throws RoyalCommandException {
        if(user.getSellAmount() == sellAmount) return;

        user.setSellAmount(sellAmount);
        context.setReloadInventory(true);
    }
}
