package net.forthecrown.events;

import com.destroystokyo.paper.event.inventory.PrepareResultEvent;
import net.forthecrown.utils.inventory.ItemStacks;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.CartographyInventory;

public class NoCopiesListener implements Listener {
    private static final String TAG_NO_COPIES = "no_copies";

    private static final int SLOT_ADDITIONAL = 1;

    @EventHandler(ignoreCancelled = true)
    public void onPrepareResult(PrepareResultEvent event) {
        // Not a cartography table
        if (!(event.getInventory() instanceof CartographyInventory table)) {
            return;
        }

        // Result item empty, don't care
        if (ItemStacks.isEmpty(event.getResult())) {
            return;
        }

        var result = event.getResult();
        var resultMeta = result.getItemMeta();

        // Item doesn't have 'no copies' tag, stop
        if (!ItemStacks.hasTagElement(resultMeta, TAG_NO_COPIES)) {
            return;
        }

        Material additional = table.getItem(SLOT_ADDITIONAL).getType();

        // They're attempting to copy map, stop them!
        if (additional == Material.MAP) {
            event.setResult(null);
            return;
        }

        var tags = ItemStacks.getUnhandledTags(resultMeta);
        tags.putBoolean(TAG_NO_COPIES, true);

        ItemStacks.setUnhandledTags(resultMeta, tags);
        result.setItemMeta(resultMeta);

        event.setResult(result);
    }
}