package net.forthecrown.core.files;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownSerializer;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractSerializer<T extends JavaPlugin> implements CrownSerializer<T> {

    private final T plugin;
    private final Logger logger;

    protected final String fileName;
    protected final String directory;

    protected boolean fileDoesntExist = false;
    protected boolean deleted = false;
    private final boolean stopIfFileDoesntExist;

    private File file;
    private YamlConfiguration fileConfig;

    protected AbstractSerializer(@NotNull String fileName, String directory, @NotNull T plugin){
        this(fileName, directory, false, plugin);
    }
    protected AbstractSerializer(@NotNull String fileName, @NotNull T plugin){
        this(fileName, null, false, plugin);
    }
    protected AbstractSerializer(@NotNull String filename, @Nullable String directory, boolean stopIfFileDoesntExist, @NotNull T plugin){
        Validate.notNull(filename, "The file name cannot be null!");
        Validate.notNull(plugin, "plugin cannot be null");

        this.fileName = filename;
        this.directory = directory;
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.stopIfFileDoesntExist = stopIfFileDoesntExist;

        loadFile();
    }

    private void loadFile(){
        if(directory == null) file = new File(getPlugin().getDataFolder(), fileName + ".yml");
        else file = new File(getPlugin().getDataFolder() + "/" + directory, fileName + ".yml");

        //if the directory doesn't exist and it can't create it
        if(!file.getParentFile().exists() && !file.getParentFile().mkdirs())
            logger.log(Level.SEVERE, "Failed to create " + fileName + " directory, at " + FtcCore.getInstance().getDataFolder() + "/" + directory);

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

    //methods used by child classes
    public void reload(){
        if(deleted) return;
        performFileNullCheck();
        fileConfig = YamlConfiguration.loadConfiguration(file);
    }

    public void save(){
        if(deleted) return;
        performFileNullCheck();

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
        performFileNullCheck();
        if(!file.delete()) logger.log(Level.WARNING, "Couldn't delete file named " + fileName);
        deleted = true;
        logger.log(Level.INFO, "Deleting file named " + fileName + " in " + directory);
    }

    private void performFileNullCheck(){ //believe you me, this is better than getting an exception that just reads: "NullPointerException: null" lol
        if(file == null) throw new NullPointerException(fileName + " in folder " + directory + " is null. Halting reload/save and throwing exception :p");
    }

    public T getPlugin(){
        return plugin;
    }
}
