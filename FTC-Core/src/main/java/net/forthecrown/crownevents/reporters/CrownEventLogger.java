package net.forthecrown.crownevents.reporters;

import net.forthecrown.crownevents.CrownEvent;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.apache.logging.log4j.spi.ExtendedLoggerWrapper;
import org.bukkit.plugin.Plugin;


public class CrownEventLogger extends ExtendedLoggerWrapper implements EventLogger {
    private final Plugin plugin;
    private final CrownEvent<?> event;

    public CrownEventLogger(Plugin plugin, CrownEvent<?> event) {
        super((ExtendedLogger) plugin.getLog4JLogger(), plugin.getName(), plugin.getLog4JLogger().getMessageFactory());
        this.event = event;
        this.plugin = plugin;
    }

    @Override
    public Logger getBaseLogger() {
        return logger;
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public CrownEvent<?> getEvent() {
        return event;
    }
}
