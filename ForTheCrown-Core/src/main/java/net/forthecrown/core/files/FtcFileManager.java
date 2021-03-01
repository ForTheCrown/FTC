package net.forthecrown.core.files;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Announcer;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public abstract class FtcFileManager {

    protected final String fileName;
    protected final String directory;

    protected boolean fileDoesntExist = false;
    protected boolean deleted = false;

    private File file;
    private YamlConfiguration fileConfig;

    protected FtcFileManager(String fileName, String folder){
        this.directory = folder;
        this.fileName = fileName;
        loadFile();
    }
    protected FtcFileManager(String fileName){
        directory = null;
        this.fileName = fileName;
        loadFile();
    }

    private void loadFile(){
        if(directory == null) file = new File(FtcCore.getInstance().getDataFolder(), fileName + ".yml");
        else file = new File(FtcCore.getInstance().getDataFolder() + "/" + directory, fileName + ".yml");

        //if the directory doesn't exist and it can't create it
        if(!file.getParentFile().exists() && !file.getParentFile().mkdirs())
            FtcCore.getInstance().getLogger().log(Level.SEVERE, "Failed to create " + fileName + " directory, at " + FtcCore.getInstance().getDataFolder() + "/" + directory);

        if(!file.exists()){
            fileDoesntExist = true;
            try{
                file.createNewFile();
                FtcCore.getInstance().getLogger().log(Level.INFO, "Creating file " + fileName + " in " + directory);
            }catch (IOException e){
                FtcCore.getInstance().getLogger().log(Level.SEVERE, "Failed to create " + fileName + " in " +directory);
                e.printStackTrace();
            }
        }
        fileConfig = YamlConfiguration.loadConfiguration(file);
    }

    //methods used by child classes
    protected void reload(){
        performFileNullCheck();
        fileConfig = YamlConfiguration.loadConfiguration(file);
    }

    protected void save(){
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
        if(!file.delete()) FtcCore.getInstance().getLogger().log(Level.WARNING, "Couldn't delete file named " + fileName);
        deleted = true;
        fileConfig = null;
        file = null;
        Announcer.log(Level.INFO, "Deleting file named " + fileName + " in " + directory);
    }

    private void performFileNullCheck(){ //believe you me, this is better than getting an exception that just reads: "NullPointerException: null" lol
        if(file == null) throw new NullPointerException(fileName + " in folder " + directory + " is null. Halting reload and throwing exception :p");
    }
}
