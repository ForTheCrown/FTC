package ftc.bigcrown.challenges;

import ftc.bigcrown.Main;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class KillBatChallenge implements Challenge, Listener {
	
	private Player player;
	
	private Location returnLocation;
	private Location startLocation/* = new Location(null, 0, 0, 0)*/; // TODO
	
	private int startScore;
	
	private TimerCountingDown timer;
	
	private boolean challengeCancelled = false;
	
	public KillBatChallenge(Player player) {
		if (player == null || this.getChallengeInUse()) return;
		this.setChallengeInUse(true);
		this.player = player;
		
		// Score before challenge
		Score score = this.getScoreboardObjective().getScore(this.getPlayer().getName());
		this.startScore = score.getScore();

		// Location saving + teleport to challenge;
		setReturnLocation(this.getPlayer().getLocation());

		startLocation = player.getLocation(); // TODO
		startLocation.add(0, 2, 0);

		this.startChallenge();
	}

	public void startChallenge() {
		// Teleport player to challenge:
		this.getPlayer().teleport(getStartLocation());
		this.getPlayer().playSound(getStartLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);

		// TODO: spawn bats
		// spawnBats();

		// Send instruction on what to do:
		this.sendTitle();

		// No countdown, so start timer immediately after title:
		KillBatChallenge kbc = this;
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
	        @Override
	        public void run() {
	        	if (!challengeCancelled) timer = new TimerCountingDown(kbc, 30, false);
	        }
	    }, 70L);
	}

	public void endChallenge() {
		// TODO: remove all bats

		// Timer stopped:
		this.timer = null;
		this.getPlayer().playSound(this.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 2f, 1.5f);

		// Amount of bats caught:
		int score = calculateScore();
		if (score != 1) this.getPlayer().sendMessage(ChatColor.YELLOW + "You've caught " + score + " bats!");
		else this.getPlayer().sendMessage(ChatColor.YELLOW + "You've caught 1 bat!");
		// Add to crown scoreboard:
    	Scoreboard mainScoreboard = Main.plugin.getServer().getScoreboardManager().getMainScoreboard();
    	Score crownScore = mainScoreboard.getObjective("crown").getScore(getPlayer().getName());
    	crownScore.setScore(crownScore.getScore() + score);

		// Teleport to where player opened present:
	    Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, () -> {
			getPlayer().teleport(getReturnLocation().add(0, 1, 0));
			getPlayer().playSound(getReturnLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
			setChallengeInUse(false);
		}, 60L);
	}


	private int calculateScore() {
		Score score = this.getScoreboardObjective().getScore(this.getPlayer().getName());
		return score.getScore() - this.startScore;
	}


	@EventHandler
	public void onLogoutWhileInChallenge(PlayerQuitEvent event) {
		if (getPlayer() == null) return;
		if (event.getPlayer().getName() == this.getPlayer().getName()) {
			if (this.timer != null) {
				this.timer.stopTimer(true);
				this.timer = null;
			}
			// TODO: remove all bats
			this.challengeCancelled = true;
			setChallengeInUse(false);
		    Main.plugin.playersThatQuitDuringChallenge.add(this.getPlayer().getName());
		}

	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		if (event.getEntity().getType() != EntityType.BAT || event.getEntity().getKiller() != this.getPlayer()) return;

		event.getEntity().setCustomNameVisible(true);
		event.getEntity().setCustomName(ChatColor.LIGHT_PURPLE + "+1");
	}


	public Player getPlayer() {
		return this.player;
	}


	public Objective getScoreboardObjective() {
		Scoreboard mainScoreboard = Main.plugin.getServer().getScoreboardManager().getMainScoreboard();
		return mainScoreboard.getObjective("batsKilled");
	}


	public Location getStartLocation() {
		return this.startLocation;
	}


	public Location getReturnLocation() {
		return this.returnLocation;
	}


	public void setReturnLocation(Location location) {
		this.returnLocation = location;
	}

	public void sendTitle() {
		this.getPlayer().sendTitle(ChatColor.YELLOW + "Kill Bats!", ChatColor.GOLD + "January Event", 5, 60, 5);
	}


	public void setChallengeInUse(boolean bool) {
		Main.plugin.challengeIsFree.replace(getChallengeType(), bool);
	}
	
	public boolean getChallengeInUse() {
		return Main.plugin.challengeIsFree.get(getChallengeType());
	}
	
	public ChallengeType getChallengeType() {
		return ChallengeType.HUNT_BATS;
	}

	@Override
	public boolean isChallengeCancelled() {
		return challengeCancelled;
	}
}
