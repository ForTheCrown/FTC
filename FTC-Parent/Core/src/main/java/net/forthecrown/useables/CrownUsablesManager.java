package net.forthecrown.useables;

import net.forthecrown.core.CrownCore;
import net.forthecrown.registry.ActionRegistry;
import net.forthecrown.registry.CheckRegistry;
import net.forthecrown.serializer.AbstractJsonSerializer;
import net.forthecrown.useables.actions.*;
import net.forthecrown.useables.preconditions.*;
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

    public void registerDefaults(ActionRegistry actions, CheckRegistry checks){
        //Actions
        actions.register(ActionCommand.USER_KEY, () -> new ActionCommand(false));
        actions.register(ActionCommand.CONSOLE_KEY, () -> new ActionCommand(true));

        actions.register(ActionRemoveNumber.BAL_KEY, () -> new ActionRemoveNumber(true));
        actions.register(ActionRemoveNumber.GEM_KEY, () -> new ActionRemoveNumber(false));

        actions.register(ActionAddNumber.BAL_KEY, () -> new ActionAddNumber(true));
        actions.register(ActionAddNumber.GEM_KEY, () -> new ActionAddNumber(false));

        actions.register(ActionItem.ADD_KEY, () -> new ActionItem(true));
        actions.register(ActionItem.REMOVE_KEY, () -> new ActionItem(false));

        actions.register(ActionChangeScore.REMOVE_KEY, () -> new ActionChangeScore(ActionChangeScore.Action.DECREMENT));
        actions.register(ActionChangeScore.ADD_KEY, () -> new ActionChangeScore(ActionChangeScore.Action.INCREMENT));
        actions.register(ActionChangeScore.SET_KEY, () -> new ActionChangeScore(ActionChangeScore.Action.SET));

        actions.register(ActionShowText.KEY, ActionShowText::new);
        actions.register(ActionTeleport.KEY, ActionTeleport::new);
        actions.register(ActionKit.KEY, ActionKit::new);
        actions.register(ActionWarp.KEY, ActionWarp::new);

        CrownCore.logger().info("Default actions registered");

        //Preconditions
        checks.register(CheckNumber.BAL_KEY, () -> new CheckNumber(true));
        checks.register(CheckNumber.GEM_KEY, () -> new CheckNumber(false));

        checks.register(CheckHasItem.KEY, CheckHasItem::new);
        checks.register(CheckInventoryEmpty.KEY, CheckInventoryEmpty::new);
        checks.register(CheckRank.KEY, CheckRank::new);
        checks.register(CheckBranch.KEY, CheckBranch::new);
        checks.register(CheckPermission.KEY, CheckPermission::new);
        checks.register(CheckCooldown.KEY, CheckCooldown::new);
        checks.register(CheckNotUsedBefore.KEY, CheckNotUsedBefore::new);
        checks.register(CheckInWorld.KEY, CheckInWorld::new);
        checks.register(CheckHasScore.KEY, CheckHasScore::new);
        checks.register(CheckNeverUsed.KEY, CheckNeverUsed::new);
        checks.register(CheckHasAllItems.KEY, CheckHasAllItems::new);
        checks.register(CheckIsNotAlt.KEY, CheckIsNotAlt::new);

        CrownCore.logger().info("Default checks registered");
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
