package net.forthecrown.core.files;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownFileManager;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public abstract class FtcFileManager<T extends JavaPlugin> implements CrownFileManager<T> {

    private final T plugin;

    protected final String fileName;
    protected final String directory;

    protected boolean fileDoesntExist = false;
    protected boolean deleted = false;
    private final boolean stopIfFileDoesntExist;

    private File file;
    private YamlConfiguration fileConfig;

    protected FtcFileManager(String fileName, String directory, T plugin){
        this.directory = directory;
        this.fileName = fileName;
        this.plugin = plugin;
        stopIfFileDoesntExist = false;

        loadFile();
    }
    protected FtcFileManager(String fileName, T plugin){
        this.fileName = fileName;
        this.directory = null;
        this.plugin = plugin;
        stopIfFileDoesntExist = false;

        loadFile();
    }
    protected FtcFileManager(String filename, String directory, boolean stopIfFileDoesntExist, T plugin){
        this.fileName = filename;
        this.directory = directory;
        this.plugin = plugin;
        this.stopIfFileDoesntExist = stopIfFileDoesntExist;

        loadFile();
    }

    private void loadFile(){
        if(directory == null) file = new File(getPlugin().getDataFolder(), fileName + ".yml");
        else file = new File(getPlugin().getDataFolder() + "/" + directory, fileName + ".yml");

        //if the directory doesn't exist and it can't create it
        if(!file.getParentFile().exists() && !file.getParentFile().mkdirs())
            getPlugin().getLogger().log(Level.SEVERE, "Failed to create " + fileName + " directory, at " + FtcCore.getInstance().getDataFolder() + "/" + directory);

        if(!file.exists()){
            fileDoesntExist = true;
            if(stopIfFileDoesntExist) return;

            try{
                file.createNewFile();
                getPlugin().getLogger().log(Level.INFO, "Creating file " + fileName + " in " + directory);
            }catch (IOException e){
                getPlugin().getLogger().log(Level.SEVERE, "Failed to create " + fileName + " in " +directory);
                e.printStackTrace();
            }
        }
        fileConfig = YamlConfiguration.loadConfiguration(file);
    }

    //methods used by child classes
    public void reload(){
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
        if(!file.delete()) getPlugin().getLogger().log(Level.WARNING, "Couldn't delete file named " + fileName);
        deleted = true;
        getPlugin().getLogger().log(Level.INFO, "Deleting file named " + fileName + " in " + directory);
    }

    private void performFileNullCheck(){ //believe you me, this is better than getting an exception that just reads: "NullPointerException: null" lol
        if(file == null) throw new NullPointerException(fileName + " in folder " + directory + " is null. Halting reload/save and throwing exception :p");
    }

    public T getPlugin(){
        return plugin;
    }
}
