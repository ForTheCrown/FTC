package net.forthecrown.economy.shops;

import net.forthecrown.economy.BalanceHolder;
import net.forthecrown.utils.Nameable;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

public interface ShopCustomer extends Audience, BalanceHolder, InventoryHolder, Nameable {
    UUID getUniqueId();

    Component shopDisplayName();
}
