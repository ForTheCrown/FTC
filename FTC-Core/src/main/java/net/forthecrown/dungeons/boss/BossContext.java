package net.forthecrown.dungeons.boss;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.core.ComVars;
import net.forthecrown.utils.math.MathUtil;
import net.forthecrown.utils.transformation.FtcBoundingBox;
import org.bukkit.GameMode;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;

public record BossContext(float modifier, List<Player> players) {
    public static BossContext create(FtcBoundingBox room) {
        // A lot of this stuff is arbitrary and should be changed
        //
        // It should dynamically change the boss difficulty based on
        // the amount of people in the room and the gear they have.
        // Aka the amount of gear they have and the quality of the
        // gear

        int armorAmount = 0;
        int enchants = 0;

        Collection<Player> players = room.getPlayers();
        players.removeIf(player -> player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR  );

        for (Player p: players) {
            for (ItemStack i: p.getInventory().getArmorContents()){
                if(i == null) continue;
                armorAmount++;
                enchants += i.getEnchantments().size();
            }

            for (ItemStack s: p.getInventory().getStorageContents()) {
                if(s == null) continue;
                enchants += s.getEnchantments().size();
            }
        }

        // Also count any dropped items in the room to stop
        // people from artificially lowering the difficulty
        for (Item i: room.getEntitiesByType(Item.class)) {
            enchants += i.getItemStack().getEnchantments().size();
        }

        // Yeah... I have no idea what this calculation is lmao
        // what = ((enchants / 3) * 2) + (armorAmount / 17),
        // that's basically what it is
        float what = ((((float) enchants / 3) * 2) + (float) armorAmount) / ((float) players.size() < 2 ? 20 : 17);

        //Clamp difficulty
        float finalModifier = (float) MathUtil.clamp(what, 1D, ComVars.getMaxBossDifficulty());

        return new BossContext(finalModifier, new ObjectArrayList<>(players));
    }

    /**
     * Creates a health value by multiplying the
     * given initial health value with the context's
     * modifier
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
