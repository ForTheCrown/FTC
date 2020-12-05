package ftc.bigcrown.challenges;

import ftc.bigcrown.Main;
import org.bukkit.*;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Score;
import org.bukkit.util.BoundingBox;

import java.util.Timer;
import java.util.TimerTask;

public class KillBatChallenge extends GenericChallenge implements Challenge, Listener {
	
	private Location startLocation = new Location(Bukkit.getWorld("world_void"), 377.5, 152, -368.5);
	private TimerCountingDown timer;
	private Timer batSpawnTimer;
	
	public KillBatChallenge(Player player) {
		super(player, ChallengeType.HUNT_BATS);
		if (getPlayer() == null || Main.plugin.getChallengeInUse(getChallengeType())) return;

		// All needed setters from super class:
		setObjectiveName("batsKilled");
		setReturnLocation(this.getPlayer().getLocation());
		setStartLocation(this.startLocation);
		setStartScore();

		startChallenge();
	}

	public void startChallenge() {
		// Teleport player to challenge:
		getPlayer().teleport(getStartLocation());
		getPlayer().playSound(getStartLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);

		sendTitle();
		// No countdown, so start timer immediately after title:
		KillBatChallenge kbc = this;
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
	        @Override
	        public void run() {
	        	if (!isChallengeCancelled()) {
	        		timer = new TimerCountingDown(kbc, 30, false);
	        		spawnBats();
	        	}
	        }
	    }, 70L);
	}


	public void endChallenge() {
		killBats();

		// Timer stopped:
		this.timer = null;
		getPlayer().playSound(getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 2f, 1.5f);

		// Amount of bats caught:
		int score = calculateScore();
		if (score != 1) getPlayer().sendMessage(ChatColor.YELLOW + "You've caught " + score + " bats!");
		else getPlayer().sendMessage(ChatColor.YELLOW + "You've caught 1 bat!");
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
	}

	public void sendTitle() {
		this.getPlayer().sendTitle(ChatColor.YELLOW + "Kill Bats!", ChatColor.GOLD + "January Event", 5, 60, 5);
	}
	
	
	private void spawnBats() {
		for (int i = -5; i < 6; i++) {
			Location loc = getStartLocation().clone();
			loc.setX(loc.getX() + (0.1*i));
			loc.setY(loc.getY() + 2.5);
			loc.setZ(loc.getZ() + (0.1*i));

			Bat bat = loc.getWorld().spawn(loc, Bat.class);
			bat.setHealth(1);
		}

		batSpawnTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				Location loc = getStartLocation().clone();
				loc.setX(loc.getX() + (0.1*Main.plugin.getRandomNumberInRange(-12, 13)));
				loc.setY(loc.getY() + 2.5);
				loc.setZ(loc.getZ() + (0.1*Main.plugin.getRandomNumberInRange(-12, 13)));

				Bat bat = loc.getWorld().spawn(loc, Bat.class);
				bat.setHealth(1);
			}
		}, 0, 750);
	}
	
	private void killBats() {
		BoundingBox box = new BoundingBox(371, 150, -375, 384, 162, -363);
		for (Entity ent : getStartLocation().getWorld().getNearbyEntities(box)) {
			if (ent instanceof Bat) {
				ent.teleport(ent.getLocation().add(0, -300, 0));
			}
		}
		batSpawnTimer.cancel();
		batSpawnTimer.purge();
	}

	@EventHandler
	public void onLogoutWhileInChallenge(PlayerQuitEvent event) {
		if (getPlayer() == null) return;
		if (event.getPlayer().getName() == this.getPlayer().getName()) {
			if (this.timer != null) {
				this.timer.stopTimer(true);
				this.timer = null;
			}
			killBats();
		}
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		if (event.getEntity().getKiller() == null || this.getPlayer() == null) return;
		if (event.getEntity().getType() != EntityType.BAT || event.getEntity().getKiller().getName() != this.getPlayer().getName()) return;

		event.getEntity().setCustomNameVisible(true);
		event.getEntity().setCustomName(ChatColor.GOLD + "" + ChatColor.BOLD + "+1");
	}
}
