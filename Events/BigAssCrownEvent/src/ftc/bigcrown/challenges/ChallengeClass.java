package ftc.bigcrown.challenges;

import ftc.bigcrown.Main;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

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
        for (ChallengeType type : Main.plugin.challengeIsFree.keySet()) {
        	if (!Main.plugin.getChallengeInUse(type)) chalList.add(type);
        }
        int randomChal = Main.plugin.getRandomNumberInRange(0, chalList.size()-1);
        challenge = chalList.get(randomChal);

        enterChallenge();
        Main.plugin.setChallengeInUse(challenge, true);
    }

    //used by /bbe usechallenge to test them
    public void enterChallenge() {
    	switch (challenge) { 
        case RACE:
        	player.sendMessage(ChatColor.GRAY + "You've found a " + ChatColor.YELLOW + "/race" + ChatColor.GRAY + " ticket!");
    		
    		ItemStack ticket = Main.plugin.makeItem(Material.MOJANG_BANNER_PATTERN, 1, false, ChatColor.YELLOW + "Race Ticket", 
    				ChatColor.GOLD + "Hold this in your hand and click the Start!",
    				ChatColor.GOLD + "villager to start a Race!",
    				ChatColor.GRAY + "You can do /race to go there, Good luck!");
    		
    		Item ticketItem = player.getLocation().getWorld().dropItem(player.getLocation(), ticket);
    		ticketItem.setVelocity(new Vector(0, 0.2, 0));
            break;
        case PINATA:
        	PinataChallenge pC = new PinataChallenge(player);
            Main.plugin.getServer().getPluginManager().registerEvents(pC, Main.plugin);
            break;
        case ENDERMEN:
            KillEndermenChallenge kbe = new KillEndermenChallenge(player);
            Main.plugin.getServer().getPluginManager().registerEvents(kbe, Main.plugin); 
            break;
        case HUNT_BATS:
            KillBatChallenge kbc = new KillBatChallenge(player);
            Main.plugin.getServer().getPluginManager().registerEvents(kbc, Main.plugin);
            break;
        case PVE_ARENA:
        	CastleRaidChallenge crc = new CastleRaidChallenge(player);
            Main.plugin.getServer().getPluginManager().registerEvents(crc, Main.plugin);
            break;
        case PROTECT_HAROLD:
        	ProtectHaroldChallenge phc = new ProtectHaroldChallenge(player);
            Main.plugin.getServer().getPluginManager().registerEvents(phc, Main.plugin);
            break;
        default:
            player.sendMessage("How have you managed this");
            break;
    	}
    }
}
