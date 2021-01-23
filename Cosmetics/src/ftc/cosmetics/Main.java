package ftc.cosmetics;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.files.FtcUser;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.*;
import org.spigotmc.event.entity.EntityDismountEvent;

import ftc.cosmetics.commands.AddParticle;
import ftc.cosmetics.commands.Cosmetics;
import ftc.cosmetics.commands.RemoveParticle;
import ftc.cosmetics.inventories.ArrowParticleMenu;
import ftc.cosmetics.inventories.CustomInventory;
import ftc.cosmetics.inventories.DeathParticleMenu;
import ftc.cosmetics.inventories.EmoteMenu;
import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin implements Listener {
	
	public static Main plugin;
	public static InventoryHolder holder;
	
	public int option = 0;
	
	public void onEnable() {
		plugin = this;
		holder = () -> null;
		
		
		// Config
		getConfig().options().copyDefaults(true);
		saveDefaultConfig();
		
		// Events
		getServer().getPluginManager().registerEvents(this, this);
		getServer().getPluginManager().registerEvents(new ArrowParticleMenu(null), this);
		getServer().getPluginManager().registerEvents(new DeathParticleMenu(null), this);
		
		// Commands
		getServer().getPluginCommand("cosmetics").setExecutor(new Cosmetics());
		
		getServer().getPluginCommand("addparticle").setExecutor(new AddParticle());
		getServer().getPluginCommand("removeparticle").setExecutor(new RemoveParticle());
	}
	
	Set<String> onCooldown = new HashSet<>();
	
	@EventHandler
    public void playerRightClickPlayer(PlayerInteractEntityEvent event) {
		if (event.getHand() == EquipmentSlot.OFF_HAND) return;
		// RightClick player with empty hand
        if (event.getRightClicked() instanceof Player && event.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR) {
            Player p2 = (Player) event.getRightClicked();
            Player player = event.getPlayer();

            FtcUser rider = FtcCore.getUser(player.getUniqueId());
            FtcUser ridden = FtcCore.getUser(p2.getUniqueId());

			// Both players allow riding:
            if(!rider.allowsRidingPlayers() || !ridden.allowsRidingPlayers()){
				player.sendMessage(ChatColor.GRAY + "You both have to enable riding other players");
				return;
			}
            if (onCooldown.contains(player.getUniqueId().toString())) return;

            // Stop lower player from trying to get on player on top of him:
			if (p2.isInsideVehicle() && isSlimySeat(p2.getVehicle()) && p2.getVehicle().isInsideVehicle() && p2.getVehicle().getVehicle() == player) return;
			else if (p2.getPassengers().isEmpty()) {
				Location loc = p2.getLocation();
				loc.setY(-10);
				Slime slime = p2.getWorld().spawn(loc, Slime.class);
				slime.setInvisible(true);
				slime.setSize(1);
				slime.setInvulnerable(true);
				slime.setSilent(true);
				slime.setAI(false);
				slime.addPassenger(player);
				slime.setCustomName(ChatColor.GREEN + "slimy");

				p2.addPassenger(slime);

				onCooldown.add(player.getUniqueId().toString());
				Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> onCooldown.remove(player.getUniqueId().toString()), 10);
    		}
        }
    }
	
	@EventHandler
	public void playerDismountFromPlayer(EntityDismountEvent event) {
		if (event.getEntity() instanceof Player) {
			Entity mount = event.getDismounted();
			if (isSlimySeat(mount)) {
				// Stop riding stuff
				mount.leaveVehicle();
				
				// Stop carrying stuff
				mount.eject();
				mount.remove();
			}
		}
	}
	
	@EventHandler
	public void playerCarryingPlayerLogout(PlayerQuitEvent event) {
		// Player leaves with something riding him:
		event.getPlayer().eject();
		Location loc = event.getPlayer().getLocation();
		for (Entity nearbyEntity : loc.getWorld().getNearbyEntities(loc, 0.1, 2, 0.1)) {
			if (isSlimySeat(nearbyEntity)) {
				nearbyEntity.eject();
				nearbyEntity.remove();
			}
		}
		// Player leaves while riding something
		if (event.getPlayer().isInsideVehicle()) {
			if (isSlimySeat(event.getPlayer().getVehicle())) {
				event.getPlayer().getVehicle().remove();
				event.getPlayer().leaveVehicle();
			}
		}
	}
	
	@EventHandler
	public void playerCarryingPlayerDies(PlayerDeathEvent event) {
		// Player leaves with something riding him:
		event.getEntity().eject();
		Location loc = event.getEntity().getLocation();
		for (Entity nearbyEntity : loc.getWorld().getNearbyEntities(loc, 0.1, 2, 0.1)) {
			if (isSlimySeat(nearbyEntity)) {
				nearbyEntity.eject();
				nearbyEntity.remove();
			}
		}
		// Player leaves while riding something
		if (event.getEntity().isInsideVehicle()) {
			if (isSlimySeat(event.getEntity().getVehicle())) {
				event.getEntity().getVehicle().remove();
				event.getEntity().leaveVehicle();
			}
		}
	}
	
	public boolean isSlimySeat(Entity entity) {
		return (entity.getType() == EntityType.SLIME
				&& entity.getCustomName() != null
				&& entity.getCustomName().contains(ChatColor.GREEN + "slimy"));
	}
	
	@EventHandler
	public void playerLaunchOtherPlayer(PlayerInteractEntityEvent event) {
		if (event.getHand().equals(EquipmentSlot.HAND)) {
			if ((!event.getPlayer().getPassengers().isEmpty()) && event.getPlayer().isSneaking() && event.getPlayer().getLocation().getPitch() <= (-75)) {	
				//Set<Entity> playerPassengers = new HashSet<>();
				for (Entity passenger : event.getPlayer().getPassengers()) {
					if (isSlimySeat(passenger)) {
						passenger.leaveVehicle();
						passenger.eject();
						passenger.remove();
					}
				}
				event.getPlayer().eject();
			}
		}	
	}
	
	@EventHandler
	public void onPlayerClickItemInInv(InventoryClickEvent event) {
		if (event.getInventory().getHolder() == holder) {
			event.setCancelled(true);
			if (event.getClickedInventory() instanceof PlayerInventory) return;
			
			Player player = (Player) event.getWhoClicked();
			FtcUser user = FtcCore.getUser(player.getUniqueId());
			int slot = event.getSlot();
			String title = event.getView().getTitle();
			
			if (title.contains("osmetics")) 
			{
				switch (slot) {
				case 20:
					player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
					ArrowParticleMenu apm = new ArrowParticleMenu(user);
					player.openInventory(apm.getInv());
					break;
				case 22:
					player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
					EmoteMenu em = new EmoteMenu(user);
					player.openInventory(em.getInv());
					break;
				case 24:
					player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
					DeathParticleMenu dpm = new DeathParticleMenu(user);
					player.openInventory(dpm.getInv());
					break;
				case 40:
					player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
					if(user.allowsRidingPlayers()) user.setAllowsRidingPlayers(false);
					else user.setAllowsRidingPlayers(true);
					player.openInventory(getMainCosmeticInventory(user));
				}
			}
			
			else if (title.contains("Arrow Effects")) 
			{
				ArrowParticleMenu apm = new ArrowParticleMenu(user);
				
				switch (slot) {
				case 4:
					player.openInventory(getMainCosmeticInventory(user));
					break;
				case 10:
					if (!setEffectIfOwned(playeruuid, event.getInventory(), slot, ".ParticleArrowActive", "FLAME")) {
						if (tryBuyEffect(event.getClickedInventory().getItem(slot), player, "arrow", "FLAME")) {
							player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
							setEffect(playeruuid, ".ParticleArrowActive", "FLAME");
						}
					}
					player.openInventory(apm.getInv());
					break;
				case 11:
					if (!setEffectIfOwned(playeruuid, event.getInventory(), slot, ".ParticleArrowActive", "SNOWBALL")) {
						if (tryBuyEffect(event.getClickedInventory().getItem(slot), player, "arrow", "SNOWBALL")) {
							player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
							setEffect(playeruuid, ".ParticleArrowActive", "SNOWBALL");
						}
					}
					player.openInventory(apm.getInv());
					break;
				case 12:
					if (!setEffectIfOwned(playeruuid, event.getInventory(), slot, ".ParticleArrowActive", "SNEEZE")) {
						if (tryBuyEffect(event.getClickedInventory().getItem(slot), player, "arrow", "SNEEZE")) {
							player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
							setEffect(playeruuid, ".ParticleArrowActive", "SNEEZE");
							player.openInventory(apm.getInv());
						}
					}
					player.openInventory(apm.getInv());
					break;
				case 13:
					if (!setEffectIfOwned(playeruuid, event.getInventory(), slot, ".ParticleArrowActive", "HEART")) {
						if (tryBuyEffect(event.getClickedInventory().getItem(slot), player, "arrow", "HEART")) {
							player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
							setEffect(playeruuid, ".ParticleArrowActive", "HEART");
							player.openInventory(apm.getInv());
						}
					}
					player.openInventory(apm.getInv());
					break;
				case 14:
					if (!setEffectIfOwned(playeruuid, event.getInventory(), slot, ".ParticleArrowActive", "DAMAGE_INDICATOR")) {
						if (tryBuyEffect(event.getClickedInventory().getItem(slot), player, "arrow", "DAMAGE_INDICATOR")) {
							player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
							setEffect(playeruuid, ".ParticleArrowActive", "DAMAGE_INDICATOR");
							player.openInventory(apm.getInv());
						}
					}
					player.openInventory(apm.getInv());
					break;
				case 15:
					if (!setEffectIfOwned(playeruuid, event.getInventory(), slot, ".ParticleArrowActive", "DRIPPING_HONEY")) {
						if (tryBuyEffect(event.getClickedInventory().getItem(slot), player, "arrow", "DRIPPING_HONEY")) {
							player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
							setEffect(playeruuid, ".ParticleArrowActive", "DRIPPING_HONEY");
							player.openInventory(apm.getInv());
						}
					}
					player.openInventory(apm.getInv());
					break;
				case 16:
					if (!setEffectIfOwned(playeruuid, event.getInventory(), slot, ".ParticleArrowActive", "CAMPFIRE_COSY_SMOKE")) {
						if (tryBuyEffect(event.getClickedInventory().getItem(slot), player, "arrow", "CAMPFIRE_COSY_SMOKE")) {
							player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
							setEffect(playeruuid, ".ParticleArrowActive", "CAMPFIRE_COSY_SMOKE");
							player.openInventory(apm.getInv());
						}
					}
					player.openInventory(apm.getInv());
					break;
				case 19:
					if (!setEffectIfOwned(playeruuid, event.getInventory(), slot, ".ParticleArrowActive", "SOUL")) {
						if (tryBuyEffect(event.getClickedInventory().getItem(slot), player, "arrow", "SOUL")) {
							player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
							setEffect(playeruuid, ".ParticleArrowActive", "SOUL");
							player.openInventory(apm.getInv());
						}
					}
					player.openInventory(apm.getInv());
					break;
				case 20:
					if (!setEffectIfOwned(playeruuid, event.getInventory(), slot, ".ParticleArrowActive", "FIREWORKS_SPARK")) {
						if (tryBuyEffect(event.getClickedInventory().getItem(slot), player, "arrow", "FIREWORKS_SPARK")) {
							player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
							setEffect(playeruuid, ".ParticleArrowActive", "FIREWORKS_SPARK");
						}
					}
					player.openInventory(apm.getInv());
					break;
				case 31:
					user.setArrowParticle(null);
					player.openInventory(apm.getInv());
					break;
				default:
					return;
				}
				
				player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
				Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").saveConfig();
			}
			
			else if (title.contains("Death Effects"))
			{
				DeathParticleMenu dpm = new DeathParticleMenu(user);
				
				switch (slot) {
				case 4:
					player.openInventory(getMainCosmeticInventory(user));
					break;
				case 10:
					//they own it
					if(user.getParticleDeathAvailable().contains("SOUL")){
						user.setDeathParticle("SOUL");
						player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
					} else {
						//TODO Attempt purchase
					}
					player.openInventory(dpm.getInv());
					break;
				case 11:
					if (!setEffectIfOwned(playeruuid, event.getInventory(), slot, ".ParticleDeathActive", "TOTEM")) {
						if (tryBuyEffect(event.getClickedInventory().getItem(slot), player, "death", "TOTEM")) {
							player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
							setEffect(playeruuid, ".ParticleDeathActive", "TOTEM");
						}
					}
					player.openInventory(dpm.getInv());
					break;
				case 12:
					if (!setEffectIfOwned(playeruuid, event.getInventory(), slot, ".ParticleDeathActive", "EXPLOSION")) {
						if (tryBuyEffect(event.getClickedInventory().getItem(slot), player, "death", "EXPLOSION")) {
							player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
							setEffect(playeruuid, ".ParticleDeathActive", "EXPLOSION");
						}
					}
					player.openInventory(dpm.getInv());
					break;
				case 13:
					if (!setEffectIfOwned(playeruuid, event.getInventory(), slot, ".ParticleDeathActive", "ENDER_RING")) {
						if (tryBuyEffect(event.getClickedInventory().getItem(slot), player, "death", "ENDER_RING")) {
							player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
							setEffect(playeruuid, ".ParticleDeathActive", "ENDER_RING");
						}
					}
					player.openInventory(dpm.getInv());
					break;
				case 31:
					user.setDeathParticle("none");
					player.openInventory(dpm.getInv());
					break;
				default:
					return;
				}
				
				player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
				Main.plugin.getServer().getPluginManager().getPlugin("DataPlugin").saveConfig();
			}
			
			else if (title.contains("Emotes"))
			{
				EmoteMenu em = new EmoteMenu(user);
				
				switch(slot) {
				case 4: 
					player.openInventory(getMainCosmeticInventory(user));
					break;
				case 31:
					Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "sudo " + player.getName() + " toggleemotes");
					Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> player.openInventory(em.getInv()), 3);
					break;
				default:
					return;
				}
				
				player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
			}
		}
	}

	public Inventory getMainCosmeticInventory(FtcUser user) {
		CustomInventory cinv = new CustomInventory(54, ChatColor.BOLD + "C" + ChatColor.RESET + "osmetics", true, false);
		Inventory inv = cinv.getInventory();
		
		inv.setItem(20, FtcCore.makeItem(Material.BOW, 1, true, ChatColor.YELLOW + "Arrow Particle Trails", "", ChatColor.GRAY + "Upgrade your arrows with fancy particle", ChatColor.GRAY + "trails as they fly through the air!"));
		inv.setItem(22, FtcCore.makeItem(Material.TOTEM_OF_UNDYING, 1, true, ChatColor.YELLOW + "Emotes", "", ChatColor.GRAY + "Poking, smooching, bonking and more", ChatColor.GRAY + "to interact with your friends."));
		inv.setItem(24, FtcCore.makeItem(Material.SKELETON_SKULL, 1, true, ChatColor.YELLOW + "Death Particles", "", ChatColor.GRAY + "Make your deaths more spectacular by", ChatColor.GRAY + "exploding into pretty particles!"));
		
		if (user.allowsRidingPlayers()) {
			inv.setItem(40, FtcCore.makeItem(Material.SADDLE, 1, true,ChatColor.YELLOW + "You can ride other players!", "",
				ChatColor.GRAY + "Right-click someone to jump on top of them.", 
				ChatColor.GRAY + "Shift-Right-click someone to kick them off.", "",
				ChatColor.GRAY + "Click to disable this feature."));
		}
		else {
			inv.setItem(40, FtcCore.makeItem(Material.BARRIER, 1, true, ChatColor.YELLOW + "You've disabled riding other players.", "",
					ChatColor.GRAY + "Right-click someone to jump on top of them.", 
					ChatColor.GRAY + "Shift-Right-click someone to kick them off.", "",
					ChatColor.GRAY + "Click to enable this feature."));
		}

		int gems = user.getGems();
		try {
			ItemStack item = inv.getItem(cinv.getHeadItemSlot());
			ItemMeta meta = item.getItemMeta();
			List<String> lore = meta.getLore();
			lore.set(1, ChatColor.GRAY + "You have " + ChatColor.GOLD + gems + ChatColor.GRAY + " Gems.");
			meta.setLore(lore);
			item.setItemMeta(meta);
			inv.setItem(cinv.getHeadItemSlot(), item);
		} catch (Exception ignored) {}
		
		return inv;
	}
	
	public Set<Particle> getAcceptedArrowParticles() {
		return new HashSet<>(Arrays.asList(
				Particle.FLAME, Particle.SNOWBALL, Particle.SNEEZE, Particle.HEART, Particle.DAMAGE_INDICATOR, Particle.DRIPPING_HONEY, Particle.CAMPFIRE_COSY_SMOKE, Particle.SOUL, Particle.FIREWORKS_SPARK));
	}
	
	public Set<String> getAcceptedDeathParticles() {
		return new HashSet<>(
				Arrays.asList("SOUL", "TOTEM", "EXPLOSION", "ENDER_RING"));
	}
	
	public Set<String> getAcceptedEmotes() {
		return new HashSet<>(
				Arrays.asList("scare", "jingle"));
	}
}