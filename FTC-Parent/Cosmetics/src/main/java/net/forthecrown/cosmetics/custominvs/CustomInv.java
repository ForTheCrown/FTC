package net.forthecrown.cosmetics.custominvs;

import net.forthecrown.core.user.CrownUser;
import net.forthecrown.cosmetics.custominvs.borders.Border;
import net.forthecrown.cosmetics.custominvs.options.Option;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class CustomInv implements InventoryHolder {

    private CrownUser userWhoIsInInv;
    private Inventory inv;
    private Border invBorder;
    private Map<Integer, Option> invSlots;
    public final static Listener listener = new InvClickListener();

    public CustomInv() {}

    void setUser(CrownUser user) { this.userWhoIsInInv = user; }
    void setInv(@NotNull Inventory inv) { this.inv = inv; }
    void setInvBorder(Border invBorder) { this.invBorder = invBorder; }
    void setInvSlots(Map<Integer, Option> slots) { this.invSlots = slots; }

    public CrownUser getUser() { return this.userWhoIsInInv; }

    public final int getSize() { return this.inv.getSize(); }
    public final Inventory getInventory() {
        return this.inv;
    }


    public void handleClick(HumanEntity clicker, int slot) {
        if (invBorder.isOnBorder(slot)) invBorder.handleClick(clicker);
        else if (invSlots.containsKey(slot)) invSlots.get(slot).handleClick(clicker);
    }

    public void updateOption(int slot, Option newOption) {
        if (invSlots.containsKey(slot)) {
            invSlots.replace(slot, newOption);
            inv.setItem(slot, newOption.getItem());
        }
    }

}
