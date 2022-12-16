package net.forthecrown.structure;

import lombok.Getter;
import net.forthecrown.core.FTC;
import net.forthecrown.core.module.OnLoad;
import net.forthecrown.core.module.OnSave;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializationHelper;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Structures {
    private static final Logger LOGGER = FTC.getLogger();

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

    @OnSave
    public void save() {
        for (var structure: registry.entries()) {
            Path p = getPath(structure);
            SerializationHelper.writeTagFile(p, structure.getValue()::save);
        }
    }

    @OnLoad
    public void load() {
        registry.clear();

        if (!Files.exists(directory)) {
            return;
        }

        PathUtil.findAllFiles(directory, false)
                .resultOrPartial(FTC.getLogger()::error)

                .ifPresent(strings -> {
                    strings.forEach(s -> {
                        Path path = directory.resolve(s + ".dat");
                        BlockStructure structure = new BlockStructure();

                        LOGGER.debug("loading structure '{}'", s);

                        if (!SerializationHelper.readTagFile(path, structure::load)) {
                            LOGGER.warn("Couldn't load '{}'", s);
                            return;
                        }

                        registry.register(s, structure);
                    });
                });
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