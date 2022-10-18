package net.forthecrown.datafix;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.useables.BukkitSavedUsable;
import net.forthecrown.useables.UsableBlock;
import net.forthecrown.useables.UsableEntity;
import net.forthecrown.useables.Usables;
import net.forthecrown.useables.command.CmdUsables;
import net.forthecrown.useables.command.CommandUsable;
import net.forthecrown.utils.EntityIdentifier;
import net.forthecrown.utils.LocationFileName;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.math.Vectors;
import org.bukkit.block.TileState;
import org.bukkit.entity.Entity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.forthecrown.utils.io.PathUtil.safeDelete;

public class UsablesJsonToTag extends DataUpdater {

    public boolean update() throws IOException, CommandSyntaxException {
        runKits();
        runWarps();
        runBlocks();

        return true;
    }

    private void runKits() throws IOException, CommandSyntaxException {
        var manager = Usables.get().getKits();
        _run("kits.json", manager);
    }

    private void runWarps() throws IOException, CommandSyntaxException {
        var manager = Usables.get().getWarps();
        _run("warps.json", manager);
    }

    private <T extends CommandUsable> void _run(String fileName, CmdUsables<T> manager) throws IOException, CommandSyntaxException {
        Path file = PathUtil.pluginPath(fileName);

        if (!Files.exists(file)) {
            LOGGER.info("{} doesn't exist or is empty, skipping", fileName);
            return;
        }

        var json = JsonUtils.readFileObject(file);

        for (var e: json.entrySet()) {
            String key = e.getKey().replaceAll("forthecrown:", "");

            T obj = manager.getFactory().create(key, e.getValue().getAsJsonObject());
            LOGGER.info("Read {}: '{}'", manager.getFilePath().getFileName().toString(), obj.getName());

            manager.add(obj);
        }

        manager.save();

        if (safeDelete(file, true, false)
                .resultOrPartial(LOGGER::error)
                .orElse(0) > 0
        ) {
            LOGGER.info("Deleted '{}'", file);
        }
    }

    private void runBlocks() {
        Path dir = PathUtil.pluginPath("signs");

        if (!iterateDirectory(dir, false, true, this::updateBlock)) {
            return;
        }

        if (safeDelete(dir, true, true)
                .resultOrPartial(LOGGER::error)
                .orElse(0) > 0
        ) {
            LOGGER.info("Deleted '{}'", dir);
        }
    }

    private void updateBlock(Path p) {
        var name = LocationFileName.parse(p.getFileName().toString());

        if (name.getWorld() == null) {
            LOGGER.warn("Found unknown world '{}' in usable block: '{}'... deleting",
                    name.world(), name
            );

            return;
        }

        var block = name.getBlock();
        var state = block.getState();

        if (!(state instanceof TileState tile)) {
            LOGGER.info("Found non tile entity usable at: {}", name);
            return;
        }

        var container = tile.getPersistentDataContainer();
        container.remove(Usables.LEGACY_KEY);
        container.remove(Usables.USABLE_KEY);

        var usable = new UsableBlock(block.getWorld(), Vectors.from(block));
        readSafe(usable, p);

        usable.save(container);
        tile.update();

        LOGGER.info("Converted legacy usable block at: {}", name);
    }

    public static void convertLegacy(Entity entity) {
        var container = entity.getPersistentDataContainer();
        container.remove(Usables.LEGACY_KEY);
        container.remove(Usables.USABLE_KEY);

        var usable = new UsableEntity(entity.getUniqueId());
        Path path = PathUtil.pluginPath("entities", usable.getId() + ".json");

        if (!Files.exists(path)) {
            return;
        }

        usable.setIdentifier(EntityIdentifier.of(entity));
        readSafe(usable, path);

        usable.save(container);

        LOGGER.info("Transformed legacy entity usable, deleting legacy file");
        safeDelete(path).resultOrPartial(LOGGER::error);
    }

    private static void readSafe(BukkitSavedUsable usable, Path file) {
        try {
            readJson(usable, file);
        } catch (Throwable t) {
            LOGGER.error("Error reading usables json", t);
        }
    }

    private static void readJson(BukkitSavedUsable usable, Path path) throws IOException, CommandSyntaxException {
        var json = JsonWrapper.wrap(JsonUtils.readFileObject(path));

        usable.setSilent(!json.getBool("sendFail"));
        usable.setCancelVanilla(json.getBool("cancelVanilla"));

        UsablesJsonReader.loadActions(usable, json.getSource());
        UsablesJsonReader.loadChecks(usable, json.getSource());
    }
}