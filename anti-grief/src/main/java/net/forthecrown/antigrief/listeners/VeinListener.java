package net.forthecrown.antigrief.listeners;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Map;
import java.util.Set;
import net.forthecrown.antigrief.AntiGriefPlugin;
import net.forthecrown.antigrief.EavesDropper;
import net.forthecrown.antigrief.GriefPermissions;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.math.Vectors;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scheduler.BukkitTask;
import org.spongepowered.math.vector.Vector3i;

class VeinListener implements Listener {

  private static final BlockFace[] FACES = {
      BlockFace.NORTH,
      BlockFace.SOUTH,
      BlockFace.EAST,
      BlockFace.WEST,
      BlockFace.UP,
      BlockFace.DOWN
  };

  private static final Map<Vector3i, OreVein> BLOCK_2_VEIN = new Object2ObjectOpenHashMap<>();

  private final AntiGriefPlugin plugin;

  public VeinListener(AntiGriefPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(ignoreCancelled = true)
  public void onBlockBreak(BlockBreakEvent event) {
    Material[] ores = plugin.getPluginConfig().getVeinReporterBlocks();
    var block = event.getBlock();

    if (!ArrayUtils.contains(ores, block.getType())) {
      return;
    }

    var player = event.getPlayer();
    Vector3i pos = Vectors.from(block);

    // Creative mode player or admins don't count
    if (player.getGameMode() == GameMode.CREATIVE
        || player.hasPermission(GriefPermissions.EAVESDROP_ADMIN)
    ) {
      return;
    }

    // If this vein has already been mined
    //
    if (BLOCK_2_VEIN.containsKey(pos)) {
      var vein = BLOCK_2_VEIN.remove(pos);
      vein.blocks.remove(pos);

      // Empty means all blocks have been mined
      // So remove vein early before it expires
      if (vein.blocks.isEmpty()) {
        Tasks.cancel(vein.task);
      }

      return;
    }

    Set<Vector3i> blocks = new ObjectOpenHashSet<>();
    countVeins(blocks, block.getWorld(), pos, block.getType());

    OreVein vein = new OreVein(blocks);
    vein.add();

    EavesDropper.reportOreMining(block, vein.blocks.size(), player);
  }

  private void countVeins(Set<Vector3i> blocks, World world, Vector3i pos, Material material) {
    var block = Vectors.getBlock(pos, world);

    if (block.getType() != material || blocks.contains(pos)) {
      return;
    }

    blocks.add(pos);

    for (var face : FACES) {
      countVeins(blocks, world, pos.add(face.getModX(), face.getModY(), face.getModZ()), material);
    }
  }

  private static final class OreVein {

    private final Set<Vector3i> blocks;
    private final BukkitTask task;

    private OreVein(Set<Vector3i> blocks) {
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
