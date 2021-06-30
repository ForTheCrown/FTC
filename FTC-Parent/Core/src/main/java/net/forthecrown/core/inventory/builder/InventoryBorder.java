package net.forthecrown.core.inventory.builder;

import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.CrownUtils;
import net.forthecrown.utils.ItemStackBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryBorder implements InventoryOption {

    private final ItemStack borderItem;

    public InventoryBorder(ItemStack borderItem) {
        this.borderItem = borderItem;
    }

    public InventoryBorder(){
        this.borderItem = new ItemStackBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .setName(Component.space())
                .build();
    }

    @Override
    public void run(CrownUser user, ClickContext context) throws RoyalCommandException {
    }

    @Override
    public void place(Inventory inventory, CrownUser user) {
        for (int i = 0; i < inventory.getSize(); i++){
            if(!CrownUtils.isInRange(i, 9, inventory.getSize() - 9)){ //Top and bottom
                inventory.setItem(i, getBorderItem());
                continue;
            }

            if(i % 9 != 0) continue;

            inventory.setItem(i, getBorderItem());
            i += 8;
            inventory.setItem(i, getBorderItem());
        }
    }

    public ItemStack getBorderItem() {
        return borderItem;
    }

    @Override
    public int getSlot() {
        return -1;
    }
}
