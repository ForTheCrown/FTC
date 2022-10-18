package net.forthecrown.dungeons.boss;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.core.Vars;
import net.forthecrown.inventory.ExtendedItems;
import net.forthecrown.utils.inventory.ItemStacks;
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
    public static final float MIN_MODIFIER = 1.0F;

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
            for (ItemStack s: p.getInventory().getArmorContents()) {
                if (ItemStacks.isEmpty(s)) {
                    continue;
                }

                armorAmount++;
                enchants += s.getEnchantments().size();

                if (!ExtendedItems.isSpecial(s)) {
                    continue;
                }

                royalItems++;
                totalRoyalRank += getRank(s);
            }

            for (ItemStack s: p.getInventory().getStorageContents()) {
                if (ItemStacks.isEmpty(s)) {
                    continue;
                }

                enchants += s.getEnchantments().size();

                if (!ExtendedItems.isSpecial(s)) {
                    continue;
                }

                royalItems++;
                totalRoyalRank += getRank(s);
            }
        }

        // Also count any dropped items in the room to stop
        // people from artificially lowering the difficulty
        for (Item i: room.getEntitiesByType(Item.class)) {
            ItemStack s = i.getItemStack();
            enchants += s.getEnchantments().size();

            if (!ExtendedItems.isSpecial(s)) {
                continue;
            }

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
        if (Float.isNaN(modifier)) {
            modifier = MIN_MODIFIER;
        }

        return new BossContext(Mth.clamp(modifier, MIN_MODIFIER, Vars.maxBossDifficulty), new ObjectArrayList<>(players));
    }

    private static int getRank(ItemStack s) {
        var sword = ExtendedItems.ROYAL_SWORD.get(s);
        var crown = ExtendedItems.CROWN.get(s);

        if (sword != null) {
            return sword.getRank().getViewerRank();
        }

        if (crown != null) {
            return crown.getRank();
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