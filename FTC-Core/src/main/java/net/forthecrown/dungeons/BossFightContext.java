package net.forthecrown.dungeons;

import net.forthecrown.core.ComVars;
import net.forthecrown.dungeons.bosses.DungeonBoss;
import net.forthecrown.utils.math.MathUtil;
import org.bukkit.GameMode;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.stream.Collectors;

public class BossFightContext {

    private final Collection<Player> players;
    private final float finalModifier;
    private final DungeonBoss<?> boss;

    private int enchants = 0;
    private int armorAmount = 0;

    public BossFightContext(DungeonBoss<?> boss){
        this.boss = boss;
        players = boss.getBossRoom()
                .getPlayers()
                .stream()
                .filter(player -> player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE)
                .collect(Collectors.toList());

        calculateBase();

        // Yeah... I have no idea what this calculation is lmao
        // what = ((enchants / 3) * 2) + (armorAmount / 17),
        // that's basically what it is
        float what = ((((float) enchants / 3) * 2) + (float) armorAmount) / ((float) players.size() < 2 ? 20 : 17);

        //Clamp difficulty
        finalModifier = (float) MathUtil.clamp(what, 1D, ComVars.getMaxBossDifficulty());
    }

    private void calculateBase() {
        // A lot of this stuff is arbitrary and should be changed
        //
        // It should dynamically change the boss difficulty based on
        // the amount of people in the room and the gear they have.
        // Aka the amount of gear they have and the quality of the
        // gear

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
        for (Item i: boss.getBossRoom().getEntitiesByType(Item.class)) {
            enchants += i.getItemStack().getEnchantments().size();
        }
    }

    public int getEnchantAmount() {
        return enchants;
    }

    public int getArmorAmount() {
        return armorAmount;
    }

    public Collection<Player> getPlayers() {
        return players;
    }

    public float getModifier(){
        return finalModifier;
    }

    public double getBossHealth(double initialHealth){
        return Math.ceil(initialHealth * finalModifier);
    }

    public double getBossDamage(double initialDamage){
        final double damage = (finalModifier / 3) * initialDamage;

        return Math.ceil(damage + finalModifier);
    }
}
