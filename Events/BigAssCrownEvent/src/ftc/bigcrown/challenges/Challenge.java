package ftc.bigcrown.challenges;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Objective;

public interface Challenge {
	
	public Player getPlayer();
	public Objective getScoreboardObjective();
	
	
	public Location getStartLocation();
	
	public Location getReturnLocation();
	public void setReturnLocation(Location location);

	public void onLogoutWhileInChallenge(PlayerQuitEvent event);
	
	public void startChallenge();
	public void endChallenge();
	
	public void sendTitle();
	
	public void setChallengeInUse(boolean bool);
}
