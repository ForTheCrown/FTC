package ftc.bigcrown.challenges;

import ftc.bigcrown.Main;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Score;

public class KillEndermenChallenge extends GenericChallenge implements Challenge, Listener {
	
	private TimerCountingDown timer;
	private Location startLocation = new Location(Bukkit.getWorld("world_the_end"), 0.5, 62, -8.5, 0, 0);
	
	public KillEndermenChallenge(Player player) {
		super(player, ChallengeType.ENDERMEN);
		if (Main.plugin.getChallengeInUse(getChallengeType())) return;

		// All needed setters from super class:
 		setObjectiveName("endKilled");
 		setReturnLocation(getPlayer().getLocation());
 		setStartLocation(this.startLocation);
 		setStartScore();

		this.startChallenge();
	}

	public void startChallenge() {
		// Teleport player to challenge:
		this.getPlayer().teleport(getStartLocation());
		this.getPlayer().playSound(getStartLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);

		// Send instruction on what to do:
		this.sendTitle();

		//adds player to score map
		scoreMap.put(getPlayer().getUniqueId(), getStartScore());

		// Countdown, so start timer immediately after title:
		KillEndermenChallenge kec = this;
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, () -> {
			if (!isChallengeCancelled()) timer = new TimerCountingDown(kec, 60, true);
		}, 50L);
	}

	public void endChallenge() {
		// Timer stopped:
		this.timer = null;
		getPlayer().playSound(this.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 2f, 1.5f);

		// Amount of endermen killed:
		int score = calculateScore();
		if (score != 1) this.getPlayer().sendMessage(ChatColor.YELLOW + "You've killed " + score + " endermen!");
		else this.getPlayer().sendMessage(ChatColor.YELLOW + "You've killed 1 enderman!");
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
		getPlayer().sendMessage(ChatColor.GRAY + "The score gets added at the end of the challenge, unless you died!");
		getPlayer().sendTitle(ChatColor.YELLOW + "Kill Endermen!", ChatColor.GOLD + "January February May", 5, 60, 5);
	}

	@EventHandler
	public void onLogoutWhileInChallenge(PlayerQuitEvent event) {
		if (getPlayer() == null) return;
		if (event.getPlayer().getName() == this.getPlayer().getName()) {
			if (this.timer != null) {
				this.timer.stopTimer(true);
				this.timer = null;
			}
		}

	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		if (event.getEntity().getKiller() == null || this.getPlayer() == null) return;
		if (event.getEntity().getType() != EntityType.ENDERMAN || event.getEntity().getKiller().getName() != this.getPlayer().getName()) return;

		event.getEntity().setCustomNameVisible(true);
		event.getEntity().setCustomName(ChatColor.GOLD + "" + ChatColor.BOLD + "+1");
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (getPlayer() == null) return;
		if (event.getEntity().getName() == getPlayer().getName()) {
			if (this.timer != null) {
				this.timer.stopTimer(true);
				this.timer = null;
			}
			setChallengeCancelled(true);
			Main.plugin.setChallengeInUse(getChallengeType(), false);
			Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, () -> {
				getPlayer().sendMessage(ChatColor.GRAY + "Challenge failed! No points earned.");
			}, 20);
		}
	}

	

}
