package ftc.randomfeatures.features;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

public class HaroldValentinesEvent implements Listener {

    public final static Location SHULKER_LOC = new Location(Bukkit.getWorld("world"), 279, 79, 965);

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if(!(event.getRightClicked() instanceof Villager)) return;
        Villager harold = (Villager) event.getRightClicked();
        if(harold.getCustomName() == null || !harold.getCustomName().equals(ChatColor.GOLD + "Harold")) return;
        if(!harold.isInvulnerable()) return;

        Player player = event.getPlayer();


        //I just didn't want to remove this code, so I commented it out to stop the compiler from telling me to go screw myself

        /*if(Main.valentinesPlayer.contains(player.getUniqueId())){
            player.sendMessage(ChatColor.GRAY + "You've already helped Harold");
            return;
        }

        List<ItemStack> toRemove = new ArrayList<>();
        for (ItemStack s: Main.VALENTINE_ITEMS){
            for (ItemStack s1 : player.getInventory()){
                if(s1 == null) continue;

                if(!s1.isSimilar(s)) continue;
                toRemove.add(s);
            }
        }

        if(toRemove.size() < Main.VALENTINE_ITEMS.size()){
            final String[] toSend = {ChatColor.GOLD + "--- " + ChatColor.YELLOW + "You don't have all the items needed, you need:" + ChatColor.GOLD + " ---",
                    ChatColor.GOLD + "A" + ChatColor.YELLOW + " red bed,", ChatColor.GOLD + "32" + ChatColor.YELLOW + " Pumpkin pies,", ChatColor.GOLD + "5" + ChatColor.YELLOW + " Rose bushes",
                    ChatColor.GOLD + "2" + ChatColor.YELLOW + " Lanterns,", ChatColor.GOLD + "10" + ChatColor.YELLOW + " Carrots and", ChatColor.GOLD + "16" + ChatColor.YELLOW + " Baked potatoes"
            };
            player.sendMessage(toSend);
            return;
        }

        for (ItemStack s: toRemove){
            player.getInventory().removeItem(s);
        }*/

        player.getInventory().addItem(getShulker());
        player.sendMessage(ChatColor.YELLOW + "Harold is thankful for your help");
        //Main.valentinesPlayer.add(player.getUniqueId());
    }

    private ItemStack getShulker(){
        if(SHULKER_LOC.getBlock().getType() != Material.CHEST) throw new NullPointerException("Chest where valentines shulker should be is not chest");

        Chest chest = (Chest) SHULKER_LOC.getBlock().getState();

        for (ItemStack s : chest.getInventory()){
            if(s == null) continue;
            return s;
        }
        return null;
    }
}
