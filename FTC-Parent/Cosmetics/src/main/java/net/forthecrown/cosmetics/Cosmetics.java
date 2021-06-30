package net.forthecrown.cosmetics;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import net.forthecrown.cosmetics.commands.CommandCosmetics;
import net.forthecrown.cosmetics.custominvs.CustomInv;
import net.forthecrown.cosmetics.effects.arrow.effects.CosmeticArrowEffect;
import net.forthecrown.cosmetics.effects.death.effects.CosmeticDeathEffect;
import org.bukkit.plugin.java.JavaPlugin;

public final class Cosmetics extends JavaPlugin {

    public static StateFlag PLAYER_RIDING_ALLOWED;
    private static PlayerRidingManager rideManager;

    private static Cosmetics plugin = null;
    public static synchronized Cosmetics getPlugin() {
        if (plugin == null) plugin = new Cosmetics();
        return plugin;
    }
    private Cosmetics() {}

    public void onEnable() {
        // Config
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        rideManager = new PlayerRidingManager();

        // Events
        getServer().getPluginManager().registerEvents(new CosmeticEvents(), this);
        getServer().getPluginManager().registerEvents(CosmeticDeathEffect.listener, this);
        getServer().getPluginManager().registerEvents(CosmeticArrowEffect.listener, this);
        getServer().getPluginManager().registerEvents(CustomInv.listener, this); // TODO: move to core


        // Command
        new CommandCosmetics();
    }

    @Override
    public void onDisable() {
        getRideManager().stopAllRiding();
    }

    @Override
    public void onLoad() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StateFlag flag = new StateFlag("player-riding", true);
            registry.register(flag);
            PLAYER_RIDING_ALLOWED = flag;
        } catch (FlagConflictException e){
            e.printStackTrace();
        }
    }

    public static PlayerRidingManager getRideManager() {
        return rideManager;
    }


    /*public static final Set<Particle> ACCEPTED_ARROW_PARTICLES = new HashSet<>(Arrays.asList(
            Particle.FLAME, Particle.SNOWBALL, Particle.SNEEZE,
            Particle.HEART, Particle.DAMAGE_INDICATOR, Particle.DRIPPING_HONEY,
            Particle.CAMPFIRE_COSY_SMOKE, Particle.SOUL, Particle.FIREWORKS_SPARK));

    public static final Set<String> ACCEPTED_DEATH_PARTICLES = new HashSet<>(
            Arrays.asList("SOUL", "TOTEM", "EXPLOSION", "ENDER_RING"));*/
}
