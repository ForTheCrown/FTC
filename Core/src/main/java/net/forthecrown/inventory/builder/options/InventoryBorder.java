package net.forthecrown.inventory.builder.options;

import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.user.CrownUser;
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
        borderItem = new ItemStackBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .setName(Component.space())
                .build();
    }

    @Override
    public void onClick(CrownUser user, ClickContext context) throws RoyalCommandException {
        context.setReloadInventory(false);
    }

    @Override
    public void place(Inventory inventory, CrownUser user) {
        for (int i = 0; i < inventory.getSize(); i++){
            if(i < 9){
                inventory.setItem(i, getBorderItem());
                continue;
            }

            if(i >= inventory.getSize() - 9){
                inventory.setItem(i, getBorderItem());
                continue;
            }

            if(i % 9 != 0) return;
            inventory.setItem(i, getBorderItem());
            i += 8;
            inventory.setItem(i, getBorderItem());
        }
    }

    public ItemStack getBorderItem() {
        return borderItem;
    }

    @Override
    public OptionPriority getPriority() {
        return OptionPriority.LOW;
    }

    @Override
    public int getSlot() {
        return -1;
    }
}
