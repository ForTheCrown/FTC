package ftc.bigcrown.challenges;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Evoker;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pillager;
import org.bukkit.entity.Player;
import org.bukkit.entity.Ravager;
import org.bukkit.entity.Vindicator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Score;
import org.bukkit.util.Vector;

import ftc.bigcrown.Main;
import net.md_5.bungee.api.ChatColor;

public class CastleRaidChallenge extends GenericChallenge implements Challenge, Listener {
	

	private Location startLocation = new Location(Bukkit.getWorld("world"), -4.5, 5, 37.5); // TODO
	
	private boolean raidHappening = false;
	private boolean startedRaid = false;
	private boolean completed = false;
	public BossBar capturePointBossBar;
	public int state = 0; 
	private Set<LivingEntity> raidwave = new HashSet<LivingEntity>();
	private Set<LivingEntity> raidwave2 = new HashSet<LivingEntity>();
	private Set<LivingEntity> raidwave3 = new HashSet<LivingEntity>();
	
	public CastleRaidChallenge(Player player) {
		super(player, ChallengeType.PVE_ARENA);
		if (player == null || Main.plugin.getChallengeInUse(getChallengeType())) return;
		
		// All needed setters from super class:
		setObjectiveName("crown");
		setReturnLocation(getPlayer().getLocation());
		setStartLocation(this.startLocation);
		setStartScore();

		startChallenge();
	}

	public void startChallenge() {
		// Teleport player to challenge:
		getPlayer().teleport(getStartLocation());
		getPlayer().playSound(getStartLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);

		// Send instruction on what to do:
		sendTitle();
		sendInfoViaChat();
		
		// Create capture point bossbar:
		capturePointBossBar = Bukkit.createBossBar(ChatColor.YELLOW + "Capture Point", BarColor.YELLOW, BarStyle.SEGMENTED_12);
		capturePointBossBar.setProgress(0.99f);
		
		// Show bossbar:
		capturePointBossBar.addPlayer(getPlayer());
		for (Entity ent : startLocation.getWorld().getNearbyEntities(startLocation, 40, 80, 40)) {
			if (ent instanceof Player) capturePointBossBar.addPlayer((Player) ent);
		}
		
		raidHappening = true;
		state = 1;
		capturePointLoop();
	}


	public void endChallenge() {
		capturePointBossBar.setVisible(false);
		capturePointBossBar.removeAll();
		
		getPlayer().playSound(getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 2f, 1.5f);

		// Amount of raiders killed:
		int score = calculateScore();
		if (completed) {
			score -= 50;
			completed = false;
		}
		if (score != 1) getPlayer().sendMessage(ChatColor.YELLOW + "You've killed " + score + " raiders!");
		else getPlayer().sendMessage(ChatColor.YELLOW + "You've killed 1 raider!");

		resetAll();
		teleportBack();
		
		PlayerQuitEvent.getHandlerList().unregister(this);
		EntityDeathEvent.getHandlerList().unregister(this);
		PlayerDeathEvent.getHandlerList().unregister(this);
	}


	public void sendTitle() {
		getPlayer().sendTitle(ChatColor.YELLOW + "Protect the Capture point!", ChatColor.GOLD + "April Event", 5, 60, 5);
	}
	
	private void sendInfoViaChat() {
		String prefix = ChatColor.GRAY + "" + ChatColor.BOLD  + "> ";
		getPlayer().sendMessage(ChatColor.YELLOW + "Event Info:");
		getPlayer().sendMessage(prefix + ChatColor.WHITE + "Earn points by killing raiders.");
		getPlayer().sendMessage(prefix + ChatColor.WHITE + "You slowly lose the capture point while you're not standing on it.");
		getPlayer().sendMessage(prefix + ChatColor.WHITE + "The challenge ends when you lose the capture point or if you die.");
		
	}

	@EventHandler
	public void onLogoutWhileInChallenge(PlayerQuitEvent event) {
		if (getPlayer() == null) return;
		if (event.getPlayer().getName() == getPlayer().getName()) {
			if (capturePointBossBar != null) {
				capturePointBossBar.setVisible(false);
				capturePointBossBar.removeAll();
			}
			resetAll();
		}

	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (getPlayer() == null) return;
		if (event.getEntity().getName() == getPlayer().getName()) {
			setChallengeCancelled(true);
			Main.plugin.setChallengeInUse(getChallengeType(), false);
			if (capturePointBossBar != null) {
				capturePointBossBar.setVisible(false);
				capturePointBossBar.removeAll();
			}
			resetAll();
		}
	}

	
	// --------------------------------------------------------------------------------- //
	

	public void capturePointLoop() {
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
	        @Override
	        public void run() {
	        	if (!raidHappening) {
	    			resetAll();
	    			return;
	    		}
	        	
	        	// Set correct state.
	        	// 0 = inactive
	        	// 1 = player on point
	        	// 2 = no player on point
	        	state = 2;
	    		for (Entity ent : getStartLocation().getWorld().getNearbyEntities(getStartLocation(), 0.6, 2, 0.6)) {
	    			if (ent.getType() == EntityType.PLAYER) {
	    				state = 1;
	    			}
	    		}
	    		
	    		// Do action based on state.
	    		switch (state) {
	    		case 1:
	    			if (capturePointBossBar.getProgress() < 1) {
	    				try {
	    					capturePointBossBar.setProgress(capturePointBossBar.getProgress()+0.05);
	    				}
	    				catch (Exception e) {
	    					capturePointBossBar.setProgress(1.0);
	    				}
	    				if (!startedRaid) {
	    					startedRaid = true;
	    					startRaid();
	    				}
	    			}
	    			break;
	    		case 2:
    				loseHealth(capturePointBossBar, 0.006);
	    			break;
    			default:
    				resetAll();
    				return;
	    		}
	    		
	    		capturePointLoop();
	        }
	    }, 5l);
	}

	public void startRaid() {
		getPlayer().playSound(getPlayer().getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.3f);
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
	        @Override
	        public void run() {
	        	getPlayer().playSound(getPlayer().getLocation(), Sound.EVENT_RAID_HORN, 10f, 1f);
	        	startWave1();
	        }
	    }, 40l);
		
	}
	

	private void startWave1() {
		if (!(raidwave.isEmpty())) return;
		// Wave 1
    	Set<Location> locs = new HashSet<Location>();
//	        	locs.add(new Location(Bukkit.getWorld("world"), 274.5, 68, 1121.5));
//	        	locs.add(new Location(Bukkit.getWorld("world"), 267.5, 69, 1112.5));
//	        	locs.add(new Location(Bukkit.getWorld("world"), 269.5, 68, 1092.5));
//	        	locs.add(new Location(Bukkit.getWorld("world"), 289.5, 68, 1094.5));
//	        	locs.add(new Location(Bukkit.getWorld("world"), 290.5, 69, 1111.5));
//	        	locs.add(new Location(Bukkit.getWorld("world"), 291.5, 69, 1131.5));
//	        	locs.add(new Location(Bukkit.getWorld("world"), 269.5, 69, 1137.5));
//	        	locs.add(new Location(Bukkit.getWorld("world"), 269.5, 69, 1131.5));
//	        	locs.add(new Location(Bukkit.getWorld("world"), 289.5, 76, 1136.5));
//	        	locs.add(new Location(Bukkit.getWorld("world"), 273.5, 75, 1083.5));
//	        	locs.add(new Location(Bukkit.getWorld("world"), 289.5, 77, 1080.5));
    	
    	// TODO update locs
    	locs.add(new Location(Bukkit.getWorld("world"), -32.5, 18, -6.5));
    	locs.add(new Location(Bukkit.getWorld("world"), -48.5, 18, -6.5));
    	locs.add(new Location(Bukkit.getWorld("world"), -32.5, 18, 11.5));
    	locs.add(new Location(Bukkit.getWorld("world"), -48.5, 18, 11.5));
    	
    	for (Location spawnLocation : locs) {
    		Vindicator vindi = spawnLocation.getWorld().spawn(spawnLocation, Vindicator.class);
    		addCustomStuffToVindicator(vindi);
    		raidwave.add(vindi);
    	}
	}
	
	private void startWave2() {
		if (!(raidwave2.isEmpty())) return;
		// Wave 2
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
			@Override
	        public void run() {
	        	Set<Location> locsv = new HashSet<Location>();
//	        	locsv.add(new Location(Bukkit.getWorld("world"), 274.5, 68, 1121.5));
//	        	locsv.add(new Location(Bukkit.getWorld("world"), 267.5, 69, 1112.5));
//	        	locsv.add(new Location(Bukkit.getWorld("world"), 269.5, 68, 1092.5));
//	        	locsv.add(new Location(Bukkit.getWorld("world"), 289.5, 68, 1094.5));
//	        	locsv.add(new Location(Bukkit.getWorld("world"), 290.5, 69, 1111.5));
//	        	locsv.add(new Location(Bukkit.getWorld("world"), 291.5, 69, 1131.5));
//	        	locsv.add(new Location(Bukkit.getWorld("world"), 269.5, 69, 1137.5));
//	        	locsv.add(new Location(Bukkit.getWorld("world"), 269.5, 69, 1131.5));
//	        	locsv.add(new Location(Bukkit.getWorld("world"), 273.5, 75, 1083.5));
//	        	locsv.add(new Location(Bukkit.getWorld("world"), 289.5, 77, 1080.5));
	        	
	        	Set<Location> locsp = new HashSet<Location>();
//	        	locsp.add(new Location(Bukkit.getWorld("world"), 277.5, 68, 1094.5));
//	        	locsp.add(new Location(Bukkit.getWorld("world"), 277.5, 69, 1083.5));
//	        	locsp.add(new Location(Bukkit.getWorld("world"), 293.5, 69, 1083.5));
//	        	locsp.add(new Location(Bukkit.getWorld("world"), 295.5, 69, 1093.5));
//	        	locsp.add(new Location(Bukkit.getWorld("world"), 282.5, 69, 1118.5));
//	        	locsp.add(new Location(Bukkit.getWorld("world"), 284.5, 69, 1128.5));
//	        	locsp.add(new Location(Bukkit.getWorld("world"), 288.5, 81, 1124.5));
//	        	locsp.add(new Location(Bukkit.getWorld("world"), 261.5, 81, 1110.5));
//	        	locsp.add(new Location(Bukkit.getWorld("world"), 261.5, 81, 1096.5));
//	        	locsp.add(new Location(Bukkit.getWorld("world"), 289.5, 83, 1078.5));
//	        	locsp.add(new Location(Bukkit.getWorld("world"), 298.5, 83, 1116.5));
	        	
	        	// TODO update locs
	        	locsv.add(new Location(Bukkit.getWorld("world"), -32.5, 18, -6.5));
	        	locsp.add(new Location(Bukkit.getWorld("world"), -48.5, 18, -6.5));
	        	locsv.add(new Location(Bukkit.getWorld("world"), -32.5, 18, 11.5));
	        	locsp.add(new Location(Bukkit.getWorld("world"), -48.5, 18, 11.5));
				
	        	for (Location spawnLocation : locsv) {
	        		Vindicator vindi = spawnLocation.getWorld().spawn(spawnLocation, Vindicator.class);
	        		addCustomStuffToVindicator(vindi);
	        		raidwave2.add(vindi);
	        	}
	        	for (Location spawnLocation : locsp) {
	        		Pillager pilli = spawnLocation.getWorld().spawn(spawnLocation, Pillager.class);
	        		addCustomStuffToPillager(pilli);
	        		raidwave2.add(pilli);
	        	}
	        }
	    }, 50l);
	}
	
	private void startWave3() {
		if (!(raidwave3.isEmpty())) return;
		// Wave 3
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
			@Override
	        public void run() {
	        	Set<Location> locsv = new HashSet<Location>();
//	        	locsv.add(new Location(Bukkit.getWorld("world"), 274.5, 68, 1121.5));
//	        	locsv.add(new Location(Bukkit.getWorld("world"), 267.5, 69, 1112.5));
//	        	locsv.add(new Location(Bukkit.getWorld("world"), 269.5, 68, 1092.5));
//	        	locsv.add(new Location(Bukkit.getWorld("world"), 289.5, 68, 1094.5));
//	        	locsv.add(new Location(Bukkit.getWorld("world"), 269.5, 69, 1131.5));
//	        	locsv.add(new Location(Bukkit.getWorld("world"), 289.5, 76, 1136.5));
//	        	locsv.add(new Location(Bukkit.getWorld("world"), 273.5, 75, 1083.5));
//	        	locsv.add(new Location(Bukkit.getWorld("world"), 289.5, 77, 1080.5));
	        	
	        	Set<Location> locsp = new HashSet<Location>();
//	        	locsp.add(new Location(Bukkit.getWorld("world"), 277.5, 68, 1094.5));
//	        	locsp.add(new Location(Bukkit.getWorld("world"), 277.5, 69, 1083.5));
//	        	locsp.add(new Location(Bukkit.getWorld("world"), 293.5, 69, 1083.5));
//	        	locsp.add(new Location(Bukkit.getWorld("world"), 295.5, 69, 1093.5));
//	        	locsp.add(new Location(Bukkit.getWorld("world"), 282.5, 69, 1118.5));
//	        	locsp.add(new Location(Bukkit.getWorld("world"), 284.5, 69, 1128.5));
//	        	locsp.add(new Location(Bukkit.getWorld("world"), 294.5, 69, 1119.5));
//	        	locsp.add(new Location(Bukkit.getWorld("world"), 268.5, 74, 1134.5));
//	        	locsp.add(new Location(Bukkit.getWorld("world"), 289.5, 83, 1078.5));
//	        	locsp.add(new Location(Bukkit.getWorld("world"), 298.5, 83, 1116.5));
	        	
	        	Set<Location> locsr = new HashSet<Location>();
//	        	locsr.add(new Location(Bukkit.getWorld("world"), 277.5, 74, 1130.5));
//	        	locsr.add(new Location(Bukkit.getWorld("world"), 266.5, 70, 1089.5));
//	        	locsr.add(new Location(Bukkit.getWorld("world"), 273.5, 69, 1134.5));
	        	
	        	Set<Location> locse = new HashSet<Location>();
//	        	locse.add(new Location(Bukkit.getWorld("world"), 290.5, 69, 1104.5));
//	        	locse.add(new Location(Bukkit.getWorld("world"), 292.5, 70, 1081.5));
//	        	locse.add(new Location(Bukkit.getWorld("world"), 295.5, 69, 1081.5));
//	        	locse.add(new Location(Bukkit.getWorld("world"), 293.5, 75, 1128.5));
	        	
	        	// TODO update locs
	        	locsv.add(new Location(Bukkit.getWorld("world"), -32.5, 18, -6.5));
	        	locsp.add(new Location(Bukkit.getWorld("world"), -48.5, 18, -6.5));
	        	locsr.add(new Location(Bukkit.getWorld("world"), -32.5, 18, 11.5));
	        	locse.add(new Location(Bukkit.getWorld("world"), -48.5, 18, 11.5));
	        	
	        	
				
	        	for (Location spawnLocation : locsv) {
	        		Vindicator vindi = spawnLocation.getWorld().spawn(spawnLocation, Vindicator.class);
	        		addCustomStuffToVindicator(vindi);
	        		raidwave3.add(vindi);
	        	}
	        	for (Location spawnLocation : locsp) {
	        		Pillager pilli = spawnLocation.getWorld().spawn(spawnLocation, Pillager.class);
	        		addCustomStuffToPillager(pilli);
	        		
	        		raidwave3.add(pilli);
	        	}
	        	for (Location spawnLocation : locsr) {
	        		Ravager ravi = spawnLocation.getWorld().spawn(spawnLocation, Ravager.class);
	        		ravi.setLootTable(loot);
	        		ravi.setRemoveWhenFarAway(false);
	        		ravi.setPersistent(true);
	        		
	        		raidwave3.add(ravi);
	        	}
	        	for (Location spawnLocation : locse) {
	        		Evoker evoki = spawnLocation.getWorld().spawn(spawnLocation, Evoker.class);
	        		evoki.setPatrolLeader(false);
	        		evoki.setLootTable(loot);
	        		evoki.setRemoveWhenFarAway(false);
	        		evoki.setPersistent(true);
	        		
	        		Attributable att = (Attributable) evoki;
	        		AttributeInstance maxHealth = att.getAttribute(Attribute.GENERIC_MAX_HEALTH);
	        		maxHealth.setBaseValue(40);
	        		evoki.setHealth(40);
	        		
	        		raidwave3.add(evoki);
	        	}
	        }
	    }, 50l);
		
	}
	
	private void addCustomStuffToVindicator(Vindicator vindi) {
		if (vindi == null) return;
		
		vindi.setPatrolLeader(false);
		vindi.setLootTable(loot);
		vindi.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_AXE));
		vindi.getEquipment().setItemInMainHandDropChance(0f);
		vindi.setRemoveWhenFarAway(false);
		vindi.setPersistent(true);
		
		Attributable att = (Attributable) vindi;
		AttributeInstance maxHealth = att.getAttribute(Attribute.GENERIC_MAX_HEALTH);
		maxHealth.setBaseValue(40);
		vindi.setHealth(40);
		
		AttributeInstance knockBackRes = att.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
		knockBackRes.setBaseValue(0.8f);
	}
	
	private void addCustomStuffToPillager(Pillager pillager) {
		if (pillager == null) return;
		
		pillager.setPatrolLeader(false);
		pillager.setLootTable(loot);
		pillager.getEquipment().setItemInMainHand(new ItemStack(Material.CROSSBOW));
		pillager.getEquipment().setItemInMainHandDropChance(0f);
		pillager.setRemoveWhenFarAway(false);
		pillager.setPersistent(true);
		
		Attributable att = (Attributable) pillager;
		
		AttributeInstance maxHealth = att.getAttribute(Attribute.GENERIC_MAX_HEALTH);
		maxHealth.setBaseValue(30);
		pillager.setHealth(30);
	}
	
	
	@EventHandler
	public void playerKillRaidMob(EntityDeathEvent event) {
		if (raidwave.contains(event.getEntity())) {	
			doStuff(event, raidwave);
        	if (raidwave.isEmpty()) startWave2();
		}
		else if (raidwave2.contains(event.getEntity())) {
			doStuff(event, raidwave2);
			if (raidwave2.isEmpty()) startWave3();
		}
		else if (raidwave3.contains(event.getEntity())) {
			
			doStuff(event, raidwave3);
			if (raidwave3.isEmpty()) {
				getPlayer().playSound(getPlayer().getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.7f, 1.2f);
				getPlayer().sendMessage(ChatColor.GRAY + "[FTC] Defeated all mobs in the arena!");
				
				Score score = getScoreboardObjective().getScore(getPlayer().getName());
				score.setScore(score.getScore() + 50);
				getPlayer().sendMessage(ChatColor.GRAY + "You've received 50 points for completing this challenge!");
				ItemStack emerald_block = new ItemStack(Material.EMERALD_BLOCK, 10);
				try {
					 getPlayer().getInventory().addItem(emerald_block);
				}
				catch (Exception e) {
					 getPlayer().getWorld().dropItem(getPlayer().getLocation(), emerald_block);
				}
				count = 6;
				raidwave.clear();
				raidwave2.clear();
				raidwave3.clear();
				
				completed = true;
				endChallenge();
			}
		}
	}
	
	private void doStuff(EntityDeathEvent event, Set<LivingEntity> raidwave) {
		// Drops
		event.getDrops().clear();
		tryDropEmerald(event.getEntity().getLocation());
		
		// Add name:
		event.getEntity().setCustomNameVisible(true);
		event.getEntity().setCustomName(ChatColor.GOLD + "" + ChatColor.BOLD + "+1");
		
		raidwave.remove(event.getEntity());
		// Make last 3 remaining raiders glow:
    	if (raidwave.size() < 4) {
        	for (LivingEntity ent : raidwave) {
        		PotionEffect potion = new PotionEffect(PotionEffectType.GLOWING, 9999, 0, false, false);
        		potion.apply(ent);
        	}
    	}
    	
    	// Add score to the player
		if (!(event.getEntity().getKiller() instanceof Player)) return;
		Player player = (Player) event.getEntity().getKiller();
		Score score = getScoreboardObjective().getScore(player.getName());
		
		event.getEntity().setCustomName(ChatColor.YELLOW + "" + ChatColor.BOLD + "+1");
		score.setScore(score.getScore() + 1);
	    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.3f);
	}
	
	private void tryDropEmerald(Location loc) {
		double random = Math.random();
		if (random < 0.5) {
			Item emerald = loc.getWorld().dropItem(loc, new ItemStack(Material.EMERALD, 1));
			emerald.setVelocity(new Vector(0, 0.2, 0));
		}
	}


	
	private int count = 6;
	public void loseHealth(BossBar bar, Double d) {
		if (bar == null) return;
		// Losing Capture Point
    	try {
    		capturePointBossBar.setProgress(capturePointBossBar.getProgress()-d);
    		if (count == 13) {
	    		try {
	    			getPlayer().playSound(getPlayer().getLocation(), Sound.BLOCK_CONDUIT_AMBIENT, SoundCategory.MASTER, (float) (0.50-capturePointBossBar.getProgress()), 1.2f);
	    		} catch (Exception ignored) {}
	    		count = 0;
    		}
    		else {
    			count++;
    		}
    	}
    	catch (IllegalArgumentException ignored) {
    		capturePointBossBar.setProgress(0);
    	}
    	
    	// Capture Point is out of juice:
    	if (capturePointBossBar.getProgress() <= 0) {
			getPlayer().sendMessage(ChatColor.GRAY + "You've lost the capture point!");
			endChallenge();
		}
	}
	
	
	
	public void resetAll() {
		capturePointBossBar = null;
		raidHappening = false;
		startedRaid = false;
		completed = false;
		count = 6;
		state = 0;
		
		for (LivingEntity ent : raidwave) {
			ent.setHealth(5.0);
			PotionEffect potion = new PotionEffect(PotionEffectType.WITHER, 9999, 5, false, false);
    		potion.apply(ent);
    	}
		for (LivingEntity ent : raidwave2) {
			ent.setHealth(5.0);
			PotionEffect potion = new PotionEffect(PotionEffectType.WITHER, 9999, 5, false, false);
    		potion.apply(ent);
    	}
		for (LivingEntity ent : raidwave3) {
			ent.setHealth(5.0);
			PotionEffect potion = new PotionEffect(PotionEffectType.WITHER, 9999, 5, false, false);
    		potion.apply(ent);
    	}
		raidwave.clear();
		raidwave2.clear();
		raidwave3.clear();

	}

}
