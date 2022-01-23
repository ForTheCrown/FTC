package net.forthecrown.serializer;

import com.google.gson.JsonObject;
import net.forthecrown.core.Crown;
import net.forthecrown.utils.JsonUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * A class for easily serializing things into Json
 */
public abstract class AbstractJsonSerializer extends AbstractSerializer implements CrownSerializer {

    private File file;

    protected final String fileName;
    private final String directory;
    private final String fullFileDirectory;
    private final boolean stopIfFileDoesntExist;

    protected boolean fileExists;
    protected boolean deleted = false;

    protected AbstractJsonSerializer(@NotNull String fileName, String directory){
        this(fileName, directory, false);
    }
    protected AbstractJsonSerializer(@NotNull String fileName){
        this(fileName, null, false);
    }
    protected AbstractJsonSerializer(String fileName, String directory, boolean stopIfFileDoesntExist) {
        this.fileName = fileName.endsWith(".json") ? fileName : fileName + ".json";
        this.directory = directory;
        this.stopIfFileDoesntExist = stopIfFileDoesntExist;

        this.fullFileDirectory = (directory == null ? "" : directory + File.separator) + this.fileName;
        load();
    }

    private void load(){
        file = new File(Crown.dataFolder() + File.separator + fullFileDirectory);
        fileExists = file.exists();

        if(!fileExists){
            if(stopIfFileDoesntExist) return;
            if(!file.getParentFile().exists() && !file.getParentFile().mkdir()) LOGGER.error("Could not create directories for " + fileName);

            try {
                file.createNewFile();
                LOGGER.info("Created file " + fileName + (directory != null ? " in " + file.getParent() : ""));
            } catch (IOException e) {
                LOGGER.error("Failed to create " + fileName + (directory != null ? " in " + file.getParent() : ""), e);
            }
        }
    }

    public void save() {
        if(deleted) return;
        JsonWrapper json = JsonWrapper.empty();

        if(fileExists) save(json);
        else createDefaults(json);

        try {
            JsonUtils.writeFile(json.getSource(), file);
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
            JsonObject json = JsonUtils.readFileObject(file);
            reload(JsonWrapper.of(json));
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    protected abstract void save(final JsonWrapper json);
    protected abstract void reload(final JsonWrapper json);

    /**
     * Empty files make JsonDaddy mad xD
     * @param json Json to add defaults to
     */
    protected void createDefaults(final JsonWrapper json) {}

    protected void delete(){
        if(!file.delete()) LOGGER.warn("Couldn't delete file named " + fileName);
        deleted = true;
        LOGGER.info("Deleting file named " + fileName + " in " + directory);
    }
}
