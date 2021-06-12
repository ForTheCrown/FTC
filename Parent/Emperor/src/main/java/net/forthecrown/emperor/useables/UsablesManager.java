package net.forthecrown.emperor.useables;

import net.forthecrown.emperor.CrownCore;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.entity.Entity;

public interface UsablesManager {
    NamespacedKey USABLE_KEY = new NamespacedKey(CrownCore.inst(), "useableSign");

    UsableBlock getBlock(Location l);

    UsableBlock createSign(TileState l);

    UsableEntity getEntity(Entity entity);

    UsableEntity createEntity(Entity entity);

    boolean isInteractableSign(Block block);

    boolean isInteractableEntity(Entity entity);

    void reloadAll();

    void saveAll();

    void addEntity(UsableEntity entity);
    void addBlock(UsableBlock sign);

    void removeEntity(UsableEntity entity);
    void removeBlock(UsableBlock sign);
}
