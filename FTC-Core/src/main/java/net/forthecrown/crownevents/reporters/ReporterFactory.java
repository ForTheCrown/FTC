package net.forthecrown.crownevents.reporters;

import net.forthecrown.core.Crown;
import net.forthecrown.crownevents.CrownEvent;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class ReporterFactory {

    public static EventLogger of(Plugin plugin, CrownEvent event){
        File dir = getReportDirectory();

        File log = new File(dir, event.getName() + "_" + dir.list().length + ".txt");
        if(!log.exists()) {
            try {
                log.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        return new CrownEventLogger(plugin, event);
    }

    public static File getReportDirectory() {
        File dir = new File(Crown.inst().getDataFolder() + File.separator + "eventReports");
        if(!dir.exists()) dir.mkdir();
        if(!dir.isDirectory()) throw new IllegalStateException("Report directory is not a directory");

        return dir;
    }
}
