package ftc.trapdoorflag;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;

public class Main extends JavaPlugin {
	
	public static StateFlag TRAPDOOR_USE;
	public Main plugin;
	
	@Override
	public void onEnable() {
		Bukkit.getServer().getPluginManager().registerEvents(new TrapdoorEvent(), this);
	}

	@Override
	public void onLoad(){ //Didn't know this existed lol

		FlagRegistry reg = WorldGuard.getInstance().getFlagRegistry(); //Copy pasted from the WorldGuard documentation lol, I barely know what this does, I understand the base of it

		try {
			StateFlag flag = new StateFlag("trapdoor-use", true);
			reg.register(flag);
			TRAPDOOR_USE = flag;
		} catch (FlagConflictException e) {
			Flag<?> existing = reg.get("trapdoor-use");
			if (existing instanceof StateFlag) {
				TRAPDOOR_USE = (StateFlag) existing;
			}

		}
	}

}
