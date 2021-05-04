package net.forthecrown.royals.dungeons.bosses;

import net.forthecrown.royals.dungeons.bosses.mobs.DungeonBoss;
import org.bukkit.GameMode;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BossFightContext implements Predicate<Player> {

    private final Collection<Player> players;
    private final float finalModifier;
    private final DungeonBoss<?> boss;

    //+1 per each in player's inv
    private int enchants = 0;
    private int armorAmount = 0;

    public BossFightContext(DungeonBoss<?> boss){
        this.boss = boss;
        players = boss.getBossRoom().getPlayers().stream().filter(this).collect(Collectors.toList());

        calculateBase();
        float initialMod = Math.max(1, (float) (enchants + armorAmount) / (players.size() < 2 ? 25 : 20));
        finalModifier = Math.min(initialMod, 5);
    }


    private void calculateBase(){
        for (Player p: players){
            for (ItemStack i: p.getInventory().getArmorContents()){
                if(i == null) continue;
                armorAmount++;
                enchants += i.getEnchantments().size();
            }

            for (ItemStack s: p.getInventory().getStorageContents()){
                if(s == null) continue;
                enchants += s.getEnchantments().size();
            }
        }

        for (Item i: boss.getBossRoom().getEntitiesByType(Item.class)){
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
        return Math.ceil(initialDamage + finalModifier);
    }

    @Override
    public boolean test(Player player) {
        return player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE;
    }
}
