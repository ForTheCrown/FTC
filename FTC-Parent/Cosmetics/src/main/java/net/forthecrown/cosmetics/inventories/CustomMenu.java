package net.forthecrown.cosmetics.inventories;

import net.forthecrown.core.inventory.CrownItems;
import net.forthecrown.core.user.CrownUser;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

/**
 * Final methods can not be overridden.
 * Abstract methods have to be overridden.
 * Normal methods can be overridden.
 */
public abstract class CustomMenu implements InventoryHolder {
    private Inventory inv;
    private CrownUser user;

    public final Inventory getBaseInventory() {
        int size = getSize();
        Inventory result = Bukkit.createInventory(this, size, getInventoryTitle());

        ItemStack glass = getGlassFiller();

        for (int i = 0; i < size; i += 9) {
            result.setItem(i, glass);
        }
        for (int i = 8; i < size; i += 9) {
            result.setItem(i, glass);
        }
        for (int i = 1; i < 8; i++) {
            result.setItem(i, glass);
        }
        for (int i = size-8; i <= size-2; i++) {
            result.setItem(i, glass);
        }

        result.setItem(getHeadItemSlot(), getHeadItem());
        result.setItem(getReturnItemSlot(), getReturnItem());
        return result;
    }

    public final ItemStack getGlassFiller() {
        return CrownItems.makeItem(Material.GRAY_STAINED_GLASS_PANE, 1, true, " ");
    }

    public final CrownUser getUser() { return this.user; }

    public final void setUser(CrownUser user) { this.user = user; }

    public final Inventory getInventory() { return this.inv; }

    public final void setInv(Inventory inv) { this.inv = inv; }



    abstract TextComponent getInventoryTitle();

    abstract int getSize();

    abstract Inventory makeInventory();



    public ItemStack getHeadItem() {
        return getGlassFiller();
    }

    public ItemStack getReturnItem() {
        return getGlassFiller();
    }

    public int getHeadItemSlot() {
        return 4;
    }

    public int getReturnItemSlot() {
        return 0;
    }

}
