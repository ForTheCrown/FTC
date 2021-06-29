package net.forthecrown.cosmetics.inventories;

import net.forthecrown.core.user.CrownUser;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * Final methods can not be overridden.
 * Abstract methods have to be overridden.
 * Normal methods can be overridden.
 */
public abstract class CustomMenu implements InventoryHolder {

    private Inventory inv;
    private CrownUser user;

    public final CrownUser getUser() { return this.user; }

    public final void setUser(CrownUser user) { this.user = user; }

    public final Inventory getInventory() { return this.inv; }

    public final void setInv(Inventory inv) { this.inv = inv; }



    abstract TextComponent getInventoryTitle();

    abstract int getSize();

    /**
     * In this method, you can create a CustomInventory object using
     * the CustomInventoryBuilder class
     * @return the result of the builder.
     */
    abstract Inventory buildInventory();


}
