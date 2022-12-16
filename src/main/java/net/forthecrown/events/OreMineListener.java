package net.forthecrown.events;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.admin.EavesDropper;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.utils.Tasks;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scheduler.BukkitTask;
import org.spongepowered.math.vector.Vector3i;

import java.util.Map;
import java.util.Set;

public class OreMineListener implements Listener {
    private static final BlockFace[] FACES = {
            BlockFace.NORTH,
            BlockFace.SOUTH,
            BlockFace.EAST,
            BlockFace.WEST,
            BlockFace.UP,
            BlockFace.DOWN
    };

    private static final Set<Material> ORES = ObjectSet.of(
            // Copied from FTC2: net.forthecrown.core.events.PlayerBreakBlockListener

            // Material.COAL_ORE,
            // Material.COPPER_ORE,
            Material.DIAMOND_ORE,
            Material.EMERALD_ORE,
            // Material.GOLD_ORE,
            Material.IRON_ORE,
            Material.LAPIS_ORE,
            Material.REDSTONE_ORE,

            // Material.DEEPSLATE_COAL_ORE,
            // Material.DEEPSLATE_COPPER_ORE,
            Material.DEEPSLATE_DIAMOND_ORE,
            Material.DEEPSLATE_EMERALD_ORE,
            // Material.DEEPSLATE_GOLD_ORE,
            Material.DEEPSLATE_IRON_ORE,
            Material.DEEPSLATE_LAPIS_ORE,
            Material.DEEPSLATE_REDSTONE_ORE,

            Material.NETHERITE_SCRAP
    );

    private static final Map<Block, OreVein> BLOCK_2_VEIN = new Object2ObjectOpenHashMap<>();

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!ORES.contains(event.getBlock().getType())) {
            return;
        }

        var player = event.getPlayer();
        var block = event.getBlock();

        // Creative mode player or admins don't count
        if (player.getGameMode() == GameMode.CREATIVE
                || player.hasPermission(Permissions.EAVESDROP_ADMIN)
        ) {
            return;
        }

        // If this vein has already been mined
        //
        if (BLOCK_2_VEIN.containsKey(block)) {
            var vein = BLOCK_2_VEIN.get(block);
            vein.blocks.remove(block);

            // Empty means all blocks have been mined
            // So remove vein early before it expires
            if (vein.blocks.isEmpty()) {
                Tasks.cancel(vein.task);
                BLOCK_2_VEIN.remove(block);
            }

            return;
        }

        Set<Block> blocks = new ObjectOpenHashSet<>();
        var pos = Vectors.from(block);
        countVeins(blocks, block.getWorld(), pos, block.getType());

        OreVein vein = new OreVein(blocks);
        vein.add();

        EavesDropper.reportOreMining(block, vein.blocks.size(), player);
    }

    private void countVeins(Set<Block> blocks, World world, Vector3i pos, Material material) {
        var block = Vectors.getBlock(pos, world);

        if (block.getType() != material
                || blocks.contains(block)
        ) {
            return;
        }

        blocks.add(block);

        for (var face: FACES) {
            countVeins(blocks, world, pos.add(face.getModX(), face.getModY(), face.getModZ()), material);
        }
    }

    private static final class OreVein {
        private final Set<Block> blocks;
        private final BukkitTask task;

        private OreVein(Set<Block> blocks) {
            this.blocks = blocks;
            this.task = Tasks.runLater(this::remove, 20 * 20); // 20 seconds
        }

        void remove() {
            for (var b : blocks) {
                BLOCK_2_VEIN.remove(b);
            }
        }

        void add() {
            for (var b : blocks) {
                BLOCK_2_VEIN.put(b, this);
            }
        }
    }
}