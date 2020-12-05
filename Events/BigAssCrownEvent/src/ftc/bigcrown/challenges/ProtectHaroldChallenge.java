package ftc.bigcrown.challenges;

import ftc.bigcrown.Main;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Score;
import org.bukkit.util.BoundingBox;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ProtectHaroldChallenge extends GenericChallenge {

    public boolean canDrop;
    private Location startLocation = new Location(Bukkit.getWorld("world"), -51.5, 66, 879.5);

    public ProtectHaroldChallenge(Player player) {
        super(player, ChallengeType.PINATA);
        if (player == null || Main.plugin.getChallengeInUse(getChallengeType())) return;
        
        // All needed setters from super class:
 		setObjectiveName("zombiesKilled");
 		setReturnLocation(getPlayer().getLocation());
 		setStartLocation(this.startLocation);
 		setStartScore();

        startChallenge();
    }

    public void startChallenge() {
    	// Teleport player to challenge:
    	getPlayer().teleport(getStartLocation());
    	getPlayer().playSound(getStartLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
    	
    	sendTitle();
    	
    	clearMobs();
    	spawnHarold();
    	Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
	        @Override
	        public void run() {
	        	setChallengeCancelled(false);
	        	zombieLoop();
	        }
	    }, 60L);
    }

	public void endChallenge() {
		clearMobs();
		
		getPlayer().playSound(getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 2f, 1.5f);

		// Amount of zombies killed:
		int score = calculateScore();
		if (score != 1) getPlayer().sendMessage(ChatColor.YELLOW + "You've protected Harold from " + score + " zombies!");
		else getPlayer().sendMessage(ChatColor.YELLOW + "You've protected Harold from 1 zombie!");

		// Add to crown scoreboard:
    	//Scoreboard mainScoreboard = Main.plugin.getServer().getScoreboardManager().getMainScoreboard();
    	//Score crownScore = mainScoreboard.getObjective("crown").getScore(getPlayer().getName());
    	//crownScore.setScore(crownScore.getScore() + score);

		// If their current score is bigger than their record score
		if(isRecordSmallerThanScore()){
			Score playerScore = getRecordScoreboardObjective().getScore(getPlayer().getName());
			playerScore.setScore(scoreMap.get(getPlayer().getUniqueId()));
			scoreMap.remove(getPlayer().getUniqueId());
		}

		calculatePlayerScore();
		teleportBack();
		
		PlayerQuitEvent.getHandlerList().unregister(this);
		EntityDeathEvent.getHandlerList().unregister(this);
		PlayerDeathEvent.getHandlerList().unregister(this);
	}
    

    public void sendTitle() {
        this.getPlayer().sendTitle(ChatColor.YELLOW + "Keep Harold Alive!", ChatColor.GOLD + "June Event", 5, 60, 5);
    }
    
    private Set<UUID> zombies = new HashSet<>();
    
    private void zombieLoop() {
    	Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
	        @Override
	        public void run() {
	        	if (isChallengeCancelled()) return;
	        	
	        	// Still lots of zombies alive:
	        	if (zombies.size() > 3) {
	        		zombieLoop();
	        		return;
	        	}
	        	
	        	// Less than 3 zombies alive:
	        	else {
	        		spawnZombies();
	        	}
	        	
	        	zombieLoop();
	        }
	    }, 40L);
		
	}

	private void spawnZombies() {
		for (Location loc : getZombieSpawnLocations()) {
			Zombie zombie = getStartLocation().getWorld().spawn(loc, Zombie.class);
			zombie.setLootTable(loot);
			zombie.setPersistent(true);
			zombie.setRemoveWhenFarAway(false);
			zombie.getEquipment().setHelmet(new ItemStack(Material.STONE_BUTTON, 1));
			zombie.getEquipment().setHelmetDropChance(0f);
			Attributable att = (Attributable) zombie;
			
			AttributeInstance maxHealth = att.getAttribute(Attribute.GENERIC_MAX_HEALTH);
			maxHealth.setBaseValue(25f);
			zombie.setHealth(25f);
			
			AttributeInstance speed = att.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
			speed.setBaseValue(0.312f);
			
			AttributeInstance followRange = att.getAttribute(Attribute.GENERIC_FOLLOW_RANGE);
			followRange.setBaseValue(10f);
			
			zombies.add(zombie.getUniqueId());
		}
		
	}
    
    
	private Location[] getZombieSpawnLocations() {
		World world = getStartLocation().getWorld();
		Location[] locs = {
				new Location(world, -46.5, 66, 879.5),
				new Location(world, -42.5, 66, 875.5),
				new Location(world, -46.5, 66, 890.5),
				new Location(world, -42.5, 66, 894.5),
				new Location(world, -57.5, 66, 890.5),
				new Location(world, -61.5, 66, 894.5),
				new Location(world, -57.5, 66, 879.5),
				new Location(world, -61.5, 66, 875.5)
		};
		return locs;
	}

	private void spawnHarold() {
		Villager harold = getStartLocation().getWorld().spawn(new Location(getStartLocation().getWorld(), -51.5, 66, 881.5, -180, 0), Villager.class);
		harold.setGlowing(true);
		harold.setLootTable(loot);
		harold.setCustomName(ChatColor.YELLOW + "Harold");
		harold.setCustomNameVisible(true);
		harold.setVillagerLevel(5);
		harold.setRecipes(new ArrayList<>());
	}

	private void clearMobs() {
		for (UUID uuid : zombies) {
			((LivingEntity) Bukkit.getEntity(uuid)).setHealth(1);
			Bukkit.getEntity(uuid).teleport(Bukkit.getEntity(uuid).getLocation().add(0, -300, 0));
		}
		
		for (Entity entity : getStartLocation().getWorld().getNearbyEntities(getArena())) {
			if (entity.getType() == EntityType.ZOMBIE || entity.getType() == EntityType.ZOMBIE_VILLAGER || entity.getType() == EntityType.VILLAGER) {
				entity.remove();
			}
		}
		
	}
	
	private BoundingBox getArena() {
		return new BoundingBox(-65, 55, 872, -41, 68, 896);
	}
    

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		if (event.getEntity().getType() == EntityType.ZOMBIE && zombies.contains(event.getEntity().getUniqueId())) {
			event.getEntity().setCustomNameVisible(true);
			event.getEntity().setCustomName(ChatColor.GOLD + "" + ChatColor.BOLD + "+1");
			zombies.remove(event.getEntity().getUniqueId());
		}
		else if (event.getEntity().getType() == EntityType.VILLAGER && event.getEntity().getCustomName().contains(ChatColor.YELLOW + "Harold")) {
			endChallenge();
		}
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (getPlayer() == null) return;
		if (event.getEntity().getName() == getPlayer().getName()) {
			//event.setKeepLevel(true);
			//event.setKeepInventory(true);
			event.setDeathMessage(getPlayer().getName() + " died trying to protect Harold.");
			Main.plugin.setChallengeInUse(getChallengeType(), false);
			endChallenge();
			
		}
	}
	
	@EventHandler
    public void onLogoutWhileInChallenge(PlayerQuitEvent event) {
        if (getPlayer() == null) return;
    	if (event.getPlayer().getName() == getPlayer().getName()) {
    		endChallenge();
        }
    }
    

}
