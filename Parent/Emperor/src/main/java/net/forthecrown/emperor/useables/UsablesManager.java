package net.forthecrown.emperor.useables;

import net.forthecrown.emperor.CrownCore;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;

public interface UsablesManager {
    NamespacedKey USABLE_KEY = new NamespacedKey(CrownCore.inst(), "useableSign");

    UsableSign getSign(Location l);

    UsableSign createSign(Sign l);

    UsableEntity getEntity(Entity entity);

    UsableEntity createEntity(Entity entity);

    boolean isInteractableSign(Block block);

    boolean isInteractableEntity(Entity entity);

    void reloadAll();

    void saveAll();

    void addEntity(UsableEntity entity);
    void addSign(UsableSign sign);

    void removeEntity(UsableEntity entity);
    void removeSign(UsableSign sign);
}
