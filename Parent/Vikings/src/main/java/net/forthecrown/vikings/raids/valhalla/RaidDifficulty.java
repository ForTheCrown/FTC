package net.forthecrown.vikings.raids.valhalla;

import net.forthecrown.vikings.VikingUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Set;

//TODO Get Wout to do the math, I can't do it lol
public class RaidDifficulty {

    private final RaidParty party;

    private byte playerAmount = 0;
    private AverageArmorLevel armorLevel;
    private short enchantmentAmount = 0;
    private float dif_modifier = 0;

    public RaidDifficulty(RaidParty party){
        this.party = party;
    }

    public void calculateModifier(){
        playerAmount = (byte) party.getParticipants().size();

        Set<Player> players = party.getParticipants();
        armorLevel = AverageArmorLevel.getFromPlayers(players);

        for (Player p: players){
            for (ItemStack i: p.getInventory()){
                if(i == null) continue;
                if(i.getEnchantments().size() < 1) continue;
                enchantmentAmount += i.getEnchantments().size();
            }
        }
        dif_modifier = ((float) playerAmount / 2) + (((float) enchantmentAmount + armorLevel.getWeight()) / 20);
    }

    public AverageArmorLevel getArmorLevel() {
        return armorLevel;
    }

    public float getModifier() {
        return dif_modifier;
    }

    public RaidParty getParty() {
        return party;
    }

    public enum AverageArmorLevel {
        NETHERITE((byte) 35),
        DIAMOND((byte) 20),
        IRON((byte) 10),
        BELOW_IRON((byte) 1);

        private final byte weight;

        AverageArmorLevel(byte weight) {
            this.weight = weight;
        }

        public byte getWeight() {
            return weight;
        }

        public static AverageArmorLevel getFromPlayers(Collection<Player> players) {
            byte[] armorContents = {0, 0, 0, 0}; // 0 - Below, 1 - Iron, 2 - Diamond, 3 - Netherite

            for (Player p : players) {
                for (ItemStack i : p.getInventory().getArmorContents()) {
                    if (i == null) continue;
                    String type = i.getType().toString().toLowerCase();
                    if (type.contains("netherite")) armorContents[3]++;
                    else if (type.contains("diamond")) armorContents[2]++;
                    else if (type.contains("iron")) armorContents[1]++;
                    else armorContents[0]++;
                }
            }

            final byte biggest = VikingUtils.findBiggestInArray(armorContents);

            if (biggest == armorContents[3]) return NETHERITE;
            if (biggest == armorContents[2]) return DIAMOND;
            if (biggest == armorContents[1]) return IRON;
            return BELOW_IRON;
        }
    }
}
