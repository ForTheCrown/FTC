package net.forthecrown.core.crownevents.reporters;

import net.forthecrown.core.crownevents.types.CrownEvent;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.time.Instant;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CrownEventReporter implements EventReporter {

    private final Plugin plugin;
    private final OutputStreamWriter writer;
    private final Logger logger;
    private final CrownEvent<?> event;

    public CrownEventReporter(Plugin plugin, File file, CrownEvent<?> event) throws FileNotFoundException {
        this.event = event;
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.writer = new OutputStreamWriter(new FileOutputStream(file));
    }

    @Override
    public void close() throws IOException {
        info("Closing EventReporter");

        writer.close();
    }

    @Override
    public void log(Level level, String info) {
        logger.log(level, info);

        StringBuilder serialize = new StringBuilder()
                .append(Date.from(Instant.now()).toString())
                .append("[").append(level.getName()).append("] ")
                .append(info).append("\n");

        try {
            writer.write(serialize.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
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
