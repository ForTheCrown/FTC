package net.forthecrown.core.useables;

import net.forthecrown.core.registry.ActionRegistry;
import net.forthecrown.core.registry.CheckRegistry;
import net.forthecrown.core.serializer.AbstractJsonSerializer;
import net.forthecrown.core.useables.actions.*;
import net.forthecrown.core.useables.preconditions.*;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CrownUsablesManager implements UsablesManager {
    final Map<Location, CrownUsableBlock> signs = new HashMap<>();
    final Map<UUID, CrownUsableEntity> entities = new HashMap<>();

    public void registerDefaults(ActionRegistry aRegistry, CheckRegistry cRegistry){
        //Actions
        aRegistry.register(ActionCommand.USER_KEY, () -> new ActionCommand(false));
        aRegistry.register(ActionCommand.CONSOLE_KEY, () -> new ActionCommand(true));

        aRegistry.register(ActionRemoveNumber.BAL_KEY, () -> new ActionRemoveNumber(true));
        aRegistry.register(ActionRemoveNumber.GEM_KEY, () -> new ActionRemoveNumber(false));

        aRegistry.register(ActionItem.ADD_KEY, () -> new ActionItem(true));
        aRegistry.register(ActionItem.REMOVE_KEY, () -> new ActionItem(false));

        aRegistry.register(ActionChangeScore.REMOVE_KEY, () -> new ActionChangeScore(ActionChangeScore.Action.DECREMENT));
        aRegistry.register(ActionChangeScore.ADD_KEY, () -> new ActionChangeScore(ActionChangeScore.Action.INCREMENT));
        aRegistry.register(ActionChangeScore.SET_KEY, () -> new ActionChangeScore(ActionChangeScore.Action.SET));

        aRegistry.register(ActionShowText.KEY, ActionShowText::new);
        aRegistry.register(ActionTeleport.KEY, ActionTeleport::new);
        aRegistry.register(ActionKit.KEY, ActionKit::new);
        aRegistry.register(ActionWarp.KEY, ActionWarp::new);

        //Preconditions
        cRegistry.register(CheckHasItem.KEY, CheckHasItem::new);
        cRegistry.register(CheckInventoryEmpty.KEY, CheckInventoryEmpty::new);

        cRegistry.register(CheckNumber.BAL_KEY, () -> new CheckNumber(true));
        cRegistry.register(CheckNumber.GEM_KEY, () -> new CheckNumber(false));

        cRegistry.register(CheckRank.KEY, CheckRank::new);
        cRegistry.register(CheckBranch.KEY, CheckBranch::new);
        cRegistry.register(CheckPermission.KEY, CheckPermission::new);
        cRegistry.register(CheckCooldown.KEY, CheckCooldown::new);
        cRegistry.register(CheckNotUsedBefore.KEY, CheckNotUsedBefore::new);
        cRegistry.register(CheckInWorld.KEY, CheckInWorld::new);
        cRegistry.register(CheckHasScore.KEY, CheckHasScore::new);
        cRegistry.register(CheckNeverUsed.KEY, CheckNeverUsed::new);
        cRegistry.register(CheckHasAllItems.KEY, CheckHasAllItems::new);
    }

    @Override
    public UsableBlock getBlock(Location l){
        if(signs.containsKey(l)) return signs.get(l);

        try {
            return new CrownUsableBlock(l, false);
        } catch (IllegalStateException e) {
            return null;
        }
    }

    @Override
    public UsableBlock createSign(TileState l){
        l.getPersistentDataContainer().set(USABLE_KEY, PersistentDataType.BYTE, (byte) 1);
        l.update();

        return new CrownUsableBlock(l.getLocation(), true);
    }

    @Override
    public UsableEntity getEntity(Entity entity){
        if(entities.containsKey(entity.getUniqueId())) return entities.get(entity.getUniqueId());

        try {
            return new CrownUsableEntity(entity, false);
        } catch (IllegalStateException e){
            return null;
        }
    }

    @Override
    public UsableEntity createEntity(Entity entity){
        entity.getPersistentDataContainer().set(USABLE_KEY, PersistentDataType.BYTE, (byte) 1);
        return new CrownUsableEntity(entity, true);
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
        signs.values().forEach(AbstractJsonSerializer::reload);
        entities.values().forEach(AbstractJsonSerializer::reload);
    }

    @Override
    public void saveAll(){
        signs.values().forEach(AbstractJsonSerializer::save);
        entities.values().forEach(AbstractJsonSerializer::save);
    }

    @Override
    public void addEntity(UsableEntity entity) {
        entities.put(entity.getUniqueId(), (CrownUsableEntity) entity);
    }

    @Override
    public void addBlock(UsableBlock sign) {
        signs.put(sign.getLocation(), (CrownUsableBlock) sign);
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
