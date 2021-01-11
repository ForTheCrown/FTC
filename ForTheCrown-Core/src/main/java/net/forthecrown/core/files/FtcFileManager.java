package net.forthecrown.core.files;
import net.forthecrown.core.FtcCore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class FtcFileManager{

    private final String fileName;
    private final String directory;
    private final boolean stopIfFileDoesntExist;

    protected boolean fileDoesntExist = false;
    private boolean deleted = false;

    protected File file;
    protected FileConfiguration fileConfig;

    public FtcFileManager(String fileName, String folder){
        this.directory = folder;
        this.fileName = fileName;
        stopIfFileDoesntExist = false;
        loadFile();
    }
    public FtcFileManager(String fileName, String folder, boolean stopIfFileDoesntExist){
        this.directory = folder;
        this.fileName = fileName;
        this.stopIfFileDoesntExist = stopIfFileDoesntExist;
        loadFile();
    }
    public FtcFileManager(String fileName){
        directory = null;
        this.fileName = fileName;
        stopIfFileDoesntExist = false;
        loadFile();
    }

    private void loadFile(){
        if(directory == null) file = new File(FtcCore.getInstance().getDataFolder(), fileName + ".yml");
        else file = new File(FtcCore.getInstance().getDataFolder() + "/" + directory, fileName + ".yml");

        //if the directory doesn't exist and it can't create it
        if(!file.getParentFile().exists() && !file.getParentFile().mkdirs()) System.out.println("[SEVERE!] Failed to create " + fileName + " directory, at " + FtcCore.getInstance().getDataFolder() + "/" + directory);

        if(!file.exists()){
            if(stopIfFileDoesntExist) return;
            fileDoesntExist = true;
            try{
                file.createNewFile();
            }catch (IOException e){
                System.out.println("[SEVERE!] Failed to create " + fileName);
                e.printStackTrace();
            }
        }
        fileConfig = YamlConfiguration.loadConfiguration(file);
    }

    //methods used by child classes
    public void reload(){
        fileConfig = YamlConfiguration.loadConfiguration(file);
    }

    public void save(){
        if(deleted) return;
        try{
            fileConfig.save(file);
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    public FileConfiguration getFile(){
        return fileConfig;
    }

    protected void delete(){
        fileConfig = null;
        file.delete();
        deleted = true;
    }
}
