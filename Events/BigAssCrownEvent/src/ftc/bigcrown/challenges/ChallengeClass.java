package ftc.bigcrown.challenges;

import ftc.bigcrown.Main;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.EnumUtils;
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
        ChallengeType actualChal = chalList.get(randomChal);
        
        switch (actualChal){ //switch statements calling a challenge's class. Those classes should be in their own packages and should have some requred fields like the player they're using
            case RACE:
                new RaceChallenge(player);
                break;
            case NETHER:
                player.sendMessage("NETHER");
                break;
            case PINATA:
                player.sendMessage("PINATA");
                break;
            case ENDERMEN:
                player.sendMessage("ENDERMEN");
                break;
            case HALLOWEEN:
                player.sendMessage("HALLOWEEN");
                break;
            case HUNT_BATS:
                player.sendMessage("HUNT_BATS");
                new KillBatChallenge(player);
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
            player.sendMessage("PINATA");
            break;
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
