package net.forthecrown.crownevents.reporters;

import net.forthecrown.core.ForTheCrown;
import net.forthecrown.crownevents.CrownEvent;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ReporterFactory {

    public static EventReporter of(Plugin plugin, CrownEvent event){
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

        try {
            return new CrownEventReporter(plugin, log, event);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static File getReportDirectory(){
        File dir = new File(ForTheCrown.inst().getDataFolder() + File.separator + "eventReports");
        if(!dir.exists()) dir.mkdir();
        if(!dir.isDirectory()) throw new IllegalStateException("Report directory is not a directory");

        return dir;
    }
}
