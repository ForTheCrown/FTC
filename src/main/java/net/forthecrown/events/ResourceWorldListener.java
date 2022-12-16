package net.forthecrown.events;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.core.Worlds;
import net.forthecrown.core.config.ResourceWorldConfig;
import net.forthecrown.core.resource.ResourceWorldTracker;
import net.forthecrown.inventory.ExtendedItems;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.math.Vectors;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import static org.bukkit.entity.EntityType.*;

public class ResourceWorldListener implements Listener {
    public static final Random RANDOM = new Random();

    private boolean testWorld(World world) {
        return world.equals(Worlds.resource());
    }

    private boolean testBlock(Block block) {
        return ResourceWorldTracker.get()
                .isNatural(Vectors.from(block));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockBreakEvent(BlockBreakEvent event) {
        // Cancel double dropping if:
        // Not in the RW
        if (!testWorld(event.getBlock().getWorld())
                // If broken block is a chest or something, it'd
                // be too OP if it worked on containers
                || event.getBlock().getState() instanceof Container

                // If they were unlucky
                || ResourceWorldConfig.doubleDropRate < RANDOM.nextFloat(1f)

                // Not a natural block or in creative
                || !testBlock(event.getBlock())
                || event.getPlayer().getGameMode() == GameMode.CREATIVE
        ) {
            return;
        }

        // Since we can't modify the drops directly in the
        // event, we cancel all drops
        event.setDropItems(false);

        Player player = event.getPlayer();
        ItemStack breaking = player.getInventory().getItemInMainHand();

        // Process drops, aka double everything that's dropped
        Collection<ItemStack> drops = event.getBlock().getDrops(breaking, player);
        Collection<ItemStack> toDrop = processDrops(drops);

        // Drop all new drops
        World world = event.getBlock().getWorld();
        Location l = event.getBlock().getLocation();

        for (ItemStack i: toDrop) {
            world.dropItemNaturally(l, i.clone());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        onBlockBecomeNonNatural(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        onBlockBecomeNonNatural(event);
    }

    private void onBlockBecomeNonNatural(BlockEvent event) {
        if (!testWorld(event.getBlock().getWorld())) {
            return;
        }

        ResourceWorldTracker.get()
                .setNonNatural(Vectors.from(event.getBlock()));
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
        // Cancel entity double drop if:
        // Entity is not allowed to drop anything
        if (ILLEGAL_ENTITIES.contains(event.getEntityType())

                // Not in the rw
                || !testWorld(event.getEntity().getWorld())

                // They were unlucky :(
                || ResourceWorldConfig.doubleDropRate < RANDOM.nextFloat(1f)
        ) {
            return;
        }

        Collection<ItemStack> drops = processDrops(event.getDrops());

        event.getDrops().clear();
        event.getDrops().addAll(drops);
    }

    private Collection<ItemStack> processDrops(Collection<ItemStack> drops) {
        if (drops == null || drops.isEmpty()) {
            return drops;
        }

        List<ItemStack> toDrop = new ObjectArrayList<>();

        // Process drops
        for (ItemStack i: drops) {
            if (ItemStacks.isEmpty(i)) {
                continue;
            }

            // Just add 2 of the item to the result
            // ClearLagg will merge the items anyway
            // if they can be merged
            toDrop.add(i.clone());

            if (!ExtendedItems.isSpecial(i)) {
                toDrop.add(i.clone());
            }
        }

        return toDrop;
    }
}