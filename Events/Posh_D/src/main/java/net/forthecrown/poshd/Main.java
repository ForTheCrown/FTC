package net.forthecrown.poshd;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.forthecrown.crown.EventTimer;
import net.forthecrown.grenadier.RoyalArguments;
import net.forthecrown.grenadier.VanillaArgumentType;
import net.forthecrown.poshd.command.*;
import net.forthecrown.royalgrenadier.RoyalGrenadier;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class Main extends JavaPlugin {
    public static final Map<UUID, EventTimer> TIMERS = new Object2ObjectOpenHashMap<>();
    public static Main inst;
    public static Logger logger;
    public static Leaderboards leaderboards;

    @Override
    public void onEnable() {
        inst = this;
        logger = getLogger();
        RoyalGrenadier.initialize();
        RoyalArguments.register(ChatArgument.class, VanillaArgumentType.GREEDY_STRING);

        leaderboards = new Leaderboards();
        leaderboards.reload();

        saveResource("messages.properties", false);
        Messages.load(this);

        new CommandLeaveParkour();
        new CommandStartTimer();
        new CommandStopTimer();
        new CommandCheckPoint();
        new CommandLeaderboard();
    }

    @Override
    public void onDisable() {
        leaderboards.save();
    }

}
