package net.forthecrown.valhalla;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.grenadier.types.KeyArgument;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.enums.Branch;
import net.forthecrown.utils.CrownRandom;
import net.forthecrown.utils.Worlds;
import net.forthecrown.utils.math.FtcBoundingBox;
import net.forthecrown.utils.math.Vector3i;
import net.forthecrown.valhalla.data.VikingRaid;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

public class VikingUtil {
    public static final FtcBoundingBox GATHERING_GROUND = new FtcBoundingBox(Worlds.OVERWORLD,
            1869, 57, -2225,
            1895, 80, -2200
    );

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

    public static void applyLootTable(Vector3i pos, World world, LootTable lootTable, Random random, float mod) {
        Block block = pos.getBlock(world);
        block.setType(Material.CHEST);

        Chest chest = (Chest) block.getState();

        chest.getBlockInventory().clear();

        lootTable.fillInventory(
                chest.getBlockInventory(),
                random,
                new LootContext.Builder(chest.getLocation())
                        .luck(mod)
                        .build()
        );
    }

    public static Attribute attributeFromKey(String strKey) {
        try {
            NamespacedKey key = KeyArgument.minecraft().parse(new StringReader(strKey));

            for (Attribute a: Attribute.values()) {
                if(a.getKey().equals(key)) return a;
            }

        } catch (CommandSyntaxException ignored) {}

        return null;
    }
}
