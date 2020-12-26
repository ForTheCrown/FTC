package ftc.cosmetics.inventories;


import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import ftc.cosmetics.Main;
import net.md_5.bungee.api.ChatColor;

public class DeathParticleMenu implements Listener {

	private Inventory inv;
	private String playerUUID;
	
	public DeathParticleMenu(String playerUUID) {
		CustomInventory cinv = new CustomInventory(36, "Death Effects", false, true);
		cinv.setHeadItemSlot(0);
		cinv.setReturnItemSlot(4);
		
		this.inv = cinv.getInventory();
		this.playerUUID = playerUUID;
	}
	
	
	public Inventory getInv() {
		return makeInventory();
	}


	private Inventory makeInventory() {
		Inventory result = this.inv;
		String activeDeathParticle = Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").getConfig().getString("players." + playerUUID + ".ParticleDeathActive");
		List<String> deathParticles = Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").getConfig().getStringList("players." + playerUUID + ".ParticleDeathAvailable");
		
		ItemStack noEffect = Main.plugin.makeItem(Material.BARRIER, 1, ChatColor.GOLD + "No effect", ChatColor.GRAY + "Click to go back to default dying", ChatColor.GRAY + "without any effects.");
		
		ItemStack souls = Main.plugin.makeItem(Material.GRAY_DYE, 1, ChatColor.YELLOW + "Souls", ChatColor.GRAY + "Scary souls escaping from your body.", "", ChatColor.GRAY + "Click to purchase for " + ChatColor.GOLD + "2000" + ChatColor.GRAY + " gems.");
		ItemStack totem = Main.plugin.makeItem(Material.GRAY_DYE, 1, ChatColor.YELLOW + "Faulty Totem", ChatColor.GRAY + "The particles are there, but you still die?", "", ChatColor.GRAY + "Click to purchase for " + ChatColor.GOLD + "2000" + ChatColor.GRAY + " gems.");
		ItemStack explosion = Main.plugin.makeItem(Material.GRAY_DYE, 1, ChatColor.YELLOW + "Creeper", ChatColor.GRAY + "Always wanted to know what it feels like...", "", ChatColor.GRAY + "Click to purchase for " + ChatColor.GOLD + "2000" + ChatColor.GRAY + " gems.");
		ItemStack enderRing = Main.plugin.makeItem(Material.GRAY_DYE, 1, ChatColor.YELLOW + "Ender Ring", ChatColor.GRAY + "Ender particles doing ring stuff.", ChatColor.GRAY + "Makes you scream like an enderman lol", "", ChatColor.GRAY + "Click to purchase for " + ChatColor.GOLD + "2000" + ChatColor.GRAY + " gems.");
		
		if (deathParticles.contains("SOUL")) souls = Main.plugin.makeItem(Material.ORANGE_DYE, 1, ChatColor.YELLOW + "Souls", ChatColor.GRAY + "Scary souls escaping from your body.", "", ChatColor.GRAY + "Click to use this effect.");
		if (deathParticles.contains("TOTEM")) totem = Main.plugin.makeItem(Material.ORANGE_DYE, 1, ChatColor.YELLOW + "Faulty Totem", ChatColor.GRAY + "The particles are there, but you still die?", "", ChatColor.GRAY + "Click to use this effect.");
		if (deathParticles.contains("EXPLOSION")) explosion = Main.plugin.makeItem(Material.ORANGE_DYE, 1, ChatColor.YELLOW + "Creeper", ChatColor.GRAY + "Always wanted to know what it feels like...", "", ChatColor.GRAY + "Click to use this effect.");
		if (deathParticles.contains("ENDER_RING")) enderRing = Main.plugin.makeItem(Material.ORANGE_DYE, 1, ChatColor.YELLOW + "Ender Ring", ChatColor.GRAY + "Ender particles doing ring stuff.", "", ChatColor.GRAY + "Makes you scream like an enderman lol", ChatColor.GRAY + "Click to use this effect.");
				
		if (activeDeathParticle == null) {
			Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").getConfig().set("players." + playerUUID + ".ParticleDeathActive", "none");
			Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").saveConfig();
			activeDeathParticle = "none";
		}
		
		switch (activeDeathParticle) {
		case "SOUL":
			souls.addUnsafeEnchantment(Enchantment.CHANNELING, 0);
			break;
		case "TOTEM":
			totem.addUnsafeEnchantment(Enchantment.CHANNELING, 0);
			break;
		case "EXPLOSION":
			explosion.addUnsafeEnchantment(Enchantment.CHANNELING, 0);
			break;
		case "ENDER_RING":
			enderRing.addUnsafeEnchantment(Enchantment.CHANNELING, 0);
			break;
		default:
			noEffect.addUnsafeEnchantment(Enchantment.CHANNELING, 0);
			break;
		}
		
		result.setItem(10, souls);
		result.setItem(11, totem);
		result.setItem(12, explosion);
		result.setItem(13, enderRing);
		
		result.setItem(31, noEffect);
		
		return result;
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {		
		if (event.getEntity().getType() == EntityType.PLAYER) {
			String activeDeathParticle = Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").getConfig().getString("players." + ((Player) event.getEntity()).getUniqueId().toString() + ".ParticleDeathActive");
			if (activeDeathParticle == null || activeDeathParticle == "" || activeDeathParticle.contains("none")) return;

			Location loc = event.getEntity().getLocation();
			double x = loc.getX();
			double y = loc.getY()+1;
			double z = loc.getZ();
			
			// Totem effect should have an extended effect
			if (activeDeathParticle.contains("TOTEM")) {
				for (int i = 0; i < 20; i++) {
					Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
				        @Override
				        public void run() {
				        	for (int i = 0; i < 2; i++) {
								loc.getWorld().spawnParticle(Particle.valueOf(activeDeathParticle), x, y, z, 5, 0, 0, 0, 0.4);
							}
				        }
		    		}, i*1L);
				}
				loc.getWorld().playSound(loc, Sound.ITEM_TOTEM_USE, 1, 1);
			}
			
			// Explosion effect
			else if (activeDeathParticle.contains("EXPLOSION")) {
				loc.getWorld().playEffect(loc, Effect.END_GATEWAY_SPAWN, 1);
			}
			
			// Ender-Ring effect
			else if (activeDeathParticle.contains("ENDER_RING")) {
				//loc.getWorld().playSound(loc, Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1f, 0.5f);
				loc.getWorld().playSound(loc, Sound.ENTITY_ILLUSIONER_PREPARE_BLINDNESS, 1.5f, 1f);
				double y2 = loc.getY();
				for (int i = 0; i < 3; i++) {
					loc.setY(y2+i);
		        	for (int j = 0; j < 5; j++) {
						loc.getWorld().playEffect(loc, Effect.ENDER_SIGNAL, 1);
					}
				}
			}
			else if (activeDeathParticle.contains("FLAME")) {
				loc.getWorld().playEffect(loc, Effect.ZOMBIE_INFECT, 1);
				for (int i = 0; i < 50; i++) {
					loc.getWorld().spawnParticle(Particle.valueOf(activeDeathParticle), x, y+(i/50), z, 1, 0.5, 0, 0.5, 0.05);
				}
			}
			
			// Instant particles
			else {
				for (int i = 0; i < 50; i++) {
					loc.getWorld().spawnParticle(Particle.valueOf(activeDeathParticle), x, y+(i/50), z, 1, 0.5, 0, 0.5, 0.05);
				}
			}
			
		}
	}
	
}
