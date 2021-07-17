package net.forthecrown.valhalla;

import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.enums.Branch;
import net.forthecrown.utils.CrownRandom;
import net.forthecrown.utils.Worlds;
import net.forthecrown.utils.math.BlockPos;
import net.forthecrown.utils.math.FtcRegion;
import net.forthecrown.valhalla.data.VikingRaid;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

public class RaidUtil {
    public static final FtcRegion GATHERING_GROUND = new FtcRegion(Worlds.OVERWORLD, 1869, 57, -2225, 1895, 80, -2200);

    public static Collection<Player> getGatheredVikings() {
        return GATHERING_GROUND.getPlayers()
                .stream()
                .filter(plr -> {
                    CrownUser user = UserManager.getUser(plr);
                    return user.getBranch() == Branch.VIKINGS;
                })
                .toList();
    }

    public static RaidGenerationContext createTestContext(VikingRaid raid) {
        return new RaidGenerationContext(Worlds.VOID, raid, new CrownRandom(), RaidDifficulty.medium(), new ArrayList<>());
    }

    public static void applyLootTable(BlockPos pos, World world, LootTable lootTable, Random random, float mod) {
        Block block = pos.getBlock(world);
        block.setType(Material.CHEST);

        Chest chest = (Chest) block.getState();

        chest.getBlockInventory().clear();

        chest.setLootTable(lootTable);
        lootTable.fillInventory(
                chest.getBlockInventory(),
                random,
                new LootContext.Builder(chest.getLocation()).luck(mod).build()
        );
    }
}
