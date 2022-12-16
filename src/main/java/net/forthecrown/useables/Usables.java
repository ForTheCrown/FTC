package net.forthecrown.useables;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import net.forthecrown.core.FTC;
import net.forthecrown.core.Main;
import net.forthecrown.core.module.OnEnable;
import net.forthecrown.core.module.OnLoad;
import net.forthecrown.core.module.OnSave;
import net.forthecrown.core.registry.Keys;
import net.forthecrown.useables.actions.UsageActions;
import net.forthecrown.useables.command.CmdUsables;
import net.forthecrown.useables.command.Kit;
import net.forthecrown.useables.command.Warp;
import net.forthecrown.useables.test.UsageTests;
import net.forthecrown.utils.EntityIdentifier;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializableObject;
import net.forthecrown.utils.math.WorldVec3i;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

/**
 * The class which manages all usables.
 */
public class Usables implements SerializableObject {
    public static final NamespacedKey
            USABLE_KEY = new NamespacedKey(FTC.getPlugin(), "useablesign"),
            LEGACY_KEY = new NamespacedKey(Main.OLD_NAMESPACE, "useablesign"),

            BLOCK_KEY  = Keys.forthecrown("usable_block"),
            ENTITY_KEY = Keys.forthecrown("usable_entity");

    private final Map<WorldVec3i, UsableBlock> loadedBlocks = new Object2ObjectOpenHashMap<>();
    private final Map<UUID, UsableEntity> loadedEntities = new Object2ObjectOpenHashMap<>();

    /**
     * Global triggers
     */
    @Getter
    private final GlobalTriggerManager triggers;

    /**
     * Kit manager
     */
    @Getter
    private final CmdUsables<Kit> kits;

    /**
     * Warp manager
     */
    @Getter
    private final CmdUsables<Warp> warps;

    @Getter
    private static final Usables instance = new Usables();

    public Usables() {
        Path path = PathUtil.getPluginDirectory("usables");

        triggers = new GlobalTriggerManager(path.resolve("triggers.dat"));
        triggers.registerListener();

        // I have not slept in 26 hours
        //
        // Enjoy some lyrics:
        // I've been prone to lose myself in these halls
        // These spirits haunt these walls I call home
        // A treasure trove of times I've lost myself here
        // I fear the worst is yet to come
        //  -- 'O.D.D' by Conform

        var kitFile = path.resolve("kits.dat");
        var warpFile = path.resolve("warps.dat");

        kits = new CmdUsables<>(kitFile, Kit::new);
        warps = new CmdUsables<>(warpFile, Warp::new);
    }

    @OnEnable
    private static void init() {
        UsageActions.init();
        UsageTests.init();
    }

    /**
     * Checks if the given block is a usable block
     * @param block The block to check
     * @return True, if the block is a block entity and a usable
     */
    public boolean isUsableBlock(Block block) {
        if (!(block.getState() instanceof TileState tileState)) {
            return false;
        }

        return _isUsable(tileState, BLOCK_KEY);
    }

    /**
     * Checks if the given entity is a usable entity
     * @param entity The entity to check
     * @return True, if the entity is usable
     */
    public boolean isUsableEntity(Entity entity) {
        return _isUsable(entity, ENTITY_KEY);
    }

    private boolean _isUsable(PersistentDataHolder holder, NamespacedKey key) {
        return holder.getPersistentDataContainer()
                .has(key, PersistentDataType.TAG_CONTAINER);
    }

    /**
     * Gets a usable block
     * @param block The block to get the usable representation of
     * @return The usable block for the given block, null, if block is not usable
     */
    public UsableBlock getBlock(Block block) {
        if (!isUsableBlock(block)) {
            return null;
        }

        var pos = WorldVec3i.of(block);
        var result = loadedBlocks.get(pos);

        if (result == null) {
            result = new UsableBlock(pos.getWorld(), pos.getPos());
            result.load();

            loadedBlocks.put(pos, result);
        }

        return result;
    }

    /**
     * Gets a usable representation of the given entity.
     * <p>
     * If the given entity is not a usable, or is not loaded,
     * then this will return null
     * @param entity The entity to get the usable of
     * @return The usable representation of the entity
     */
    public UsableEntity getLoadedEntity(Entity entity) {
        if (!isUsableEntity(entity)) {
            return null;
        }

        return loadedEntities.get(entity.getUniqueId());
    }

    /**
     * Gets the usable entity representation for the given
     * entity
     * @param entity The entity to get the usable entity of
     * @return The usable entity of the given entity, null, if entity is not usable
     */
    public UsableEntity getEntity(Entity entity) {
        if (!isUsableEntity(entity)) {
            return null;
        }

        var id = entity.getUniqueId();
        var result = loadedEntities.get(id);

        if (result == null) {
            result = new UsableEntity(id);

            if (result.getIdentifier() == null) {
                result.setIdentifier(EntityIdentifier.of(entity));
            }

            result.load();

            loadedEntities.put(id, result);
        }

        return result;
    }

    /**
     * Creates a usable version of the given block
     * @param block The block to create
     * @return The created usable block, null, if block was already usable
     */
    public UsableBlock createBlock(Block block) {
        if (isUsableBlock(block)) {
            return null;
        }

        var pos = WorldVec3i.of(block);
        var result = new UsableBlock(pos.getWorld(), pos.getPos());

        result.save();
        loadedBlocks.put(pos, result);

        return result;
    }

    /**
     * Creates a usable representation or the given entity
     * @param entity The entity to create
     * @return The created usable entity, null, if entity was already usable
     */
    public UsableEntity createEntity(Entity entity) {
        if (isUsableEntity(entity)) {
            return null;
        }

        var id = entity.getUniqueId();
        var result = new UsableEntity(id);
        result.setIdentifier(EntityIdentifier.of(entity));

        result.save(entity.getPersistentDataContainer());
        loadedEntities.put(id, result);

        return result;
    }

    /**
     * Saves all loaded entities and loaded blocks
     * and triggers
     */
    @OnSave
    public void save() {
        for (var v: loadedEntities.values()) {
            v.save();
        }

        loadedBlocks.entrySet().removeIf(entry -> {
            var block = entry.getKey().getBlock();
            return !(block.getState() instanceof TileState);
        });

        for (var v: loadedBlocks.values()) {
            v.save();
        }

        triggers.saveFile();

        warps.save();
        kits.save();
    }

    /**
     * Loads all triggers, loaded entities and loaded blocks
     */
    @OnLoad
    public void reload() {
        triggers.readFile();

        warps.reload();
        kits.reload();

        for (var v: loadedEntities.values()) {
            v.load();
        }

        for (var v: loadedBlocks.values()) {
            v.load();
        }
    }

    /**
     * Deletes the given usable entity
     * @param entity The entity to delete
     */
    public void deleteEntity(UsableEntity entity) {
        entityUnload(entity);

        entity.getEntity().getPersistentDataContainer()
                .remove(ENTITY_KEY);
    }

    /**
     * Deletes a usable block
     * @param block The block to delete
     */
    public void deleteBlock(UsableBlock block) {
        loadedBlocks.remove(block.getPosition());

        TileState tile = block.getTileEntity();
        tile.getPersistentDataContainer().remove(BLOCK_KEY);
        tile.update();
    }

    /**
     * Unloads the given entity
     * @param entity The entity to unload
     */
    public void entityUnload(UsableEntity entity) {
        loadedEntities.remove(entity.getId());
    }
}