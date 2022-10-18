package net.forthecrown.structure;

import lombok.Getter;
import net.forthecrown.core.Crown;
import net.forthecrown.core.AutoSave;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializationHelper;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Structures {
    private static final Structures inst = new Structures();

    @Getter
    private final Registry<BlockStructure> registry = Registries.newRegistry();

    @Getter
    private final Path directory;

    public Structures() {
        this.directory = PathUtil.getPluginDirectory("structures");
    }

    public static Structures get() {
        return inst;
    }

    private static void init() {
        get().load();
        AutoSave.get().addCallback(() -> get().save());
    }

    public void save() {
        for (var structure: registry.entries()) {
            Path p = getPath(structure);
            SerializationHelper.writeTagFile(p, structure.getValue()::save);
        }
    }

    public void load() {
        registry.clear();

        if (!Files.exists(directory)) {
            return;
        }

        try {
            loadDirectory(directory, "");
        } catch (IOException e) {
            Crown.logger().error("Error loading structures", e);
        }
    }

    public void loadDirectory(Path path, String prefix) throws IOException {
        loadDirectory(Files.newDirectoryStream(path), prefix);
    }

    private void loadDirectory(DirectoryStream<Path> dir, String prefix) throws IOException {
        for (var path: dir) {
            if (Files.isDirectory(path)) {
                loadDirectory(path, prefix + path.getFileName().toString() + "/");
                continue;
            }

            // Skip non NBT files
            if (!path.toString().endsWith(".dat")) {
                continue;
            }

            String key = prefix + path.getFileName().toString().replaceAll(".dat", "");

            BlockStructure structure = new BlockStructure();
            SerializationHelper.readTagFile(path, structure::load);

            Crown.logger().info("Loaded structure: '{}'", key);
            registry.register(key, structure);
        }

        dir.close();
    }

    public void delete(Holder<BlockStructure> structure) {
        var path = getPath(structure);

        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Path getPath(Holder<BlockStructure> holder) {
        String strPath = holder.getKey();
        return directory.resolve(strPath + ".dat");
    }
}