package net.forthecrown.serializer;

import net.forthecrown.core.Crown;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Makes serialization easier for subclasses
 */
public abstract class AbstractYamlSerializer implements CrownSerializer {

    private static final Logger logger = Crown.logger();

    protected final String fileName;
    protected final String directory;

    protected boolean fileDoesntExist = false;
    protected boolean deleted = false;
    protected final boolean stopIfFileDoesntExist;

    private File file;
    private YamlConfiguration fileConfig;

    protected AbstractYamlSerializer(@NotNull String fileName, String directory){
        this(fileName, directory, false);
    }
    protected AbstractYamlSerializer(@NotNull String fileName){
        this(fileName, null, false);
    }
    protected AbstractYamlSerializer(@NotNull String filename, @Nullable String directory, boolean stopIfFileDoesntExist){
        Validate.notNull(filename, "The file name cannot be null!");

        this.fileName = filename;
        this.directory = directory;
        this.stopIfFileDoesntExist = stopIfFileDoesntExist;

        loadOrCreate();
    }

    private void loadOrCreate(){
        if(directory == null) file = new File(Crown.dataFolder(), fileName + ".yml");
        else file = new File(Crown.dataFolder() + "/" + directory, fileName + ".yml");

        //if the directory doesn't exist and it can't create it
        if(!file.getParentFile().exists() && !file.getParentFile().mkdirs())
            logger.log(Level.SEVERE, "Failed to create " + fileName + " directory, at " + Crown.inst().getDataFolder() + "/" + directory);

        if(!file.exists()){
            fileDoesntExist = true;
            if(stopIfFileDoesntExist) return;

            try{
                file.createNewFile();
                logger.log(Level.INFO, "Creating file " + fileName + " in " + directory);
            }catch (IOException e){
                logger.log(Level.SEVERE, "Failed to create " + fileName + " in " +directory);
                e.printStackTrace();
            }
        }
        fileConfig = YamlConfiguration.loadConfiguration(file);
    }

    public void reload(){
        if(deleted) return;
        checkFileNull();
        fileConfig = YamlConfiguration.loadConfiguration(file);
        reloadFile();
    }

    public void save(){
        save(true);
    }

    public void save(boolean subClassToo){
        if(deleted) return;
        checkFileNull();

        if(subClassToo) saveFile();

        try{
            fileConfig.save(file);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    protected YamlConfiguration getFile(){
        return fileConfig;
    }

    protected void delete(){
        checkFileNull();
        if(!file.delete()) logger.log(Level.WARNING, "Couldn't delete file named " + fileName);
        deleted = true;
        logger.log(Level.INFO, "Deleting file named " + fileName + " in " + directory);
    }

    private void checkFileNull(){ //believe you me, this is better than getting an exception that just reads: "NullPointerException: null" lol
        if(file == null) throw new NullPointerException(fileName + " in folder " + directory + " is null. Halting reload/save and throwing exception :p");
    }

    protected abstract void saveFile();
    protected abstract void reloadFile();
}
