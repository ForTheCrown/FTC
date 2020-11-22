package ftc.bigcrown.events;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Random;

enum ChallengeEnum{
    RACE, HALLOWEEN, NETHER, MAGMALOVANIA, PROTECT_HAROLD, PVE_ARENA, PINATA, HUNT_BATS, ENDERMEN
}

public class ChallengeClass {

    public Player player;
    public ChallengeEnum chalEnum;

    public ChallengeClass(Player enteredPlayer){
        player = enteredPlayer;;
    }
    private void challangeMethod() {

        ChallengeEnum chalEnum = randomChallenge();
        Location chalLoc = player.getLocation();

        switch (chalEnum){ //use enums, said the brain, it'll be a fun learning experience he said. MISERY IT BECAME
            default: //PINATA is default lol
                chalLoc = new Location(Bukkit.getWorld("world_void"), 10, 10 ,10); //you define a new location here
                break;
            case RACE:
                break;
            case HALLOWEEN:
                break;
            case NETHER:
                break;
            case MAGMALOVANIA:
                break;
            case PROTECT_HAROLD:
                break;
            case PVE_ARENA:
                break;
            case HUNT_BATS:
                break;
            case ENDERMEN:
                break;
        }
        
        player.teleport(chalLoc);
    }

    private ChallengeEnum randomChallenge() {
        return ChallengeEnum.values()[new Random().nextInt(ChallengeEnum.values().length)];
    }
}
