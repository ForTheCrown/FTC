package net.forthecrown.poshd;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.forthecrown.crownevents.EventTimer;
import net.forthecrown.poshd.command.CommandStartTimer;
import net.forthecrown.poshd.command.CommandStopTimer;
import net.forthecrown.royalgrenadier.RoyalGrenadier;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class Main extends JavaPlugin {
    public static final Map<UUID, EventTimer> TIMERS = new Object2ObjectOpenHashMap<>();
    public static Main inst;
    public static Logger logger;

    @Override
    public void onEnable() {
        inst = this;
        logger = getLogger();
        RoyalGrenadier.initialize();

        new CommandStartTimer();
        new CommandStopTimer();
    }

    @Override
    public void onDisable() {
    }
}
