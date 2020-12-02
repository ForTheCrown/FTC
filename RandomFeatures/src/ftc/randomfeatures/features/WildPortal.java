package ftc.randomfeatures.features;

import ftc.randomfeatures.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class WildPortal implements Listener {

    @EventHandler
    public void test(EntityPortalEnterEvent event)
    {
        if (event.getEntity().getWorld().getName().contains("world_the_end"))
        {
            //Bukkit.broadcastMessage("happened! (2)");
            if (event.getEntity() instanceof Player)
            {
                try {
                    event.getEntity().teleport(((Player) event.getEntity()).getBedSpawnLocation());
                }
                catch (Exception e) {
                    event.getEntity().teleport(new Location(Bukkit.getWorld("world"), 200.5, 70, 1000.5));
                }
            }
            else
            {
                event.getEntity().teleport(new Location(Bukkit.getWorld("world"), 282.0, 72, 948.0));
            }
        }
        else
        {
            // Wildportals
            Location locs[] = {new Location(Bukkit.getWorld("world"), 181, 67, 1000), new Location(Bukkit.getWorld("world"), 269, 62, 1014)};

            for (Location loc : locs)
            {
                try {
                    if (loc.distance(event.getEntity().getLocation()) > 3) continue;
                } catch (Exception e) {
                    continue;
                }

                if (!(event.getEntity() instanceof Player))
                    return;

                Player player = (Player) event.getEntity();

                int x = 0;
                if (Math.random() < 0.5)
                    x = Main.getRandomNumberInRange(200, 4800);
                else
                    x = Main.getRandomNumberInRange(-4800, -200);

                int z = 0;
                if (Math.random() < 0.5)
                    z = Main.getRandomNumberInRange(200, 4800);
                else
                    z = Main.getRandomNumberInRange(-4800, -200);

                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 400, 1));
                player.teleport(new Location(player.getWorld(), x, 150, z));

                player.setBedSpawnLocation(new Location(player.getWorld(), 200, 70, 1000), true);
                player.sendMessage(ChatColor.GRAY + "You've been teleported. To get back to spawn, you can do " + ChatColor.YELLOW + "/findpost" + ChatColor.GRAY + " and then " + ChatColor.YELLOW + "/visit hazelguard" + ChatColor.GRAY + ".");
                break;
            }
        }

    }
}
