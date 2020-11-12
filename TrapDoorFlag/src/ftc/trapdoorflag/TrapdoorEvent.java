package ftc.trapdoorflag;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;

public class TrapdoorEvent implements Listener {

	public WorldGuardPlugin worldGuardPlugin;

	@EventHandler
	public void onTrapdoorUse(PlayerInteractEvent event) {
		
		Player plr = event.getPlayer();
		Material mat = event.getClickedBlock().getType();
		if(!(mat == Material.OAK_TRAPDOOR ||
				mat == Material.DARK_OAK_TRAPDOOR ||
				mat == Material.SPRUCE_TRAPDOOR ||
				mat == Material.BIRCH_TRAPDOOR ||
				mat == Material.JUNGLE_TRAPDOOR ||
				mat == Material.ACACIA_TRAPDOOR ||
				mat == Material.CRIMSON_TRAPDOOR ||
				mat == Material.WARPED_TRAPDOOR
		)) return; //checks the mats, if they're not a trapdoor, ends the code

		LocalPlayer localPlayer  = WorldGuardPlugin.inst().wrapPlayer(plr); //WorldGuard stuff, this wraps the player into a thingy WorldGuard can use
		boolean canBypass = WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, localPlayer.getWorld()); //This checks if the player can bypass the flag(s)
		Location loc = localPlayer.getLocation(); //WorldGuard has its own Location thingy o.O
		RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer(); //What is this???????????
		RegionQuery query = container.createQuery(); //At this point I don't know
		ApplicableRegionSet set = query.getApplicableRegions(loc);
		boolean canUseEvent = false;
		for (ProtectedRegion region : set){ //Loops through all the regions the player is in, I think. And checks if they're a member of one of them, if they are, it ends the loop and sets the var...
			if(region.isMember(localPlayer)){ // ... to true
				canUseEvent = true;
				break;
			}
		}

		if (!query.testState(loc, localPlayer , Main.TRAPDOOR_USE) && !canBypass && !canUseEvent) { //This actually tests the flag, to see if you should be able to use it or not, along with the other stuffs
			event.setCancelled(true);
			plr.sendMessage( ChatColor.RED + "" + ChatColor.BOLD + "Hey! " + ChatColor.GRAY + "Sorry, but you can't use that here.");
		}

	}

}
