package net.forthecrown.julyevent;

import net.forthecrown.core.crownevents.ObjectiveLeaderboard;
import org.bukkit.plugin.java.JavaPlugin;

public final class JulyMain extends JavaPlugin {
    public static JulyMain inst;

    public static ObjectiveLeaderboard leaderboard;
    public static JulyEvent event;

    @Override
    public void onEnable() {
        inst = this;

        event = new JulyEvent();
    }

    @Override
    public void onDisable() {
    }
}
