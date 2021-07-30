package net.forthecrown.serializer;

import net.forthecrown.core.ForTheCrown;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public abstract class AbstractNbtSerializer implements CrownSerializer {
    private static final Logger logger = ForTheCrown.logger();

    private final File file;

    protected boolean deleted;
    protected boolean fileExists;

    protected final boolean stopIfFileDoesntExist;

    public AbstractNbtSerializer(String filePath) { this(new File(filePath), false); }
    public AbstractNbtSerializer(String filePath, boolean stopIfFileDoesntExist) { this(new File(filePath), stopIfFileDoesntExist); }
    public AbstractNbtSerializer(File file) { this(file, false); }

    public AbstractNbtSerializer(File file, boolean stopIfFileDoesntExist) {
        this.file = file;
        this.stopIfFileDoesntExist = stopIfFileDoesntExist;

        load();
    }

    private void load(){
        fileExists = file.exists();

        if(!fileExists){
            if(stopIfFileDoesntExist) return;
            if(!file.getParentFile().exists() && !file.getParentFile().mkdir()) logger.severe("Could not create directories for " + file.getPath());

            try {
                file.createNewFile();
                logger.info("Created file " + file.getPath());
            } catch (IOException e) {
                logger.severe("Failed to create " + file.getPath());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void save() {
        if(deleted) return;

        CompoundTag tag = new CompoundTag();

        if(fileExists) save(tag);
        else addDefaults(tag);

        try {
            NbtIo.writeCompressed(tag, file);
            fileExists = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reload() {
        if(deleted) return;

        if(!fileExists){
            save();
        }

        try {
            CompoundTag tag = NbtIo.readCompressed(file);
            reload(tag);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    protected void delete(){
        file.delete();
        deleted = true;
    }

    protected abstract void save(CompoundTag tag);
    protected abstract void reload(CompoundTag tag);

    protected abstract void addDefaults(CompoundTag tag);
}
