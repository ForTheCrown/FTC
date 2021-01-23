package ftc.cosmetics.inventories;


import ftc.cosmetics.Main;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.files.FtcUser;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class DeathParticleMenu implements Listener {

	private Inventory inv;
	private FtcUser user;
	
	public DeathParticleMenu(FtcUser user) {
		CustomInventory cinv = new CustomInventory(36, "Death Effects", false, true);
		cinv.setHeadItemSlot(0);
		cinv.setReturnItemSlot(4);
		
		this.inv = cinv.getInventory();
		this.user = user;
	}
	
	
	public Inventory getInv() {
		return makeInventory();
	}


	private Inventory makeInventory() {
		Inventory result = this.inv;
		
		ItemStack noEffect = FtcCore.makeItem(Material.BARRIER, 1, true, ChatColor.GOLD + "No effect", ChatColor.GRAY + "Click to go back to default dying", ChatColor.GRAY + "without any effects.");
		
		ItemStack souls = getEffectItem("SOUL", "&eSouls", ChatColor.GRAY + "Scary souls escaping from your body.", "", ChatColor.GRAY + "Click to purchase for " + ChatColor.GOLD + "2000" + ChatColor.GRAY + " gems.");
		ItemStack totem = getEffectItem("TOTEM", "&eFaulty Totem", ChatColor.GRAY + "The particles are there, but you still die?", "", ChatColor.GRAY + "Click to purchase for " + ChatColor.GOLD + "2000" + ChatColor.GRAY + " gems.");
		ItemStack explosion = getEffectItem("EXPLOSION", ChatColor.YELLOW + "Creeper", ChatColor.GRAY + "Always wanted to know what it feels like...", "", ChatColor.GRAY + "Click to purchase for " + ChatColor.GOLD + "2000" + ChatColor.GRAY + " gems.");
		ItemStack enderRing = getEffectItem("ENDER_RING", ChatColor.YELLOW + "Ender Ring", ChatColor.GRAY + "Ender particles doing ring stuff.", ChatColor.GRAY + "Makes you scream like an enderman lol", "", ChatColor.GRAY + "Click to purchase for " + ChatColor.GOLD + "2000" + ChatColor.GRAY + " gems.");
		
		result.setItem(10, souls);
		result.setItem(11, totem);
		result.setItem(12, explosion);
		result.setItem(13, enderRing);
		
		result.setItem(31, noEffect);
		
		return result;
	}

	private ItemStack getEffectItem(String effect, String name, String... desc){
		ItemStack dumb = FtcCore.makeItem(Material.GRAY_DYE, 1, true, name, desc);

		if(user.getDeathParticle() != null && user.getDeathParticle().contains(effect)) dumb.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);
		if(user.getParticleDeathAvailable().contains(effect)) dumb.setType(Material.ORANGE_DYE);

		return dumb;
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
