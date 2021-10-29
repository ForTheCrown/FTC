package net.forthecrown.useables;

import net.forthecrown.serializer.CrownSerializer;
import net.forthecrown.utils.math.WorldVec3i;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FtcUsablesManager implements UsablesManager {
    final Map<WorldVec3i, UsableBlock> signs = new HashMap<>();
    final Map<UUID, UsableEntity> entities = new HashMap<>();

    @Override
    public UsableBlock getBlock(Location l){
        WorldVec3i pos = WorldVec3i.of(l);
        if(signs.containsKey(pos)) return signs.get(pos);

        try {
            return new FtcUsableBlock(pos, false);
        } catch (IllegalStateException e) {
            return null;
        }
    }

    @Override
    public UsableBlock createBlock(TileState l){
        l.getPersistentDataContainer().set(USABLE_KEY, PersistentDataType.BYTE, (byte) 1);
        l.update();

        return new FtcUsableBlock(WorldVec3i.of(l.getBlock()), true);
    }

    @Override
    public UsableEntity getEntity(Entity entity){
        if(entities.containsKey(entity.getUniqueId())) return entities.get(entity.getUniqueId());

        try {
            return new FtcUsableEntity(entity, false);
        } catch (IllegalStateException e){
            return null;
        }
    }

    @Override
    public UsableEntity createEntity(Entity entity){
        entity.getPersistentDataContainer().set(USABLE_KEY, PersistentDataType.BYTE, (byte) 1);
        return new FtcUsableEntity(entity, true);
    }

    @Override
    public boolean isInteractableSign(Block block){
        if(block == null) return false;
        if (!(block.getState() instanceof TileState)) return false;
        return isInteractable((TileState) block.getState());
    }

    @Override
    public boolean isInteractableEntity(Entity entity){
        if(entity == null) return false;
        return isInteractable(entity);
    }

    private boolean isInteractable(PersistentDataHolder holder){
        return holder.getPersistentDataContainer().has(USABLE_KEY, PersistentDataType.BYTE);
    }

    @Override
    public void reloadAll(){
        signs.values().forEach(CrownSerializer::reload);
        entities.values().forEach(CrownSerializer::reload);
    }

    @Override
    public void saveAll(){
        signs.values().forEach(CrownSerializer::save);
        entities.values().forEach(CrownSerializer::save);
    }

    @Override
    public void addEntity(UsableEntity entity) {
        entities.put(entity.getUniqueId(), entity);
    }

    @Override
    public void addBlock(UsableBlock sign) {
        signs.put(sign.getLocation(), sign);
    }

    @Override
    public void removeEntity(UsableEntity entity) {
        entities.remove(entity.getUniqueId());
    }

    @Override
    public void removeBlock(UsableBlock sign) {
        signs.remove(sign.getLocation());
    }
}
