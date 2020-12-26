package ftc.bigcrown.challenges;

import ftc.bigcrown.Main;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Rabbit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Score;
import org.bukkit.util.Vector;

public class PinataChallenge extends GenericChallenge implements Challenge, Listener {

	private TimerCountingDown timer;
    public boolean canDrop = true;
    //private Location startLocation = new Location(Bukkit.getWorld("world_void"), 349.5, 162, -289.5, -45, 0);
    private Location startLocation = new Location(Bukkit.getWorld("world"), 349.5, 162, -289.5, -45, 0);

    public PinataChallenge(Player player) {
        super(player, ChallengeType.PINATA);
        if (Main.plugin.getChallengeInUse(getChallengeType())) return;
        
        // All needed setters from super class:
 		setObjectiveName("pinataHits");
 		setReturnLocation(getPlayer().getLocation());
 		setStartLocation(this.startLocation);
 		setStartScore();

        startChallenge();
    }

    public void startChallenge() {
    	// Teleport player to challenge:
    	getPlayer().teleport(getStartLocation());
    	getPlayer().playSound(getStartLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);

    	// Add player to score map
		scoreMap.put(getPlayer().getUniqueId(), 0);
    	
    	sendTitle();
    	// No countdown, so start timer immediately after title:
    	PinataChallenge pc = this;
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
	        @Override
	        public void run() {
	        	if (!isChallengeCancelled()) timer = new TimerCountingDown(pc, 60, false);
	        }
	    }, 70L);
    }
    
    public void endChallenge() {
		getPlayer().playSound(getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 2f, 1.5f);

		// Amount of pinata hits:
		int score = scoreMap.get(getPlayer().getUniqueId());
		if (score == 0) {
			getPlayer().sendMessage(ChatColor.GRAY + "You've hit the Pinata 0 times!");
			getPlayer().sendMessage(ChatColor.YELLOW + "Who could hurt such a cutie??");
			Score playerScore = getScoreboardObjective().getScore(getPlayer().getName());
			playerScore.setScore(playerScore.getScore() + 10);
			getPlayer().sendMessage(ChatColor.YELLOW + "You've earned 10 points for not hurting the Pinata.");
		}
		else if (score != 1) getPlayer().sendMessage(ChatColor.YELLOW + "You've hit the Pinata " + score + " times!");
		else getPlayer().sendMessage(ChatColor.YELLOW + "You've hit the Pinata 1 time!");

		// If their current score is bigger than their record score
		if(isRecordSmallerThanScore()){
			Score playerScore = getRecordScoreboardObjective().getScore(getPlayer().getName());
			playerScore.setScore(scoreMap.get(getPlayer().getUniqueId()));
			scoreMap.remove(getPlayer().getUniqueId());
		}

		calculatePlayerScore();
		teleportBack();
		EntityDamageByEntityEvent.getHandlerList().unregister(this);
		PlayerQuitEvent.getHandlerList().unregister(this);
		
	}
    

    public void sendTitle() {
        this.getPlayer().sendTitle(ChatColor.YELLOW + "Punch the Pinata!", ChatColor.GOLD + "March Event", 5, 60, 5);
    }

    
    
    @EventHandler
    public void onPlayerHitPinata(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Rabbit && event.getDamager() instanceof Player)) return;
        
        Player player = (Player) event.getDamager();
        if (player.getName() != getPlayer().getName()) return;
        
        Rabbit bunny = (Rabbit) event.getEntity();
        if (bunny.getCustomName() == null || (!bunny.getCustomName().contains(ChatColor.YELLOW + "P"))) return;

        scoreMap.put(getPlayer().getUniqueId(), scoreMap.get(getPlayer().getUniqueId()) + 1);
        Score score = getScoreboardObjective().getScore(getPlayer().getName());
        score.setScore(score.getScore() + 1);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.2f, 1.0f);

        if (!canDrop) return;

        double random = Math.random();
        if (random < 0.33) {
            Item gold = player.getWorld().dropItem(bunny.getLocation(), new ItemStack(Material.GOLD_INGOT));
            gold.setVelocity(new Vector(0, 0.2, 0));
        } else if (random > 0.85) {
            Item diamond = player.getWorld().dropItem(bunny.getLocation(), new ItemStack(Material.DIAMOND));
            diamond.setVelocity(new Vector(0, 0.2, 0));
        } else return;

        canDrop = false;
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, () -> canDrop = true, 100L);
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
}
