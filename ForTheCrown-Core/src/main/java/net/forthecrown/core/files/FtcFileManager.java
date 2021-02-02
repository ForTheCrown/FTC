package net.forthecrown.core.files;
import net.forthecrown.core.FtcCore;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class FtcFileManager {

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
        if(!file.getParentFile().exists() && !file.getParentFile().mkdirs()) System.out.println("[SEVERE!] Failed to create " + fileName + " directory, at " + FtcCore.getInstance().getDataFolder() + "/" + directory);

        if(!file.exists()){
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
    protected void reload(){
        fileConfig = YamlConfiguration.loadConfiguration(file);
    }

    protected void save(){
        if(deleted) return;
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
        fileConfig = null;
        if(!file.delete()) System.out.println("[WARNING] Couldn't delete file named " + fileName);
        deleted = true;
        file = null;
    }
}
