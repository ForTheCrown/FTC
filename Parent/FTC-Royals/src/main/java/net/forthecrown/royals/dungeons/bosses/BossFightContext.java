package net.forthecrown.royals.dungeons.bosses;

import net.forthecrown.royals.dungeons.bosses.mobs.DungeonBoss;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.stream.Collectors;

public class BossFightContext {

    private final Collection<Player> players;
    private final float finalModifier;

    //+1 per each in player's inv
    private int enchants = 0;
    private int armorAmount = 0;
    //private int itemAmount = 0;

    public BossFightContext(DungeonBoss<?> boss){
        players = boss.bossRoom().getPlayers().stream().filter(plr -> plr.getGameMode() == GameMode.SURVIVAL).collect(Collectors.toList());

        calculateBase();
        float initialMod = Math.max(1, (float) (enchants + armorAmount + players.size())/20);
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
                    case TOTEM_OF_UNDYING:
                    case CROSSBOW:
                    case NETHERITE_BOOTS:
                    case NETHERITE_CHESTPLATE:
                    case NETHERITE_LEGGINGS:
                    case NETHERITE_HELMET:
                    case DIAMOND_BOOTS:
                    case DIAMOND_CHESTPLATE:
                    case DIAMOND_LEGGINGS:
                    case DIAMOND_HELMET:
                    case GOLDEN_SWORD:
                    case BOW:
                        //itemAmount++;
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

    public Collection<Player> players() {
        return players;
    }

    public float finalModifier(){
        return finalModifier;
    }

    /*public int itemAmount() {
        return itemAmount;
    }*/

    public double bossHealthMod(double initialHealth){
        return Math.ceil(initialHealth * finalModifier);
    }

    public double bossDamageMod(double initialDamage){
        return Math.ceil(initialDamage + finalModifier);
    }
}
