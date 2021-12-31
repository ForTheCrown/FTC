package net.forthecrown.useables;

import net.forthecrown.core.Crown;
import net.forthecrown.core.transformers.NamespaceRenamer;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.entity.Entity;

public interface UsablesManager {

    /**
     * The key that blocks and entities have to make sure they're interactable
     */
    NamespacedKey USABLE_KEY = new NamespacedKey(Crown.inst(), "useablesign");
    NamespacedKey LEGACY_KEY = new NamespacedKey(NamespaceRenamer.OLD_NAMESPACE, "useablesign");

    /**
     * Gets a block from the given location
     * @param l The location
     * @return The usable block at that location
     */
    UsableBlock getBlock(Location l);

    /**
     * Creates a block in the given tile entity
     * @param l The tile entity to turn into a usable
     * @return The usable
     */
    UsableBlock createBlock(TileState l);

    /**
     * Gets a usable entity from the given entity
     * @param entity The entity to get the usable version of
     * @return The usable entity of that entity
     */
    UsableEntity getEntity(Entity entity);

    /**
     * Creates a usable entity
     * @param entity To entity to make usable
     * @return The usable entity
     */
    UsableEntity createEntity(Entity entity);

    /**
     * Checks whether the given block is a usable or not
     * @param block The block to check
     * @return Whether that block is a usable or not
     */
    boolean isInteractableSign(Block block);

    /**
     * Checks if the given entity is usable
     * @param entity The entity to check
     * @return Whether the entity is usable
     */
    boolean isInteractableEntity(Entity entity);

    /**
     * Reloads all usables
     */
    void reloadAll();

    /**
     * Saves all usables.
     */
    void saveAll();

    void addEntity(UsableEntity entity);
    void addBlock(UsableBlock sign);

    void removeEntity(UsableEntity entity);
    void removeBlock(UsableBlock sign);
}
