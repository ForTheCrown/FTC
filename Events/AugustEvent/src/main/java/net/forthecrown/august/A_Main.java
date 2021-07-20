package net.forthecrown.august;

import net.forthecrown.crownevents.reporters.EventReporter;
import net.forthecrown.crownevents.reporters.ReporterFactory;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Logger;

public class A_Main extends JavaPlugin {

    public static A_Main inst;

    public static AugustEvent event;

    public static Logger logger;
    public static EventReporter reporter;

    @Override
    public void onEnable() {
        inst = this;
        logger = getLogger();

        event = new AugustEvent();
        reporter = ReporterFactory.of(this, event);
    }

    @Override
    public void onDisable() {
        if(AugustEvent.currentEntry != null) event.end(AugustEvent.currentEntry);

        try {
            reporter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
