package net.forthecrown.core.transformers;

import net.forthecrown.core.Main;
import net.minecraft.nbt.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

public class NamespaceRenamer {
    public static final String OLD_NAMESPACE = "ftccore";
    public static final String NEW_NAMESPACE = Main.NAME.toLowerCase();
    public static final String CHECK_NAME = "namespaces_renamed";

    public static final Set<FileTypeRenamer> RENAMERS = Set.of(new TagRenamer(), new JsonRenamer());
    private static Logger LOGGER;
    private static final boolean THROW_EXCEPTION_ON_FAIL = true;

    public static void run(Logger logger, File dataFolder) {
        LOGGER = logger;

        File firstCheck = new File(dataFolder, CHECK_NAME);
        if (firstCheck.exists()) {
            LOGGER.info("Found check file, not running renamer");
            return;
        }

        LOGGER.info("Beginning namespace renamer");

        LOGGER.info("FTC directory: " + dataFolder);
        LOGGER.info("directory is directory: " + dataFolder.isDirectory());

        File[] files = dataFolder.listFiles();
        Objects.requireNonNull(files, "Files are null");

        renameDirectory(dataFolder, files);

        try {
            File check = new File(dataFolder, CHECK_NAME);
            check.createNewFile();
            LOGGER.info("Created check file");
        } catch (IOException e) {
            LOGGER.severe("Couldn't create check file");
            throwOrReport(e);
        }

        LOGGER.info("Renamer finished");
    }

    private static void throwOrReport(Throwable e) {
        if(THROW_EXCEPTION_ON_FAIL) {
            throw new IllegalStateException(e);
        } else {
            e.printStackTrace();
        }
    }

    static void renameFile(File file) {
        FileTypeRenamer renamer = getRenamer(file);
        if(renamer == null) return;

        try {
            LOGGER.info("Attempting to rename namespaces file " + file.getPath());
            renamer.renameFile(file);
        } catch (IOException e) {
            LOGGER.severe("Couldn't rename namespaces, error");
            throwOrReport(e);
        }
    }

    static FileTypeRenamer getRenamer(File file) {
        String type = file.getName().substring(file.getName().lastIndexOf('.') + 1);

        for (FileTypeRenamer r: RENAMERS) {
            if(r.fileSuffix.equalsIgnoreCase(type)) return r;
        }

        LOGGER.warning("Found no renamer for type: " + type);
        return null;
    }

    static void renameDirectory(File dirHolder, File[] files) {
        LOGGER.info("Renaming all in directory: " + dirHolder.getPath());
        for (File f: files) {
            if(f.isDirectory()) {
                renameDirectory(f, f.listFiles());
                continue;
            }

            renameFile(f);
        }
    }

    public static String renameString(String str) {
        if(str == null || str.isBlank()) return str;
        return str
                .replaceAll("not_used_before", "one_use_per_user")
                .replaceAll(OLD_NAMESPACE, NEW_NAMESPACE);
    }

    static abstract class FileTypeRenamer {
        private final String fileSuffix;

        public FileTypeRenamer(String fileSuffix) {
            this.fileSuffix = fileSuffix;
        }

        public abstract void renameFile(File file) throws IOException;
    }

    static class JsonRenamer extends FileTypeRenamer {
        public JsonRenamer() {
            super("json");
        }

        @Override
        public void renameFile(File file) throws IOException {
            Path path = file.toPath();
            String s = Files.readString(path, StandardCharsets.UTF_8);

            Files.writeString(path, renameString(s), StandardCharsets.UTF_8);
        }
    }

    static class TagRenamer extends FileTypeRenamer {
        public TagRenamer() {
            super("dat");
        }

        @Override
        public void renameFile(File file) throws IOException {
            CompoundTag tag = NbtIo.readCompressed(file);
            tag = rewriteCompound(tag);

            NbtIo.writeCompressed(tag, file);
        }

        Tag rewriteTag(Tag tag) {
            if(tag.getId() == Tag.TAG_LIST) return rewriteList((ListTag) tag);
            if(tag.getId() == Tag.TAG_COMPOUND) return rewriteCompound((CompoundTag) tag);
            if(tag.getId() == Tag.TAG_STRING) return rewriteString((StringTag) tag);

            return tag;
        }

        StringTag rewriteString(StringTag tag) {
            return StringTag.valueOf(renameString(tag.getAsString()));
        }

        ListTag rewriteList(ListTag tag) {
            ListTag clone = new ListTag();

            for (Tag t: tag) {
                clone.add(rewriteTag(t));
            }

            return clone;
        }

        CompoundTag rewriteCompound(CompoundTag tag) {
            CompoundTag result = new CompoundTag();

            for (Map.Entry<String, Tag> e: tag.tags.entrySet()) {
                result.put(renameString(e.getKey()), rewriteTag(e.getValue()));
            }

            return result;
        }
    }
}
