package net.forthecrown.economy.shops;

import net.forthecrown.economy.BalanceHolder;
import net.forthecrown.utils.Nameable;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

/**
 * The customer of a single shop
 */
public interface ShopCustomer extends Audience, BalanceHolder, InventoryHolder, Nameable {
    /**
     * Gets the unique ID of the customer
     * @return The Customer's UUID
     */
    UUID getUniqueId();

    /**
     * Gets the display name to use for shop messages
     * @return The shop display name
     */
    Component shopDisplayName();
}