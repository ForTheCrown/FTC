package net.forthecrown.economy.pirates;

import net.forthecrown.inventory.CustomInventoryHolder;
import net.forthecrown.inventory.FtcItems;
import net.forthecrown.utils.FtcUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Score;

/**
 * BlackMarketUtils, not API
 */
public final class BlackMarketUtils {
    private BlackMarketUtils() {}

    private static final ItemStack borderItem = FtcItems.makeItem(Material.GRAY_STAINED_GLASS_PANE, 1, true, "&7-");

    public static Inventory getBaseInventory(String name, ItemStack header){
        CustomInventoryHolder holder = new CustomInventoryHolder(name, InventoryType.CHEST);
        Inventory base = holder.getInventory();

        base.setItem(4, header);

        for (int i = 0; i < 27; i++){
            if(i == 4) i++;
            if(i == 10) i += 7;

            base.setItem(i, borderItem);
        }

        return base;
    }

    public static Score getPiratePointScore(String name){
        return Bukkit.getScoreboardManager().getMainScoreboard().getObjective("PiratePoints").getScore(name);
    }

    //True if the item in a border item, the inventory header or null
    public static boolean isInvalidItem(ItemStack item, InventoryView header){
        return FtcUtils.isItemEmpty(item)
                || item.getType() == borderItem.getType()
                || item.equals(header.getTopInventory().getItem(4));
    }

    //Return a clone of the original item, so modifications to this don't change the original
    public static ItemStack borderItem(){
        return borderItem.clone();
    }
}
