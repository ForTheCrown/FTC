package net.forthecrown.core;

import net.forthecrown.dungeons.Bosses;
import net.forthecrown.economy.Economy;
import net.forthecrown.events.MobHealthBar;
import net.forthecrown.user.packet.PacketListeners;
import net.forthecrown.utils.world.WorldLoader;
import net.forthecrown.vars.VarRegistry;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.dynmap.DynmapCommonAPIListener;

import static net.forthecrown.core.FtcDiscord.C_SERVER;
import static net.forthecrown.utils.Util.runSafe;

/**
 * Main class that does all the dirty internal stuff
 */
public final class Main extends JavaPlugin implements Crown {
    public static final String
            NAME            = "ForTheCrown",
            NAMESPACE       = NAME.toLowerCase(),
            OLD_NAMESPACE   = "ftccore";

    static VarRegistry              varRegistry;
    static Economy                  economy;
    static Announcer                announcer;
    static FtcConfig                config;

    static ServerRules              rules;
    static JoinInfo                 joinInfo;

    boolean debugMode;

    @Override
    public void onEnable() {
        setDebugMode();
        BootStrap.enable();

        FtcDiscord.staffLog(C_SERVER, "FTC started, plugin version: {}, paper version: {}",
                getDescription().getVersion(),
                Bukkit.getVersion()
        );

        Crown.logger().info("FTC started");
    }

    @Override
    public void onLoad() {
        // Register dynmap hook connection thing
        DynmapCommonAPIListener.register(new FtcDynmap());

        setDebugMode();
        Crown.logger().info("Debug mode={}", debugMode);

        BootStrap.initConfig();
        BootStrap.initVars();
        BootStrap.load();
    }

    void setDebugMode() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(
                getTextResource("plugin.yml")
        );

        debugMode = config.getBoolean("debug_build");
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);

        AutoSave.get().run();

        for (Player p: Bukkit.getOnlinePlayers()) {
            p.closeInventory();
        }

        runSafe(MobHealthBar::shutdown);
        runSafe(Bosses::shutdown);
        runSafe(WorldLoader::shutdown);
        runSafe(PacketListeners::removeAll);

        BootStrap.loaded = false;
        FtcDiscord.staffLog(C_SERVER, "FTC shutting down");
    }

    @Override
    public @NonNull String namespace() {
        return NAMESPACE;
    }
}