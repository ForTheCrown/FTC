package ftc.bigcrown.events;

import ftc.bigcrown.Main;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.List;

public class Events implements Listener {

    @EventHandler
    public void onPresentUse(PlayerInteractEntityEvent event){
        Entity interacted = event.getRightClicked();
        Player player = event.getPlayer();

        if(!event.getRightClicked().getWorld().getName().equals("world")) return;
        if(!(interacted instanceof Slime)) return;
        Slime slime = (Slime) interacted;
        if(!slime.isInvulnerable() && slime.getCustomName() == null) return;
        if(!slime.getCustomName().equals(ChatColor.GOLD + "Present")) return;

        List<Location> locList = (List<Location>) Main.plugin.getConfig().getList("ChallengeList");
        if(locList.size() <= 0) return;

        double initialChance = Math.random();
        if(initialChance <= 0.25){ //Challange
            ChallengeClass cc = new ChallengeClass(player);

        } else {

            //The code ahead isn't finished, this is because I heard you said you'd make this part, sorry if I misheard, but i left it relativly empty for you to do
            //ask you like.

            Material mat = Material.AIR;
            int amount = 0;
            String name = null;
            String lore1 = null;
            String lore2 = null;
            String lore3 = null;

            if (initialChance > 0.25 && initialChance <= 0.75) { //50% item range
                switch ((int) (Math.random() * 6)) {
                    default: // nothing lol
                        break;
                    case 1: // charcoal: for the naughty kids
                        mat = Material.CHARCOAL;
                        amount = 1;
                        name = "Charcoal for the naughty kids";
                        lore1 = "";
                        lore2 = "";
                        lore3 = "";
                        break;
                    case 2: // 5 emeralds
                        mat = Material.EMERALD;
                        amount = 5;
                        break;
                    case 3: // Sweet berries
                        mat = Material.SWEET_BERRIES;
                        amount = 1;
                        break;
                    case 4: // Golden Nugget
                        mat = Material.GOLD_NUGGET;
                        amount = 1;
                        break;
                    case 5: // 3 Roast Chickens
                        mat = Material.COOKED_CHICKEN;
                        amount = 3;
                        break;
                    case 6: // 5 snowballs
                        mat = Material.SNOWBALL;
                        amount = 5;
                        break;
                }
            } else if (initialChance > 0.75 && initialChance <= 0.95) { //20% item range
                switch ((int) (Math.random()*4)){
                    default: // Gold Ingot
                        break;
                    case 1: // Rhines
                        break;
                    case 2: // Diamond
                        break;
                    case 3: // Bebe (Totem of Undying)
                        break;
                    case 4: // Jingle Bell (A bell lol)
                        break;
                }
            } else { //5% item range
                switch ((int) (Math.random()*2)){
                    default: //gems
                        break;
                    case 1: // Selfpowered Sleigh (Oak Boat)
                        break;
                    case 2: // Christmas Tree (Spruce Sapling)
                        break;
                }
            }

            player.getLocation().getWorld().dropItemNaturally(player.getLocation(), Main.plugin.makeItem(mat, amount, name, lore1, lore2, lore3));
        }

        player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_WOLF_DEATH, 1, 1);
        player.getLocation().getWorld().spawnParticle(Particle.CRIT, player.getLocation(),10, 2, 2, 2);

        slime.getLocation().getBlock().setType(Material.AIR);
        slime.remove();
    }
}
