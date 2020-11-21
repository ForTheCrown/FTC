package ftc.cosmetics.inventories;


import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import ftc.cosmetics.Main;
import net.md_5.bungee.api.ChatColor;

public class ArrowParticleMenu implements Listener {

	private Inventory inv;
	private String playerUUID;
	
	public ArrowParticleMenu(String playerUUID) {
		CustomInventory cinv = new CustomInventory(36, "Arrow Effects", false, true);
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
		String ActiveArrowParticle = Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").getConfig().getString("players." + playerUUID + ".ParticleArrowActive");
		List<String> ArrowParticles = Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").getConfig().getStringList("players." + playerUUID + ".ParticleArrowAvailable");
		
		ItemStack noEffect = Main.plugin.makeItem(Material.BARRIER, 1, ChatColor.GOLD + "No effect", ChatColor.GRAY + "Click to go back to default arrows", ChatColor.GRAY + "without any effects.");
		
		ItemStack flame = Main.plugin.makeItem(Material.GRAY_DYE, 1, ChatColor.YELLOW + "Flame", ChatColor.GRAY + "Works perfectly with flame arrows.", "", ChatColor.GRAY + "Click to purchase for " + ChatColor.GOLD + "1000" + ChatColor.GRAY + " gems.");
		ItemStack snow = Main.plugin.makeItem(Material.GRAY_DYE, 1, ChatColor.YELLOW + "Snowy", ChatColor.GRAY + "To stay in the Christmas spirit!", "", ChatColor.GRAY + "Click to purchase for " + ChatColor.GOLD + "1000" + ChatColor.GRAY + " gems.");
		ItemStack sneeze = Main.plugin.makeItem(Material.GRAY_DYE, 1, ChatColor.YELLOW + "Sneeze", ChatColor.GRAY + "Cover the place in that juicy snot.", "", ChatColor.GRAY + "Click to purchase for " + ChatColor.GOLD + "1000" + ChatColor.GRAY + " gems.");
		ItemStack lovetab = Main.plugin.makeItem(Material.GRAY_DYE, 1, ChatColor.YELLOW + "Cupid's Arrows", ChatColor.GRAY + "Time to do some matchmaking...", "", ChatColor.GRAY + "Click to purchase for " + ChatColor.GOLD + "1000" + ChatColor.GRAY + " gems.");
		ItemStack evillove = Main.plugin.makeItem(Material.GRAY_DYE, 1, ChatColor.YELLOW + "Cupid's Evil Twin", ChatColor.GRAY + "Time to undo some matchmaking...", "", ChatColor.GRAY + "Click to purchase for " + ChatColor.GOLD + "1000" + ChatColor.GRAY + " gems.");
		ItemStack honeytrail = Main.plugin.makeItem(Material.GRAY_DYE, 1, ChatColor.YELLOW + "Sticky Trail", ChatColor.GRAY + "For those who enjoy looking at the trail lol", "", ChatColor.GRAY + "Click to purchase for " + ChatColor.GOLD + "1000" + ChatColor.GRAY + " gems.");
		ItemStack smoke = Main.plugin.makeItem(Material.GRAY_DYE, 1, ChatColor.YELLOW + "Smoke", ChatColor.GRAY + "Pretend to be a cannon.", "", ChatColor.GRAY + "Click to purchase for " + ChatColor.GOLD + "1000" + ChatColor.GRAY + " gems.");
		
		ItemStack souls = Main.plugin.makeItem(Material.GRAY_DYE, 1, ChatColor.YELLOW + "Souls", ChatColor.GRAY + "Scary souls escaping from your arrow.", "", ChatColor.GRAY + "Click to purchase for " + ChatColor.GOLD + "1000" + ChatColor.GRAY + " gems.");
		ItemStack firework = Main.plugin.makeItem(Material.GRAY_DYE, 1, ChatColor.YELLOW + "Firework", ChatColor.GRAY + "Almost as if you're using a crossbow.", "", ChatColor.GRAY + "Click to purchase for " + ChatColor.GOLD + "1000" + ChatColor.GRAY + " gems.");
		
		if (ArrowParticles.contains("FLAME")) flame = Main.plugin.makeItem(Material.ORANGE_DYE, 1, ChatColor.YELLOW + "Flame", ChatColor.GRAY + "Works perfectly with flame arrows.", "", ChatColor.GRAY + "Click to use this effect.");
		if (ArrowParticles.contains("SNOWBALL")) snow = Main.plugin.makeItem(Material.ORANGE_DYE, 1, ChatColor.YELLOW + "Snowy", ChatColor.GRAY + "To stay in the Christmas spirit!", "", ChatColor.GRAY + "Click to use this effect.");
		if (ArrowParticles.contains("SNEEZE")) sneeze = Main.plugin.makeItem(Material.ORANGE_DYE, 1, ChatColor.YELLOW + "Sneeze", ChatColor.GRAY + "Cover the place in that juicy snot.", "", ChatColor.GRAY + "Click to use this effect.");
		if (ArrowParticles.contains("HEART")) lovetab = Main.plugin.makeItem(Material.ORANGE_DYE, 1, ChatColor.YELLOW + "Cupid's Arrows", ChatColor.GRAY + "Time to do some matchmaking...", "", ChatColor.GRAY + "Click to use this effect.");
		if (ArrowParticles.contains("DAMAGE_INDICATOR")) evillove = Main.plugin.makeItem(Material.ORANGE_DYE, 1, ChatColor.YELLOW + "Cupid's Evil Twin", ChatColor.GRAY + "Time to undo some matchmaking...", "", ChatColor.GRAY + "Click to use this effect.");
		if (ArrowParticles.contains("DRIPPING_HONEY")) honeytrail = Main.plugin.makeItem(Material.ORANGE_DYE, 1, ChatColor.YELLOW + "Sticky Trail", ChatColor.GRAY + "For those who enjoy looking at the trail lol", "", ChatColor.GRAY + "Click to use this effect.");
		if (ArrowParticles.contains("CAMPFIRE_COSY_SMOKE")) smoke = Main.plugin.makeItem(Material.ORANGE_DYE, 1, ChatColor.YELLOW + "Smoke", ChatColor.GRAY + "Pretend to be a cannon.", "", ChatColor.GRAY + "Click to use this effect.");
		
		if (ArrowParticles.contains("SOUL")) souls = Main.plugin.makeItem(Material.ORANGE_DYE, 1, ChatColor.YELLOW + "Souls", ChatColor.GRAY + "Scary souls escaping from your arrow.", "", ChatColor.GRAY + "Click to use this effect.");
		if (ArrowParticles.contains("FIREWORKS_SPARK")) firework = Main.plugin.makeItem(Material.ORANGE_DYE, 1, ChatColor.YELLOW + "Firework", ChatColor.GRAY + "Almost as if you're using a crossbow.", "", ChatColor.GRAY + "Click to use this effect.");
		
		if (ActiveArrowParticle == null) {
			Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").getConfig().set("players." + playerUUID + ".ParticleArrowActive", "none");
			Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").saveConfig();
			ActiveArrowParticle = "none";
		}
		
		switch (ActiveArrowParticle) {
		case "FLAME":
			flame.addUnsafeEnchantment(Enchantment.CHANNELING, 0);
			break;
		case "SNOWBALL":
			snow.addUnsafeEnchantment(Enchantment.CHANNELING, 0);
			break;
		case "SNEEZE":
			sneeze.addUnsafeEnchantment(Enchantment.CHANNELING, 0);
			break;
		case "HEART":
			lovetab.addUnsafeEnchantment(Enchantment.CHANNELING, 0);
			break;
		case "DAMAGE_INDICATOR":
			evillove.addUnsafeEnchantment(Enchantment.CHANNELING, 0);
			break;
		case "DRIPPING_HONEY":
			honeytrail.addUnsafeEnchantment(Enchantment.CHANNELING, 0);
			break;
		case "CAMPFIRE_COSY_SMOKE":
			smoke.addUnsafeEnchantment(Enchantment.CHANNELING, 0);
			break;
		case "SOUL":
			souls.addUnsafeEnchantment(Enchantment.CHANNELING, 0);
			break;
		case "FIREWORKS_SPARK":
			firework.addUnsafeEnchantment(Enchantment.CHANNELING, 0);
			break;
		default:
			noEffect.addUnsafeEnchantment(Enchantment.CHANNELING, 0);
			break;
		}
		
		result.setItem(10, flame);
		result.setItem(11, snow);
		result.setItem(12, sneeze);
		result.setItem(13, lovetab);
		result.setItem(14, evillove);
		result.setItem(15, honeytrail);
		result.setItem(16, smoke);
		
		result.setItem(19, souls);
		result.setItem(20, firework);
		
		result.setItem(31, noEffect);
		
		return result;
	}
	
	@EventHandler
	public void onPlayerShootsBow(EntityShootBowEvent event) {
		if (event.getEntity().getType() == EntityType.PLAYER) {
			String ActiveArrowParticle = Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").getConfig().getString("players." + ((Player) event.getEntity()).getUniqueId().toString() + ".ParticleArrowActive");
			if (ActiveArrowParticle == null || ActiveArrowParticle == "" || ActiveArrowParticle.contains("none")) return;
			
			double speed = 0;
			if (ActiveArrowParticle.contains("FIREWORKS_SPARK")) speed = 0.1;
			else if (ActiveArrowParticle.contains("CAMPFIRE_COSY_SMOKE")) speed = 0.005;
			else if (ActiveArrowParticle.contains("SNOWBALL")) speed = 0.1;
			
			try {addParticleToArrow(event.getProjectile(), Particle.valueOf(ActiveArrowParticle), speed);}
			catch (Exception ignored) {}
		}
	}
	
	private void addParticleToArrow(Entity projectile, Particle particle, double speed) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
	        @Override
	        public void run() {
	        	projectile.getWorld().spawnParticle(particle, projectile.getLocation(), 1, 0, 0, 0, speed);
	        	if (!(projectile.isOnGround() || projectile.isDead() || projectile == null)) addParticleToArrow(projectile, particle, speed);
	        }
	    }, 1);
		
	}
}
