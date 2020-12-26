package me.wout.Dungeons.Bosses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.wout.Dungeons.main;
import net.md_5.bungee.api.ChatColor;

public class Skalatan implements Listener {

	private main plugin;
	private Set<UUID> skalatans = new HashSet<UUID>();
	Location spawnloc;
	private Map<UUID, Boolean> wieldingBow = new HashMap<UUID, Boolean>();
	BossBar bossbar;
	
	public Skalatan(main plugin) {
		this.plugin = plugin;
	}
	
	public void summonSkalatan(Location loc) {
		loc.setY(loc.getY()+2);
		WitherSkeleton skalatan = loc.getWorld().spawn(loc, WitherSkeleton.class);
		skalatan.setCustomName(ChatColor.YELLOW + "Skalatan");
		skalatan.setCustomNameVisible(true);
		skalatan.setRemoveWhenFarAway(false);
		skalatan.setPersistent(true);
		skalatans.add(skalatan.getUniqueId());

		skalatan.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(300);
		skalatan.setHealth(300);
		skalatan.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(25.0);
		skalatan.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(1.0F);
		skalatan.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(10.0);
		skalatan.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.305F);;
		
		skalatan.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999, 1, false, false));
		
		createBossBar(loc);
		wieldingBow.put(skalatan.getUniqueId(), false);
		skalatanSwapping(skalatan, skalatan.getUniqueId());
	}
	
	private void skalatanSwapping(WitherSkeleton skalatan, UUID uuid) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
	        @Override
	        public void run() {
	        	if (skalatans.contains(uuid)) {
	        		
	        		if (wieldingBow.get(uuid)) {
	        			skalatan.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD, 1));
	        			wieldingBow.replace(uuid, false);
	        		}
	        		else {
	        			skalatan.getEquipment().setItemInMainHand(new ItemStack(Material.BOW, 1));
	        			wieldingBow.replace(uuid, true);
	        		}
	        		
	        		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
	        	        @Override
	        	        public void run() {
	    	        		skalatanSwapping(skalatan, uuid);
	        	        }
	        	    }, 100L);
	        	}
	        }
	    }, 100L);
		
	}

	@EventHandler(ignoreCancelled = true)
    public void onMobDeath(EntityDeathEvent event){
        if(event.getEntity() instanceof WitherSkeleton){
            if(skalatans.contains(event.getEntity().getUniqueId())) {
            	skalatans.remove(event.getEntity().getUniqueId());
            	event.getDrops().clear();
                
            	ItemStack item = new ItemStack(Material.GOLDEN_APPLE, 1);
            	ItemMeta itemMeta = item.getItemMeta();
            	List<String> itemLore = new ArrayList<>();
            	itemMeta.setDisplayName("Skalatan Defeated!");
            	itemLore.add("Matching outfits for extra style points...");
            	itemMeta.setLore(itemLore);
            	item.setItemMeta(itemMeta);           	
            	
                event.getDrops().add(item);
                if (event.getEntity().getKiller() instanceof Player)
                	Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "advancement grant " + event.getEntity().getKiller().getName() + " only adventure:skalatan");
                
                if (bossbar != null) {
                	bossbar.removeAll();
                    bossbar.setVisible(false);
                    bossbar = null;
                }
            }
        }
    }
	
	@EventHandler(ignoreCancelled = true)
	public void onHit(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof WitherSkeleton) {
			if (!wieldingBow.containsKey(event.getEntity().getUniqueId())) return;
			if (event.getDamager() instanceof Arrow && !wieldingBow.get(event.getEntity().getUniqueId())) {
				WitherSkeleton wskel = (WitherSkeleton) event.getEntity();
				if (skalatans.contains(wskel.getUniqueId())) {
					event.setCancelled(true);
					wskel.getWorld().playSound(wskel.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.3f, 1.2f);
					wskel.getWorld().spawnParticle(Particle.SQUID_INK, wskel.getLocation().add(0, wskel.getHeight()*0.66, 0), 5, 0.1D, 0.1D, 0.1D, 0.05D);
				}
			}
			else if (event.getDamager() instanceof Player && wieldingBow.get(event.getEntity().getUniqueId())) {
				WitherSkeleton wskel = (WitherSkeleton) event.getEntity();
				if (skalatans.contains(wskel.getUniqueId())) {
					event.setCancelled(true);
					wskel.getWorld().playSound(wskel.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.3f, 1.2f);
					wskel.getWorld().spawnParticle(Particle.SQUID_INK, wskel.getLocation().add(0, wskel.getHeight()*0.66, 0), 5, 0.1D, 0.1D, 0.1D, 0.05D);
				}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled = true)
	public void onSkalatanHit(EntityDamageEvent event) {
		if (skalatans.contains(event.getEntity().getUniqueId())) {
			if (bossbar != null) bossbar.setProgress(((WitherSkeleton) event.getEntity()).getHealth() / ((WitherSkeleton) event.getEntity()).getMaxHealth());
		}
	}
	
	public void createBossBar(Location loc) {
		if (bossbar != null) return;
		bossbar = Bukkit.createBossBar(ChatColor.YELLOW + "Skalatan", BarColor.YELLOW, BarStyle.SEGMENTED_12);
		bossbar.setProgress(1.0);
		
		List<Entity> nearbyEntities = (List<Entity>) loc.getWorld().getNearbyEntities(loc, 30, 80, 30);
		for (Entity ent : nearbyEntities) {
			if (ent instanceof Player) {
				bossbar.addPlayer((Player) ent);
			}
		}
		bossbar.setVisible(true);
	}
}
