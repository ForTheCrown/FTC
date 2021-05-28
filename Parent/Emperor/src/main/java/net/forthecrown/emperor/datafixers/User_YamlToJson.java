package net.forthecrown.emperor.datafixers;

import net.forthecrown.emperor.CrownCore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class User_YamlToJson {
    public static void runAsync(){
        CompletableFuture.runAsync(() -> {
            try {
                new User_YamlToJson().start().finish();
            } catch (IOException e){
                e.printStackTrace();
            }
        });
    }

    private final OutputStreamWriter writer;

    private final File dir;
    private final Logger logger;
    int updated = 0;

    public User_YamlToJson() throws IOException {
        this.dir = new File(CrownCore.dataFolder() + File.separator + "playerdata" + File.separator);
        this.logger = CrownCore.logger();

        File outputLog = new File(CrownCore.dataFolder() + File.separator + "user_yaml_json_log.txt");
        if(!outputLog.exists()) outputLog.createNewFile();
        writer = new OutputStreamWriter(new FileOutputStream(outputLog));
    }

    public User_YamlToJson start() throws IOException{

        return this;
    }

    public void info(String info) throws IOException {
        log(Level.INFO, info);
    }

    public void log(Level level, String info) throws IOException {
        logger.log(level, info);
        writer.write("[" + level.getName() + "] " + info);
    }

    public void finish() throws IOException{
        writer.close();
    }
}
