package ftc.bigcrown.challenges;

import ftc.bigcrown.Main;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class KillBatChallenge extends GenericChallenge implements Challenge, Listener {
	
	private Location startLocation = new Location(Bukkit.getWorld("world"), -4.5, 5, 37.5); // TODO
	private TimerCountingDown timer;
	
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

		// TODO: spawn bats
		// spawnBats();

		sendTitle();
		// No countdown, so start timer immediately after title:
		KillBatChallenge kbc = this;
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
	        @Override
	        public void run() {
	        	if (!isChallengeCancelled()) timer = new TimerCountingDown(kbc, 30, false);
	        }
	    }, 70L);
	}

	public void endChallenge() {
		// TODO: remove all bats

		// Timer stopped:
		this.timer = null;
		getPlayer().playSound(getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 2f, 1.5f);

		// Amount of bats caught:
		int score = calculateScore();
		if (score != 1) getPlayer().sendMessage(ChatColor.YELLOW + "You've caught " + score + " bats!");
		else getPlayer().sendMessage(ChatColor.YELLOW + "You've caught 1 bat!");
		// Add to crown scoreboard:
    	Scoreboard mainScoreboard = Main.plugin.getServer().getScoreboardManager().getMainScoreboard();
    	Score crownScore = mainScoreboard.getObjective("crown").getScore(getPlayer().getName());
    	crownScore.setScore(crownScore.getScore() + score);

    	teleportBack();
	}
	
	public void sendTitle() {
		this.getPlayer().sendTitle(ChatColor.YELLOW + "Kill Bats!", ChatColor.GOLD + "January Event", 5, 60, 5);
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
		}

	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		if (event.getEntity().getKiller() == null || this.getPlayer() == null) return;
		if (event.getEntity().getType() != EntityType.BAT || event.getEntity().getKiller().getName() != this.getPlayer().getName()) return;

		event.getEntity().setCustomNameVisible(true);
		event.getEntity().setCustomName(ChatColor.GOLD + "" + ChatColor.BOLD + "+1");
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (this.getPlayer() == null) return;
		if (event.getEntity().getName() == this.getPlayer().getName()) {
			if (this.timer != null) {
				this.timer.stopTimer(true);
				this.timer = null;
			}
			setChallengeCancelled(true);
            Main.plugin.setChallengeInUse(getChallengeType(), false);
            Main.plugin.playersThatQuitDuringChallenge.add(getPlayer().getName());
			this.getPlayer().sendMessage(ChatColor.GRAY + "Challenge failed! No points earned.");
		}
	}
}
