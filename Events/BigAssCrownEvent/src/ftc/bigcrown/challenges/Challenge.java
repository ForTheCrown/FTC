package ftc.bigcrown.challenges;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Objective;

public interface Challenge {
	
	 Player getPlayer();
	 Objective getScoreboardObjective();
	
	 Location getStartLocation();
	 Location getReturnLocation();
	 void setReturnLocation(Location location);

	 ChallengeType getChallengeType();
	 boolean isChallengeCancelled();
	 
	 void startChallenge();
	 void endChallenge();
	 
	 void sendTitle();
	 
	 void onLogoutWhileInChallenge(PlayerQuitEvent event);
}
