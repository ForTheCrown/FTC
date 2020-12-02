package me.wout.RandomFeatures.commands;

import org.bukkit.command.CommandExecutor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;



public class Capturing implements CommandExecutor, Listener {

	/*private main plugin;
	private Location loc;
	private List<Entity> nearbyEntites;
	private boolean lock = false;
	private String playername = "Notch";
	*/
	
//	public Capturing(main plugin, Location loc) {
//		//this.loc = loc;
//		//this.plugin = plugin;
//		//plugin.getCommand("capturing").setExecutor(this);
//	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		/*if (sender instanceof Player) {
			sender.sendMessage(ChatColor.RED + "Players can't do this.");
			return false;
		}
		lock = !lock;
		
		if (plugin.capturePointHealth == null) {
			plugin.state = 0;
			return false;
		}
		this.loc = plugin.cap.getLocation();
		doUntilState0();*/
		return true;
	}
	
	/*private void doUntilState0() {
		boolean currentlock = lock;
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
	        @Override
	        public void run() {
	        	if (currentlock != lock) return;
	        	if (plugin.capturePointHealth == null) {
	    			plugin.state = 0;
	    			return;
	    		}
	    		
	    		nearbyEntites = (List<Entity>) loc.getWorld().getNearbyEntities(loc, 0.6, 2, 0.6);
	    		List<Entity> nearbyPlayers = new ArrayList<Entity>();
	    		for (Entity ent : nearbyEntites) {
	    			if (ent.getType() == EntityType.PLAYER) {
	    				nearbyPlayers.add(ent);
	    			}
	    		}
	    		String name = "";
	    		
	    		// Set correct state.
	    		if (nearbyPlayers.size() != 1) {
	    			plugin.state = 2;
	    		}
	    		else if (nearbyPlayers.get(0) instanceof Player) {
	    			playername = nearbyPlayers.get(0).getName();
	    			name = plugin.capturePointHealth.getTitle().split(ChatColor.YELLOW + "")[1];
	    			if (name.equalsIgnoreCase(playername)) plugin.state = 1;
		    		else plugin.state = 3;
	    		}
	    		else {
	    			plugin.state = 2;
	    		}
	    		
	    		List<Entity> nearbyEntites2 = (List<Entity>) loc.getWorld().getNearbyEntities(loc, 60, 80, 60);
	    		// Do action based on state.
	    		if (plugin.state == 1) {
	    			if (plugin.capturePointHealth.getProgress() < 1) {
	    				try {
	    					plugin.capturePointHealth.setProgress(plugin.capturePointHealth.getProgress()+0.15);
	    				}
	    				catch (Exception e) {
	    					plugin.capturePointHealth.setProgress(1.0);
	    					// Start wave 1.
	    					startEvent(name, nearbyEntites2);
	    				}
	    			}
	    			
	    		} 
	    		else if (plugin.state == 2) {
	    			loseHealth(plugin.capturePointHealth, 0.006);
	    		}
	    		else if (plugin.state == 3) {
	    			loseHealth(plugin.capturePointHealth, 0.015);
	    		} 
	    		else {
	    			return;
	    		}
	    		
	    		for (Entity ent : nearbyEntites2) {
	    			if (ent != null && ent instanceof Player && plugin.capturePointHealth != null) {
	    				plugin.capturePointHealth.addPlayer((Player) ent);
	    			}
	    		}
    			doUntilState0();
	        }
	    }, 5l);
		
		
		
	}

	protected void startEvent(String playername, List<Entity> nearbyEntites2) {
		if (Bukkit.getPlayer(playername) == null || plugin.busy == true) return;
		ItemStack itemInHand = Bukkit.getPlayer(playername).getInventory().getItemInMainHand();
		if (!(itemInHand.getType() == Material.PAPER && itemInHand.hasItemMeta() && itemInHand.getItemMeta().hasLore() && itemInHand.getItemMeta().getLore().contains("arena event, be ready!"))) {
			Bukkit.getPlayer(playername).sendMessage(ChatColor.GRAY + "[FTC] You need to hold an arena ticket to start.");
			return;
		}
		itemInHand.setAmount(itemInHand.getAmount()-1);
		
		for (int i = 0; i < nearbyEntites2.size(); i++) {
			if (nearbyEntites2.get(i) != null && nearbyEntites2.get(i) instanceof Player) {
				((Player) nearbyEntites2.get(i)).playSound(((Player) nearbyEntites2.get(i)).getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.3f);
			} 
		}
		
		plugin.busy = true;
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
	        @Override
	        public void run() {
	        	for (int i = 0; i < nearbyEntites2.size(); i++) {
	        		if (nearbyEntites2.get(i) != null && nearbyEntites2.get(i) instanceof Player) {
	        			((Player) nearbyEntites2.get(i)).playSound(((Player) nearbyEntites2.get(i)).getLocation(), Sound.EVENT_RAID_HORN, 10f, 1f);
	        		}
	        	}
	        }
	    }, 50l);
		startWave1();
	}
	
	private void startWave3() {
		if (!(plugin.raidwave3.isEmpty())) return;
		// Wave 3
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
	        @SuppressWarnings("deprecation")
			@Override
	        public void run() {
	        	Set<Location> locsv = new HashSet<Location>();
	        	locsv.add(new Location(Bukkit.getWorld("world"), 274.5, 68, 1121.5));
	        	locsv.add(new Location(Bukkit.getWorld("world"), 267.5, 69, 1112.5));
	        	locsv.add(new Location(Bukkit.getWorld("world"), 269.5, 68, 1092.5));
	        	locsv.add(new Location(Bukkit.getWorld("world"), 289.5, 68, 1094.5));
	        	locsv.add(new Location(Bukkit.getWorld("world"), 269.5, 69, 1131.5));
	        	locsv.add(new Location(Bukkit.getWorld("world"), 289.5, 76, 1136.5));
	        	locsv.add(new Location(Bukkit.getWorld("world"), 273.5, 75, 1083.5));
	        	locsv.add(new Location(Bukkit.getWorld("world"), 289.5, 77, 1080.5));
	        	
	        	Set<Location> locsp = new HashSet<Location>();
	        	locsp.add(new Location(Bukkit.getWorld("world"), 277.5, 68, 1094.5));
	        	locsp.add(new Location(Bukkit.getWorld("world"), 277.5, 69, 1083.5));
	        	locsp.add(new Location(Bukkit.getWorld("world"), 293.5, 69, 1083.5));
	        	locsp.add(new Location(Bukkit.getWorld("world"), 295.5, 69, 1093.5));
	        	locsp.add(new Location(Bukkit.getWorld("world"), 282.5, 69, 1118.5));
	        	locsp.add(new Location(Bukkit.getWorld("world"), 284.5, 69, 1128.5));
	        	locsp.add(new Location(Bukkit.getWorld("world"), 294.5, 69, 1119.5));
	        	locsp.add(new Location(Bukkit.getWorld("world"), 268.5, 74, 1134.5));
	        	locsp.add(new Location(Bukkit.getWorld("world"), 289.5, 83, 1078.5));
	        	locsp.add(new Location(Bukkit.getWorld("world"), 298.5, 83, 1116.5));
	        	
	        	Set<Location> locsr = new HashSet<Location>();
	        	locsr.add(new Location(Bukkit.getWorld("world"), 277.5, 74, 1130.5));
	        	locsr.add(new Location(Bukkit.getWorld("world"), 266.5, 70, 1089.5));
	        	locsr.add(new Location(Bukkit.getWorld("world"), 273.5, 69, 1134.5));
	        	
	        	Set<Location> locse = new HashSet<Location>();
	        	locse.add(new Location(Bukkit.getWorld("world"), 290.5, 69, 1104.5));
	        	locse.add(new Location(Bukkit.getWorld("world"), 292.5, 70, 1081.5));
	        	locse.add(new Location(Bukkit.getWorld("world"), 295.5, 69, 1081.5));
	        	locse.add(new Location(Bukkit.getWorld("world"), 293.5, 75, 1128.5));
	        	
	        	LootTable loot = new LootTable() {
					
					@Override
					public NamespacedKey getKey() {
						return new NamespacedKey(plugin, "woutvoid");
					}
					
					@Override
					public Collection<ItemStack> populateLoot(Random arg0, LootContext arg1) {
						return null;
					}
					
					@Override
					public void fillInventory(Inventory arg0, Random arg1, LootContext arg2) {
					}
				};
				
	        	for (Location spawnLocation : locsv) {
	        		Vindicator vindi = spawnLocation.getWorld().spawn(spawnLocation, Vindicator.class);
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
	        		knockBackRes.setBaseValue(0.7f);
	        		
	        		plugin.raidwave3.add(vindi);
	        	}
	        	for (Location spawnLocation : locsp) {
	        		Pillager pilli = spawnLocation.getWorld().spawn(spawnLocation, Pillager.class);
	        		pilli.setPatrolLeader(false);
	        		pilli.setLootTable(loot);
	        		pilli.getEquipment().setItemInMainHand(new ItemStack(Material.CROSSBOW));
	        		pilli.getEquipment().setItemInMainHandDropChance(0f);
	        		pilli.setRemoveWhenFarAway(false);
	        		pilli.setPersistent(true);
	        		
	        		Attributable att = (Attributable) pilli;
	        		
	        		AttributeInstance maxHealth = att.getAttribute(Attribute.GENERIC_MAX_HEALTH);
	        		maxHealth.setBaseValue(30);
	        		pilli.setHealth(30);
	        		
	        		plugin.raidwave3.add(pilli);
	        	}
	        	for (Location spawnLocation : locsr) {
	        		Ravager ravi = spawnLocation.getWorld().spawn(spawnLocation, Ravager.class);
	        		ravi.setLootTable(loot);
	        		ravi.setRemoveWhenFarAway(false);
	        		ravi.setPersistent(true);
	        		
	        		plugin.raidwave3.add(ravi);
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
	        		
	        		plugin.raidwave3.add(evoki);
	        	}
	        }
	    }, 50l);
		
	}
	
	private void startWave2() {
		if (!(plugin.raidwave2.isEmpty())) return;
		// Wave 2
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
	        @SuppressWarnings("deprecation")
			@Override
	        public void run() {
	        	Set<Location> locsv = new HashSet<Location>();
	        	locsv.add(new Location(Bukkit.getWorld("world"), 274.5, 68, 1121.5));
	        	locsv.add(new Location(Bukkit.getWorld("world"), 267.5, 69, 1112.5));
	        	locsv.add(new Location(Bukkit.getWorld("world"), 269.5, 68, 1092.5));
	        	locsv.add(new Location(Bukkit.getWorld("world"), 289.5, 68, 1094.5));
	        	locsv.add(new Location(Bukkit.getWorld("world"), 290.5, 69, 1111.5));
	        	locsv.add(new Location(Bukkit.getWorld("world"), 291.5, 69, 1131.5));
	        	locsv.add(new Location(Bukkit.getWorld("world"), 269.5, 69, 1137.5));
	        	locsv.add(new Location(Bukkit.getWorld("world"), 269.5, 69, 1131.5));
	        	locsv.add(new Location(Bukkit.getWorld("world"), 273.5, 75, 1083.5));
	        	locsv.add(new Location(Bukkit.getWorld("world"), 289.5, 77, 1080.5));
	        	
	        	Set<Location> locsp = new HashSet<Location>();
	        	locsp.add(new Location(Bukkit.getWorld("world"), 277.5, 68, 1094.5));
	        	locsp.add(new Location(Bukkit.getWorld("world"), 277.5, 69, 1083.5));
	        	locsp.add(new Location(Bukkit.getWorld("world"), 293.5, 69, 1083.5));
	        	locsp.add(new Location(Bukkit.getWorld("world"), 295.5, 69, 1093.5));
	        	locsp.add(new Location(Bukkit.getWorld("world"), 282.5, 69, 1118.5));
	        	locsp.add(new Location(Bukkit.getWorld("world"), 284.5, 69, 1128.5));
	        	locsp.add(new Location(Bukkit.getWorld("world"), 288.5, 81, 1124.5));
	        	locsp.add(new Location(Bukkit.getWorld("world"), 261.5, 81, 1110.5));
	        	locsp.add(new Location(Bukkit.getWorld("world"), 261.5, 81, 1096.5));
	        	locsp.add(new Location(Bukkit.getWorld("world"), 289.5, 83, 1078.5));
	        	locsp.add(new Location(Bukkit.getWorld("world"), 298.5, 83, 1116.5));
	        	
	        	LootTable loot = new LootTable() {
					
					@Override
					public NamespacedKey getKey() {
						return new NamespacedKey(plugin, "woutvoid");
					}
					
					@Override
					public Collection<ItemStack> populateLoot(Random arg0, LootContext arg1) {
						return null;
					}
					
					@Override
					public void fillInventory(Inventory arg0, Random arg1, LootContext arg2) {
					}
				};
				
	        	for (Location spawnLocation : locsv) {
	        		Vindicator vindi = spawnLocation.getWorld().spawn(spawnLocation, Vindicator.class);
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
	        		knockBackRes.setBaseValue(0.7f);
	        		
	        		
	        		/*PotionEffect potion = new PotionEffect(PotionEffectType.INVISIBILITY, 9999, 0, false, false);
	        		potion.apply((LivingEntity) vindi);
	        		
	        		
	        		plugin.raidwave2.add(vindi);
	        	}
	        	for (Location spawnLocation : locsp) {
	        		Pillager pilli = spawnLocation.getWorld().spawn(spawnLocation, Pillager.class);
	        		pilli.setPatrolLeader(false);
	        		pilli.setLootTable(loot);
	        		pilli.getEquipment().setItemInMainHand(new ItemStack(Material.CROSSBOW));
	        		pilli.getEquipment().setItemInMainHandDropChance(0f);
	        		pilli.setRemoveWhenFarAway(false);
	        		pilli.setPersistent(true);
	        		
	        		Attributable att = (Attributable) pilli;
	        		
	        		AttributeInstance maxHealth = att.getAttribute(Attribute.GENERIC_MAX_HEALTH);
	        		maxHealth.setBaseValue(30);
	        		pilli.setHealth(30);
	        		
	        		plugin.raidwave2.add(pilli);
	        	}
	        }
	    }, 50l);
	}
	

	private void startWave1() {
		if (!(plugin.raidwave.isEmpty())) return;
		// Wave 1
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
	        @SuppressWarnings("deprecation")
			@Override
	        public void run() {
	        	Set<Location> locs = new HashSet<Location>();
	        	locs.add(new Location(Bukkit.getWorld("world"), 274.5, 68, 1121.5));
	        	locs.add(new Location(Bukkit.getWorld("world"), 267.5, 69, 1112.5));
	        	locs.add(new Location(Bukkit.getWorld("world"), 269.5, 68, 1092.5));
	        	locs.add(new Location(Bukkit.getWorld("world"), 289.5, 68, 1094.5));
	        	locs.add(new Location(Bukkit.getWorld("world"), 290.5, 69, 1111.5));
	        	locs.add(new Location(Bukkit.getWorld("world"), 291.5, 69, 1131.5));
	        	locs.add(new Location(Bukkit.getWorld("world"), 269.5, 69, 1137.5));
	        	locs.add(new Location(Bukkit.getWorld("world"), 269.5, 69, 1131.5));
	        	locs.add(new Location(Bukkit.getWorld("world"), 289.5, 76, 1136.5));
	        	locs.add(new Location(Bukkit.getWorld("world"), 273.5, 75, 1083.5));
	        	locs.add(new Location(Bukkit.getWorld("world"), 289.5, 77, 1080.5));
	        	
	        	LootTable loot = new LootTable() {
					
					@Override
					public NamespacedKey getKey() {
						return new NamespacedKey(plugin, "woutvoid");
					}
					
					@Override
					public Collection<ItemStack> populateLoot(Random arg0, LootContext arg1) {
						return null;
					}
					
					@Override
					public void fillInventory(Inventory arg0, Random arg1, LootContext arg2) {
					}
				};
	        	
	        	for (Location spawnLocation : locs) {
	        		Vindicator vindi = spawnLocation.getWorld().spawn(spawnLocation, Vindicator.class);
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
	        		
	        		/*PotionEffect potion = new PotionEffect(PotionEffectType.INVISIBILITY, 9999, 0, false, false);
	        		potion.apply((LivingEntity) vindi);
	        		
	        		
	        		plugin.raidwave.add(vindi);
	        	}
	        }
	    }, 50l);
	}
	
	int wavecount = 0;
	
	@EventHandler
	public void playerKillRaidMob(EntityDeathEvent event) {
		if (plugin.raidwave.contains(event.getEntity())) {	
			double random = Math.random();
			if (random < 0.45) {
				Item emerald = event.getEntity().getWorld().dropItem(event.getEntity().getLocation(), new ItemStack(Material.EMERALD, 3));
				emerald.setVelocity(new Vector(0, 0.2, 0));
			}
			doStuff(event, plugin.raidwave);
        	if (plugin.raidwave.isEmpty()) {
        		if (wavecount == 0) {
        			startWave1();
        			wavecount++;
        		} else {
        			startWave2();
        			wavecount = 0;
        		}
        	}
		}
		else if (plugin.raidwave2.contains(event.getEntity())) {
			double random = Math.random();
			if (random < 0.45) {
				Item emerald = event.getEntity().getWorld().dropItem(event.getEntity().getLocation(), new ItemStack(Material.EMERALD, 3));
				emerald.setVelocity(new Vector(0, 0.2, 0));
			}
			doStuff(event, plugin.raidwave2);
			if (plugin.raidwave2.isEmpty()) startWave3();
		}
		else if (plugin.raidwave3.contains(event.getEntity())) {
			double random = Math.random();
			if (random < 0.45) {
				Item emerald = event.getEntity().getWorld().dropItem(event.getEntity().getLocation(), new ItemStack(Material.EMERALD, 3));
				emerald.setVelocity(new Vector(0, 0.2, 0));
			}
			doStuff(event, plugin.raidwave3);
			if (plugin.raidwave3.isEmpty()) {
				List<Entity> nearbyEntities = (List<Entity>) loc.getWorld().getNearbyEntities(loc, 60, 80, 60);
				for (Entity entity : nearbyEntities) {
					if (entity instanceof Player) {
						((Player) entity).playSound(entity.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.7f, 1.2f);
						((Player) entity).sendMessage(ChatColor.GRAY + "[FTC] Defeated all mobs in the arena!");
					}
				}
				/*Objective scoreboardobj = Bukkit.getPlayer(playername).getScoreboard().getObjective(DisplaySlot.PLAYER_LIST);
				if (scoreboardobj.getName().equalsIgnoreCase(plugin.getConfig().getString("Scoreboard"))) {
					@SuppressWarnings("deprecation")
					Score score = scoreboardobj.getScore(Bukkit.getPlayer(playername));
					score.setScore(score.getScore() + 50);
					Bukkit.getPlayer(playername).sendMessage(ChatColor.GRAY + "You've received 50 points because the capture point was yours at the end.");
					ItemStack emerald_block = new ItemStack(Material.EMERALD_BLOCK, 10);
					try {
						Bukkit.getPlayer(playername).getInventory().addItem(emerald_block);
					}
					catch (Exception e) {
						Bukkit.getPlayer(playername).getWorld().dropItem(Bukkit.getPlayer(playername).getLocation(), emerald_block);
					}
				//}
				wavecount = 0;
				count = 6;
				plugin.busy = false;
				plugin.raidwave.clear();
				plugin.raidwave2.clear();
				plugin.raidwave3.clear();
			}
		}
	}
	
	/*@EventHandler
	public void onPlayerDeath2(PlayerDeathEvent event) {
		Player player = (Player) event.getEntity();
		Location deadloc = player.getLocation();

		if (deadloc.getWorld() == Bukkit.getWorld("world")
				&& deadloc.getX() > 250.0 && deadloc.getX() < 310
				&& deadloc.getZ() > 1070 && deadloc.getZ() < 1150) {
			Objective scoreboardobj = player.getScoreboard().getObjective(DisplaySlot.PLAYER_LIST);
			if (!scoreboardobj.getName().equalsIgnoreCase(plugin.getConfig().getString("Scoreboard"))) {
				player.sendMessage(ChatColor.RED + "[" + ChatColor.GOLD + "FTC" + ChatColor.RED + "]" + ChatColor.RESET + " You can't do this at this moment!");
				return;
			}
			@SuppressWarnings("deprecation")
			Score score = scoreboardobj.getScore(player);
			int pointslost = (score.getScore() - (int) (score.getScore() * 0.9));
			score.setScore(score.getScore() - pointslost);
			player.sendMessage(ChatColor.RED + "[FTC]" + ChatColor.GRAY + " You've lost " + pointslost + " points because you died here!");
			
			if (player.getKiller() != null && player.getKiller() != player) {
				@SuppressWarnings("deprecation")
				Score killerscore = scoreboardobj.getScore(player.getKiller());
				
				if (killerscore.getScore() < 50) {
					player.getKiller().sendMessage(ChatColor.YELLOW + "[FTC]" + ChatColor.GRAY + " You've gained some of their points! (" + pointslost + " + 10)");
					killerscore.setScore(((int) (killerscore.getScore() + pointslost + 10)));
				}
				else {
					player.getKiller().sendMessage(ChatColor.YELLOW + "[FTC]" + ChatColor.GRAY + " You've gained some of their points! (" + pointslost + ")");
					killerscore.setScore(((int) (killerscore.getScore() + pointslost)));
				}
				
			}
			
		}
			
	}*/
	

	//private void doStuff(EntityDeathEvent event, Set<LivingEntity> raidwave) {
		// OTHERS
		/*event.getDrops().clear();
		raidwave.remove(event.getEntity());
    	if (raidwave.size() < 4) {
        	for (LivingEntity ent : raidwave) {
        		PotionEffect potion = new PotionEffect(PotionEffectType.GLOWING, 9999, 0, false, false);
        		potion.apply(ent);
        	}
    	}
    	
    	// CHECK REQUIREMENTS
		/*if (!(event.getEntity().getKiller() instanceof Player)) return;
		Player player = (Player) event.getEntity().getKiller();
		Objective scoreboardobj = player.getScoreboard().getObjective(DisplaySlot.PLAYER_LIST);
		if (!scoreboardobj.getName().equalsIgnoreCase(plugin.getConfig().getString("Scoreboard"))) {
			player.sendMessage(ChatColor.RED + "[" + ChatColor.GOLD + "FTC" + ChatColor.RED + "]" + ChatColor.RESET + " You can't do this at this moment!");
			return;
		}
		
		// ADDSCORE (3 or 1)
		@SuppressWarnings("deprecation")
		Score score = scoreboardobj.getScore(player);
		event.getEntity().setCustomNameVisible(true);
    	if (player.getName() == playername) {
    		event.getEntity().setCustomName(ChatColor.GREEN + "Kill: " + ChatColor.BOLD + "+3");
			for (int i = 0; i < 3; i++) {
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			        @Override
			        public void run() {
			        	player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.5f);
			        	score.setScore(score.getScore() + 1);
			        }
			    }, 4l * i);
				
			}
    	} 
    	else {
    		event.getEntity().setCustomName(ChatColor.YELLOW + "Kill: " + ChatColor.BOLD + "+1");
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
		        @Override
		        public void run() {
		        	score.setScore(score.getScore() + 1);
		        	player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.3f);
		        }
		    }, 4l);
    	}
	}


	private int count = 6;
	public void loseHealth(BossBar bar, Double d) {
		if (bar == null) return;
    	try {
    		plugin.capturePointHealth.setProgress(plugin.capturePointHealth.getProgress()-d);
    		if (count == 13) {
	    		try {
	    			Bukkit.getPlayer(bar.getTitle().split(ChatColor.YELLOW + "")[1]).playSound(Bukkit.getPlayer(bar.getTitle().split(ChatColor.YELLOW + "")[1]).getLocation(), Sound.BLOCK_CONDUIT_AMBIENT, SoundCategory.MASTER, (float) (0.50-plugin.capturePointHealth.getProgress()), 1.2f);
	    		} catch (Exception ignored) {
	    		}
	    		count = 0;
    		}
    		else {
    			count++;
    		}
    	}
    	catch (IllegalArgumentException ignored) {
    		plugin.capturePointHealth.setProgress(0);
    	}
    	
    	if (plugin.capturePointHealth.getProgress() <= 0) {
			if (plugin.capturePointHealth != null ) {
				plugin.capturePointHealth.setVisible(false);
				plugin.capturePointHealth.removeAll();
				plugin.capturePointHealth = null;
				
				if (plugin.state == 2) {
					plugin.state = 0;
					wavecount = 0;
					count = 6;
					plugin.busy = false;
					for (LivingEntity ent : plugin.raidwave) {
						ent.setHealth(5.0);
						PotionEffect potion = new PotionEffect(PotionEffectType.WITHER, 9999, 5, false, false);
		        		potion.apply(ent);
		        	}
					for (LivingEntity ent : plugin.raidwave2) {
						ent.setHealth(5.0);
						PotionEffect potion = new PotionEffect(PotionEffectType.WITHER, 9999, 5, false, false);
		        		potion.apply(ent);
		        	}
					for (LivingEntity ent : plugin.raidwave3) {
						ent.setHealth(5.0);
						PotionEffect potion = new PotionEffect(PotionEffectType.WITHER, 9999, 5, false, false);
		        		potion.apply(ent);
		        	}
					plugin.raidwave.clear();
					plugin.raidwave2.clear();
					plugin.raidwave3.clear();
				}
				else if (plugin.state == 3) {
					plugin.cap.createCapturePointBossBar(((Player) nearbyEntites.get(0)).getName());
					plugin.state = 1;
					plugin.playername = ((Player) nearbyEntites.get(0)).getName();
				}
			}
		}*/
	}