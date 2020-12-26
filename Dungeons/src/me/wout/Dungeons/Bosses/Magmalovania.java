package me.wout.Dungeons.Bosses;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.SlimeSplitEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.util.Vector;

import me.wout.Dungeons.main;
import net.md_5.bungee.api.ChatColor;

public class Magmalovania implements Listener {

	private main plugin;
	public Set<UUID> magmalovanias = new HashSet<UUID>();
	private Set<UUID> lilcubes = new HashSet<UUID>();
	BossBar bossbar;
	private String spawnPlayer;
	private int currentSize;
	
	public Magmalovania(main plugin) {
		this.plugin = plugin;
	}
	
	
	public void summonMagmalovania(Location loc, String playerName) {
		this.spawnPlayer = playerName;
		loc.setY(loc.getY()+8);
		MagmaCube magmalovania = loc.getWorld().spawn(loc, MagmaCube.class);
		magmalovania.setCustomName(ChatColor.RED + "Magmalovania");
		magmalovania.setCustomNameVisible(true);
		magmalovania.setRemoveWhenFarAway(false);
		magmalovania.setPersistent(true);

		magmalovania.setSize(4);
		//Bukkit.broadcastMessage("health: " + magmalovania.getHealth() + ", maxhealth: " + magmalovania.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue()); Health blijft 16 voor een of andere reden.
		
		magmalovanias.add(magmalovania.getUniqueId());
		createBossBar(loc);
		lilcubes.clear();
		startCycle(magmalovania);
		currentSize = 4;
		
		//startCycle(drawned.getLocation(), drawned, drawned.getUniqueId());
		//guardians.clear();
	}
	
	
	private void spawnLilCube(Location loc, int size, double health) {
		if (size == 0) return;
		MagmaCube mag = loc.getWorld().spawn(loc, MagmaCube.class);
		mag.setSize(size);
		mag.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
		mag.setHealth(health);
		lilcubes.add(mag.getUniqueId());
	}
	

	private void startCycle(MagmaCube mg) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
	        @Override
	        public void run() {
	        	mg.damage(mg.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue()/10);
	        	if (mg.isDead()) return;
	        	spawnLilCube(mg.getLocation(), getRandomNumberInRange(1, mg.getSize()-3), mg.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue()/20);
	        	startCycle(mg);
	        }
	    }, 100L);
		
	}


	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled = true)
    public void onMobDeath(EntityDeathEvent event){
        if (event.getEntity() instanceof MagmaCube) {
            if(magmalovanias.contains(event.getEntity().getUniqueId())) {
            	magmalovanias.remove(event.getEntity().getUniqueId());

                if (bossbar != null) {
                	bossbar.removeAll();
                    bossbar.setVisible(false);
                    bossbar = null;
                }
                
                Location loc = event.getEntity().getLocation();
                Location deleteLoc = new Location(loc.getWorld(), loc.getX(), loc.getY()-500, loc.getZ());
                for (UUID lilcube : lilcubes)
                {
                	if (Bukkit.getEntity(lilcube) == null || Bukkit.getEntity(lilcube).isDead()) continue; 
                	loc.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, ((MagmaCube) Bukkit.getEntity(lilcube)).getLocation().getX(), ((MagmaCube) Bukkit.getEntity(lilcube)).getLocation().getY()+0.5, ((MagmaCube) Bukkit.getEntity(lilcube)).getLocation().getZ(), 1, 0.0, 0.6, 0.0, 0.01);
                	((MagmaCube) Bukkit.getEntity(lilcube)).teleport(deleteLoc);
                }

                Objective scoreboardobj = Bukkit.getScoreboardManager().getMainScoreboard().getObjective(DisplaySlot.PLAYER_LIST);
				if (!scoreboardobj.getName().equalsIgnoreCase(plugin.getConfig().getString("Scoreboard"))) {
					return;
				}
				Score score = null;
				try {
					score = scoreboardobj.getScore(Bukkit.getPlayer(spawnPlayer));
					Bukkit.getPlayer(spawnPlayer).sendMessage(ChatColor.GRAY + "Magmalovania died! It had a size of " + currentSize + ".");
					loc.getWorld().playSound(loc, Sound.ENTITY_ZOMBIE_VILLAGER_DEATH, 1, 0.5f);
				} catch (Exception e) {
					score = scoreboardobj.getScore(Bukkit.getOfflinePlayer(spawnPlayer));
				}
				if (score.getScore() < currentSize) {
					score.setScore(currentSize);
				}
				
				currentSize = 0;
				spawnPlayer = null;

            }
		    else if (lilcubes.contains(event.getEntity().getUniqueId())) 
		    {
		    	if (((MagmaCube) event.getEntity()).getSize() == 1) 
		    	{
		    		lilcubes.remove(event.getEntity().getUniqueId());
		    	}
		        event.getDrops().clear();
		        event.getDrops().add(new ItemStack(Material.MAGMA_CREAM, 1));
		    }
        }
    }
	
	@EventHandler
	public void magmaSplit(SlimeSplitEvent event) {
		if (event.getEntity() instanceof MagmaCube) 
		{
			if (event.getEntity().getName().contains(ChatColor.RED + "Magmalovania")) 
			{
				event.setCancelled(true);
			}
			else if (lilcubes.contains(event.getEntity().getUniqueId()))
			{
				if (((MagmaCube) event.getEntity()).getSize() >= 3) 
				{
					spawnLilCube(event.getEntity().getLocation(), ((MagmaCube) event.getEntity()).getSize()-getRandomNumberInRange(1, ((MagmaCube) event.getEntity()).getSize()-1), event.getEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue()/2);
				}
				lilcubes.remove(event.getEntity().getUniqueId());
				event.setCancelled(true);
			}
		}
	}
	
	
	@EventHandler(ignoreCancelled = true)
	public void onMagmalovaniaHit(EntityDamageEvent event) {
		if (magmalovanias.contains(event.getEntity().getUniqueId())) {
			updateBossBar();
		}
	}
	
	private void updateBossBar() {
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
	        public void run() {
	        	if (magmalovanias.isEmpty() || magmalovanias.size() > 1 || bossbar == null) return;
	        	MagmaCube mg = null;
	        	for (UUID uuid : magmalovanias)
	    		{
	    			mg = (MagmaCube) Bukkit.getEntity(uuid);
	    		}
	        	bossbar.setProgress(mg.getHealth() / mg.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
	        }
	    }, 2L);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerClick(PlayerInteractEntityEvent event) {
		if(!event.getHand().equals(EquipmentSlot.HAND))
			return;
		if (magmalovanias.contains(event.getRightClicked().getUniqueId())) {
			if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.MAGMA_CREAM) 
			{
				MagmaCube mg = (MagmaCube) event.getRightClicked();
				try {
					mg.setHealth(mg.getHealth() + mg.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue()/5);
				} catch (Exception e) {
					upgradeSize(mg);
				}
				updateBossBar();
				event.getPlayer().getInventory().getItemInMainHand().setAmount(event.getPlayer().getInventory().getItemInMainHand().getAmount()-1);
				event.getPlayer().playSound(mg.getLocation(), Sound.ENTITY_GENERIC_EAT, 1f, 0.75f);
			}
		}	
		
	}
	
	private void upgradeSize(MagmaCube mg) {
		mg.setSize(mg.getSize()+1);
		mg.setHealth(mg.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
		mg.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(mg.getHealth()*2);
		updateBossBar();
		mg.setVelocity(new Vector(0.0, 1.0, 0.0));
		mg.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, mg.getLocation().getX(), mg.getLocation().getY()+mg.getHeight()/2, mg.getLocation().getZ(), 100, mg.getSize()/2, mg.getSize()/2, mg.getSize()/2);
		currentSize++;
		
		double chance = Math.random();
		if (chance > 0.5) {
			Bukkit.dispatchCommand(plugin.getServer().getConsoleSender(), "crate givekey " + spawnPlayer + " crate1 1");
		}
	}


	public void createBossBar(Location loc) {
		if (bossbar != null) return;
		bossbar = Bukkit.createBossBar(ChatColor.RED + "Magmalovania", BarColor.RED, BarStyle.SEGMENTED_12);
		bossbar.setProgress(1.0);
		
		List<Entity> nearbyEntities = (List<Entity>) loc.getWorld().getNearbyEntities(loc, 30, 80, 30);
		for (Entity ent : nearbyEntities) {
			if (ent instanceof Player) {
				bossbar.addPlayer((Player) ent);
			}
		}
		bossbar.setVisible(true);
	}
	
	private static int getRandomNumberInRange(int min, int max) {
		if (min >= max) {
			return 1;
		}
		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}
}
