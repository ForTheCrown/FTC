package ftc.bigcrown.challenges;

import ftc.bigcrown.Main;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.EnumUtils;
import org.bukkit.entity.Player;

import java.util.List;

public class ChallengeClass {

    public Player player;
    public Enum challenge;


    public ChallengeClass(Player enteredPlayer, Enum challenge){ //used in the commands class to enter a challenge for testing
        player = enteredPlayer;
        this.challenge = challenge;
        enterChallenge();
    }
    public ChallengeClass(Player enteredPlayer){ //used in the Events class
        player = enteredPlayer;
    }

    //gets a random challenge to use
    public void randomChallenge(){
        List<Challenge> chalList = EnumUtils.getEnumList(Challenge.class);
        int randomChal = Main.plugin.getRandomNumberInRange(0, chalList.size()-1);
        Challenge actualChal = chalList.get(randomChal);
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
    private static void enterChallenge(){

    }
}
