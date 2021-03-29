package net.forthecrown.royals.dungeons.bosses;

import com.google.common.collect.ImmutableList;
import net.forthecrown.royals.dungeons.bosses.mobs.DungeonBoss;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BossFightContext {

    private final ImmutableList<Player> players;
    private final float finalModifier;

    //+1 per each in player's inv
    private int enchants = 0;
    private int armorAmount = 0;

    public BossFightContext(DungeonBoss<?> boss){
        this.players = ImmutableList.copyOf(boss.bossRoom().getPlayers());

        calculateBase();
        float initialMod = Math.max(1, (float) (enchants + armorAmount + players.size())/7);
        finalModifier = Math.min(initialMod, 5);
    }

    private void calculateBase(){
        for (Player p: players){
            for (ItemStack i: p.getInventory().getArmorContents()){
                if(i == null) continue;
                armorAmount++;
            }

            for (ItemStack s: p.getInventory().getStorageContents()){
                if(s == null) continue;

                switch (s.getType()){
                    case TRIDENT:
                    case DIAMOND_SWORD:
                    case NETHERITE_SWORD:
                    case DIAMOND_AXE:
                    case NETHERITE_AXE:
                    case CROSSBOW:
                    case BOW:
                        enchants += s.getEnchantments().size();
                }
            }
        }
    }

    public int enchantModifier() {
        return enchants;
    }

    public int armorModifier() {
        return armorAmount;
    }

    public ImmutableList<Player> players() {
        return players;
    }

    public float finalModifier(){
        return finalModifier;
    }

    public double bossHealthMod(double initialHealth){
        return Math.ceil(initialHealth * finalModifier);
    }
}
