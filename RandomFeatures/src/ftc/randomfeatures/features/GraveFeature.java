package ftc.randomfeatures.features;

import ftc.randomfeatures.Main;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GraveFeature implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Main.plugin.loadFiles();
        if (Main.plugin.gravesyaml.getList(event.getPlayer().getUniqueId().toString()) != null) {
            event.getPlayer().sendMessage(ChatColor.GRAY + "[FTC] You have royal items in your " + ChatColor.YELLOW + "/grave" + ChatColor.GRAY + ".");
        }
        Main.plugin.unloadFiles();
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Main.plugin.loadFiles();
        if (Main.plugin.gravesyaml.getList(event.getPlayer().getUniqueId().toString()) != null) {
            event.getPlayer().sendMessage(ChatColor.GRAY + "[FTC] You have royal items in your " + ChatColor.YELLOW + "/grave" + ChatColor.GRAY + ".");
        }
        Main.plugin.unloadFiles();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // Find royal tools
        List<ItemStack> graveitems = new ArrayList<>();
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                if (item.getItemMeta().hasLore() && (item.getType() == Material.GOLDEN_HELMET || item.getType() == Material.GOLDEN_SWORD || item.getType() == Material.NETHERITE_SWORD)) {
                    graveitems.add(item.clone());
                    item.setAmount(0);
                }
            }
        }

        // Add to file or existing grave if found.
        if (!graveitems.isEmpty()) {
            Main.plugin.loadFiles();
            if (Main.plugin.gravesyaml.getList(player.getUniqueId().toString()) == null) {
                Main.plugin.gravesyaml.createSection(player.getUniqueId().toString());
                Main.plugin.gravesyaml.set(player.getUniqueId().toString(), graveitems);

            }
            else {
                @SuppressWarnings("unchecked")
                List<ItemStack> existingGrave = (List<ItemStack>) Main.plugin.gravesyaml.getList(player.getUniqueId().toString());
                existingGrave.addAll(graveitems);
                Main.plugin.gravesyaml.set(player.getUniqueId().toString(), existingGrave);
            }
            Main.plugin.unloadFiles();
        }
    }
}
