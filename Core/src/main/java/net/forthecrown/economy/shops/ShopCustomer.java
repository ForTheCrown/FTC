package net.forthecrown.economy.shops;

import net.kyori.adventure.audience.Audience;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

public interface ShopCustomer extends Audience {
    Inventory getInventory();

    UUID getUniqueId();
}
