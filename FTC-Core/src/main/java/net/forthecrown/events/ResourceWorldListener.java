package net.forthecrown.events;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.core.FtcVars;
import net.forthecrown.core.Worlds;
import net.forthecrown.inventory.ItemStacks;
import net.forthecrown.utils.CrownRandom;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.ListUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Container;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import static org.bukkit.entity.EntityType.*;

public class ResourceWorldListener implements Listener {
    public static final CrownRandom RANDOM = new CrownRandom();

    private boolean testWorld(World world) {
        return world.equals(Worlds.resource());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockBreakEvent(BlockBreakEvent event) {
        if (!testWorld(event.getBlock().getWorld())) return;
        if (event.getBlock().getState() instanceof Container) return;
        if (FtcVars.rwDoubleDropRate.get() < RANDOM.nextFloat(1f)) return;
        if (!FtcUtils.isNaturallyPlaced(event.getBlock())) return;
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) return;

        // Since we can't modify the drops directly in the
        // event, we cancel all drops
        event.setDropItems(false);

        Player player = event.getPlayer();
        ItemStack breaking = player.getInventory().getItemInMainHand();

        // Process drops, aka double everything that's dropped
        Collection<ItemStack> drops = event.getBlock().getDrops(breaking, player);
        List<ItemStack> toDrop = processDrops(drops);

        // Drop all new drops
        World world = event.getBlock().getWorld();
        Location l = event.getBlock().getLocation();
        for (ItemStack i: toDrop) {
            world.dropItemNaturally(l, i.clone());
        }
    }

    // All entity types that aren't allowed to have their
    // drops doubled
    public static EnumSet<EntityType> ILLEGAL_ENTITIES = EnumSet.of(
            ITEM_FRAME,     GLOW_ITEM_FRAME,
            ARMOR_STAND,    WITHER,
            PLAYER
    );

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if (ILLEGAL_ENTITIES.contains(event.getEntityType())) return;
        if (!testWorld(event.getEntity().getWorld())) return;
        if (FtcVars.rwDoubleDropRate.get() < RANDOM.nextFloat(1f)) return;

        List<ItemStack> drops = processDrops(event.getDrops());

        event.getDrops().clear();
        event.getDrops().addAll(drops);
    }

    private List<ItemStack> processDrops(Collection<ItemStack> drops) {
        List<ItemStack> toDrop = new ObjectArrayList<>();
        if (ListUtils.isNullOrEmpty(drops)) return toDrop;

        // Process drops
        for (ItemStack i: drops) {
            if(ItemStacks.isEmpty(i)) continue;

            // Just add 2 of the item to the result
            // ClearLagg will merge the items anyway
            // if they can be merged
            toDrop.add(i.clone());

            if(!ItemStacks.isSpecial(i)) {
                toDrop.add(i.clone());
            }
        }

        return toDrop;
    }
}