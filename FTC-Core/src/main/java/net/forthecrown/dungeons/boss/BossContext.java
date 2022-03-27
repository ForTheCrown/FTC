package net.forthecrown.dungeons.boss;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.core.FtcVars;
import net.forthecrown.inventory.ItemStacks;
import net.forthecrown.inventory.crown.Crowns;
import net.forthecrown.inventory.crown.RoyalCrown;
import net.forthecrown.inventory.weapon.RoyalSword;
import net.forthecrown.inventory.weapon.RoyalWeapons;
import net.forthecrown.utils.math.WorldBounds3i;
import net.minecraft.util.Mth;
import org.apache.commons.lang.Validate;
import org.bukkit.GameMode;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;

public record BossContext(float modifier, List<Player> players) {
    public static BossContext create(WorldBounds3i room) {
        // A lot of this stuff is arbitrary and should be changed
        //
        // It should dynamically change the boss difficulty based on
        // the amount of people in the room and the gear they have.
        // Aka the amount of gear they have and the quality of the
        // gear

        Collection<Player> players = room.getPlayers();
        players.removeIf(player -> player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR);

        Validate.isTrue(!players.isEmpty(), "Cannot spawn boss with no 'acceptable' players, all players must be in survival");

        float armorAmount = 0;
        float enchants = 0;
        float royalItems = 0;
        float totalRoyalRank = 0;
        float playerSize = players.size();

        for (Player p: players) {
            for (ItemStack s: p.getInventory().getArmorContents()){
                if(ItemStacks.isEmpty(s)) continue;
                armorAmount++;
                enchants += s.getEnchantments().size();

                if(!ItemStacks.isSpecial(s)) continue;
                royalItems++;
                totalRoyalRank += getRank(s);
            }

            for (ItemStack s: p.getInventory().getStorageContents()) {
                if(ItemStacks.isEmpty(s)) continue;
                enchants += s.getEnchantments().size();

                if(!ItemStacks.isSpecial(s)) continue;
                royalItems++;
                totalRoyalRank += getRank(s);
            }
        }

        // Also count any dropped items in the room to stop
        // people from artificially lowering the difficulty
        for (Item i: room.getEntitiesByType(Item.class)) {
            ItemStack s = i.getItemStack();
            enchants += s.getEnchantments().size();

            if(!ItemStacks.isSpecial(s)) continue;
            royalItems++;
            totalRoyalRank += getRank(s);
        }

        float avgEnchants = enchants / playerSize;
        float avgArmor = armorAmount / playerSize;
        float avgRank = royalItems == 0 ? 0.0F : (totalRoyalRank / royalItems) / 10;
        float modifier = (avgArmor + avgRank + avgEnchants) / 40;

        // Should only be true in testing circumstances when all the above
        // avg variables are 0, because nobody could possibly want to solo
        // a boss with nothing in their inventory
        if(Float.isNaN(modifier)) modifier = 1.0F;

        return new BossContext(Mth.clamp(modifier, 1F, FtcVars.maxBossDifficulty.get()), new ObjectArrayList<>(players));
    }

    private static int getRank(ItemStack s) {
        if(Crowns.isCrown(s)) {
            // Crown is half as important as sword
            return new RoyalCrown(s).getRank() >> 1;
        }

        if(RoyalWeapons.isRoyalSword(s)) {
            return new RoyalSword(s).getRank();
        }
        return 0;
    }

    /**
     * Creates a health value by multiplying the
     * given initial health value with the context's
     * modifier and rounding up
     * @param initial The initial health value
     * @return The modifier multiplied health value
     */
    public double health(double initial) {
        return Math.ceil(initial * modifier());
    }

    public double damage(double initial) {
        final double damage = (modifier() / 3) * initial;
        return Math.ceil(damage + modifier());
    }
}
