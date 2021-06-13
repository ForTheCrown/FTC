package net.forthecrown.core.serializer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class for easily serializing things into Json
 * @param <T> The plugin into which this will place the file
 */
public abstract class AbstractJsonSerializer<T extends Plugin> implements CrownSerializer<T> {

    private File file;

    protected final String fileName;
    private final String directory;
    private final String fullFileDirectory;
    protected final T plugin;
    protected final Logger logger;
    private final boolean stopIfFileDoesntExist;

    protected boolean fileExists;
    protected boolean deleted = false;

    protected AbstractJsonSerializer(@NotNull String fileName, String directory, @NotNull T plugin){
        this(fileName, directory, false, plugin);
    }
    protected AbstractJsonSerializer(@NotNull String fileName, @NotNull T plugin){
        this(fileName, null, false, plugin);
    }
    protected AbstractJsonSerializer(String fileName, String directory, boolean stopIfFileDoesntExist, T plugin) {
        this.fileName = fileName.endsWith(".json") ? fileName : fileName + ".json";
        this.directory = directory;
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.stopIfFileDoesntExist = stopIfFileDoesntExist;

        this.fullFileDirectory = (directory == null ? "" : directory + File.separator) + this.fileName;
        load();
    }

    private void load(){
        file = new File(plugin.getDataFolder() + File.separator + fullFileDirectory);
        fileExists = file.exists();

        if(!fileExists){
            if(stopIfFileDoesntExist) return;
            if(!file.getParentFile().exists() && !file.getParentFile().mkdir()) logger.severe("Could not create directories for " + fileName);

            try {
                file.createNewFile();
                logger.info("Created file " + fileName + (directory != null ? " in " + file.getParent() : ""));
            } catch (IOException e) {
                logger.severe("Failed to create " + fileName + (directory != null ? " in " + file.getParent() : ""));
                e.printStackTrace();
            }
        }
    }

    public void save() {
        if(deleted) return;
        JsonObject json = new JsonObject();

        if(fileExists) save(json);
        else json = createDefaults(json);

        try {
            FileWriter writer = new FileWriter(file);
            writer.write(json.toString());
            writer.close();
            fileExists = true;
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void reload() {
        if(deleted) return;

        if(!fileExists){
            save();
            return;
        }

        try {
            FileReader reader = new FileReader(file);
            JsonParser parser = new JsonParser();
            JsonObject json = parser.parse(reader).getAsJsonObject();
            reader.close();
            reload(json.getAsJsonObject());
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    protected abstract void save(final JsonObject json);
    protected abstract void reload(final JsonObject json);

    /**
     * Empty files make JsonDaddy mad xD
     * @param json Json to add defaults to
     * @return The Json the defaults were added to
     */
    protected abstract JsonObject createDefaults(final JsonObject json);

    public T getPlugin() {
        return plugin;
    }

    protected void delete(){
        if(!file.delete()) logger.log(Level.WARNING, "Couldn't delete file named " + fileName);
        deleted = true;
        logger.log(Level.INFO, "Deleting file named " + fileName + " in " + directory);
    }
}
