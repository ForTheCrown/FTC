package net.forthecrown.vikings;

import net.forthecrown.core.CrownUtils;
import net.forthecrown.core.inventories.CustomInventoryHolder;
import net.forthecrown.vikings.raids.VikingRaid;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class VikingUtils {

    public static Inventory getRaidSelector(){
        CustomInventoryHolder holder = new CustomInventoryHolder("Raid Selection", 27);
        Inventory result = holder.getInventory();

        final ItemStack border = CrownUtils.makeItem(Material.GRAY_STAINED_GLASS_PANE, 1, true, "");
        for (int i = 0; i < 27; i++){
            if(i == 10) i += 7;
            result.setItem(i, border);
        }

        int i = 0;
        for (VikingRaid r : Vikings.getRaidHandler().getRaids()){
            result.setItem(i + 10, CrownUtils.makeItem(Material.BARRIER, 1, true, r.getName()));
            i++;
        }

        return result;
    }
}
