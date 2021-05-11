package net.forthecrown.core.types.interactable;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.InteractableEntity;
import net.forthecrown.core.api.InteractableSign;
import net.forthecrown.core.serialization.AbstractJsonSerializer;
import net.forthecrown.core.types.interactable.actions.*;
import net.forthecrown.core.types.interactable.preconditions.*;
import net.forthecrown.core.utils.CrownUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public class UseablesManager {
    static final Map<Location, InteractionSign> SIGNS = new HashMap<>();
    static final Map<UUID, InteractionEntity> ENTITIES = new HashMap<>();
    public static NamespacedKey USEABLE_KEY = new NamespacedKey(FtcCore.getInstance(), "useableSign");

    static final Map<String, Supplier<InteractionAction>> ACTIONS = new HashMap<>();
    static final Map<String, Supplier<InteractionCheck>> PRECONDITIONS = new HashMap<>();

    public static void init(){
        //Actions
        registerAction("command_user", () -> new ActionCommand(false));
        registerAction("command_console", () -> new ActionCommand(true));

        registerAction("remove_balance", () -> new ActionRemoveThing(true));
        registerAction("remove_gems", () -> new ActionRemoveThing(false));

        registerAction("give_item", ActionGiveItem::new);
        registerAction("show_text", ActionShowText::new);
        registerAction("teleport_user", ActionTeleport::new);
        registerAction("remove_item", ActionRemoveItem::new);

        //Preconditions
        registerPrecondition("has_item", CheckHasItem::new);
        registerPrecondition("inventory_empty", CheckInventoryEmpty::new);

        registerPrecondition("required_balance", () -> new CheckNumber(true));
        registerPrecondition("required_gems", () -> new CheckNumber(false));

        registerPrecondition("required_rank", CheckRank::new);
        registerPrecondition("required_branch", CheckBranch::new);
        registerPrecondition("required_permission", CheckPermission::new);
        registerPrecondition("cooldown", CheckCooldown::new);
    }

    public static InteractableSign getSign(Location l){
        if(SIGNS.containsKey(l)) return SIGNS.get(l);

        try {
            return new InteractionSign(l, false);
        } catch (IllegalStateException e) {
            return null;
        }
    }

    public static InteractableSign createSign(Sign l){
        l.getPersistentDataContainer().set(USEABLE_KEY, PersistentDataType.BYTE, (byte) 1);
        l.update();

        return new InteractionSign(l.getLocation(), true);
    }

    public static InteractableEntity getEntity(Entity entity){
        if(ENTITIES.containsKey(entity.getUniqueId())) return ENTITIES.get(entity.getUniqueId());

        try {
            return new InteractionEntity(entity, false);
        } catch (IllegalStateException e){
            return null;
        }
    }

    public static InteractableEntity createEntity(Entity entity){
        entity.getPersistentDataContainer().set(USEABLE_KEY, PersistentDataType.BYTE, (byte) 1);
        return new InteractionEntity(entity, true);
    }

    public static void registerPrecondition(String name, Supplier<InteractionCheck> precondition){
        Validate.notNull(precondition);
        Validate.notNull(precondition.get());

        PRECONDITIONS.put(formatName(name), precondition);
    }

    public static void registerAction(String name, Supplier<InteractionAction> action){
        Validate.notNull(action);
        Validate.notNull(action.get());

        ACTIONS.put(formatName(name), action);
    }

    private static String formatName(String name){
        Validate.isTrue(!CrownUtils.isNullOrBlank(name), "Name cannot be null or blank");
        return name.replaceAll(" ", "_").toLowerCase().trim();
    }

    public static InteractionAction getAction(String name){
        if(!ACTIONS.containsKey(name)) throw new NullPointerException("Actions does not contain key: " + name);
        return ACTIONS.get(name).get();
    }

    public static InteractionCheck getPrecondition(String name){
        if(!PRECONDITIONS.containsKey(name)) throw new NullPointerException("Preconditions does not contain key: " + name);
        return PRECONDITIONS.get(name).get();
    }

    public static Set<String> getPreconditions(){
        return PRECONDITIONS.keySet();
    }

    public static Set<String> getActions(){
        return ACTIONS.keySet();
    }

    public static boolean isInteractableSign(Block block){
        if(block == null) return false;
        if (!(block.getState() instanceof Sign)) return false;
        return isInteractable((Sign) block.getState());
    }

    public static boolean isInteractableEntity(Entity entity){
        if(entity == null) return false;
        return isInteractable(entity);
    }

    private static boolean isInteractable(PersistentDataHolder holder){
        return holder.getPersistentDataContainer().has(USEABLE_KEY, PersistentDataType.BYTE);
    }

    public static void reloadAll(){
        SIGNS.values().forEach(AbstractJsonSerializer::reload);
        ENTITIES.values().forEach(AbstractJsonSerializer::reload);
    }

    public static void saveAll(){
        SIGNS.values().forEach(AbstractJsonSerializer::save);
        ENTITIES.values().forEach(AbstractJsonSerializer::save);
    }
}
