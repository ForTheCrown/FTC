package ftc.bigcrown.challenges;

import ftc.bigcrown.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChallengeClass implements Listener {

    public Player player;
    public ChallengeType challenge;


    public ChallengeClass(Player enteredPlayer, ChallengeType challenge){ //used in the commands class to enter a challenge for testing
        this.player = enteredPlayer;
        this.challenge = challenge;
        enterChallenge();
    }
    public ChallengeClass(Player enteredPlayer){ //used in the Events class
        player = enteredPlayer;
    }

    //gets a random challenge to use
    public void randomChallenge(){
        List<ChallengeType> chalList = new ArrayList<>();
        for (Map.Entry<ChallengeType, Boolean> entry : Main.plugin.challengeIsFree.entrySet()) {
        	if (!entry.getValue()) chalList.add(entry.getKey());
        }
        int randomChal = Main.plugin.getRandomNumberInRange(0, chalList.size()-1);
        challenge = chalList.get(randomChal);

        enterChallenge();
    }

    //used by /bbe usechallenge to test them
    public void enterChallenge() {
    	switch (challenge) { 
        case RACE:
            new RaceChallenge(player);
            break;
        case NETHER:
            player.sendMessage("NETHER");
            break;
        case PINATA:
            PinataChallenge pC = new PinataChallenge(player);
            Main.plugin.getServer().getPluginManager().registerEvents(pC, Main.plugin);
        case ENDERMEN:
            player.sendMessage("ENDERMEN");
            break;
        case HALLOWEEN:
            player.sendMessage("HALLOWEEN");
            break;
        case HUNT_BATS:
            KillBatChallenge kbc = new KillBatChallenge(player);
            Main.plugin.getServer().getPluginManager().registerEvents(kbc, Main.plugin);
            break;
        case PVE_ARENA:
            player.sendMessage("PVE_ARENA");
            break;
        case MAGMALOVANIA:
            player.sendMessage("MAGMALOVANIA");
            break;
        case PROTECT_HAROLD:
            player.sendMessage("PROTECT_HAROLD");
            break;
        default:
            player.sendMessage("How have you managed this");
            break;
    	}
    }
}
