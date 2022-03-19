package net.forthecrown.dungeons;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.core.Crown;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.math.Vector3i;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BossLootBox {
    private final Map<UUID, ClaimData> claimDataMap = new Object2ObjectOpenHashMap<>();
    private final BoxListener listener;
    private final Vector3i pos;

    private ArmorStand display;
    private BoxRewards rewards;

    public BossLootBox(Vector3i pos) {
        this.pos = pos;

        listener = new BoxListener(this);
        Bukkit.getPluginManager().registerEvents(listener, Crown.inst());
    }

    public ArmorStand getDisplay() {
        return display;
    }

    public Vector3i getPos() {
        return pos;
    }

    public void remove() {
        if(display != null) {
            display.discard();
            display = null;
        }
    }

    public void destroy() {
        HandlerList.unregisterAll(listener);
    }

    public void create() {
    }

    public Map<UUID, ClaimData> getClaimDataMap() {
        return claimDataMap;
    }

    public BoxRewards getRewards() {
        return rewards;
    }

    public void setRewards(BoxRewards rewards) {
        this.rewards = rewards;
    }

    public void give(Player player) {
        ClaimData data = claimDataMap.getOrDefault(player.getUniqueId(), new ClaimData(new float[] { 1.0F }));
        LootTable table = getRewards().getTable();
        List<ItemStack> reward = new ObjectArrayList<>();

        if(table != null) {
            LootContext context = new LootContext.Builder(pos.toLoc(player.getWorld()))
                    .luck(data.popClaim())
                    .killer(player)
                    .build();

            reward.addAll(table.populateLoot(FtcUtils.RANDOM, context));
        }

        if(getRewards().getBossItems() != null) {
            reward.add(getRewards().getBossItems().item());
        }
    }

    public static class ClaimData {
        private float[] claims;

        public ClaimData(float[] claims) {
            this.claims = claims;
        }

        public float[] getClaims() {
            return claims;
        }

        public float popClaim() {
            float val = claims[0];
            claims = ArrayUtils.remove(claims, 0);

            return val;
        }

        public void addClaim(float difficultyMod) {
            claims = ArrayUtils.addFirst(claims, difficultyMod);
        }
    }

    public static class BoxRewards {
        private LootTable table;
        private BossItems bossItems;

        public BoxRewards(LootTable table, BossItems bossItems) {
            this.table = table;
            this.bossItems = bossItems;
        }

        public BoxRewards() {
        }

        public LootTable getTable() {
            return table;
        }

        public void setTable(LootTable table) {
            this.table = table;
        }

        public BossItems getBossItems() {
            return bossItems;
        }

        public void setBossItems(BossItems bossItems) {
            this.bossItems = bossItems;
        }
    }

    private static class BoxListener implements Listener {
        private final BossLootBox box;

        public BoxListener(BossLootBox box) {
            this.box = box;
        }

        public BossLootBox getBox() {
            return box;
        }

        private boolean testChunk(Chunk chunk) {
            return box.getPos().getChunkPos().toLong() == chunk.getChunkKey();
        }

        @EventHandler(ignoreCancelled = true)
        public void onChunkLoad(ChunkLoadEvent event) {
            if(!testChunk(event.getChunk())) return;

            Bukkit.getScheduler().runTaskLater(Crown.inst(), box::create, 1L);
        }

        @EventHandler(ignoreCancelled = true)
        public void onChunkUnload(ChunkUnloadEvent event) {
            if(!testChunk(event.getChunk())) return;

            box.remove();
        }

        @EventHandler(ignoreCancelled = true)
        public void onPlayerInteract(PlayerInteractEvent event) {
            Block b = event.getClickedBlock();

            if (b.getX() != box.getPos().getX() || b.getZ() != box.getPos().getZ()) {
                return;
            }


        }
    }
}
