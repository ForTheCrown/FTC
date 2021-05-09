package net.forthecrown.core.types.signs;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownSign;
import net.forthecrown.core.serialization.AbstractJsonSerializer;
import net.forthecrown.core.types.signs.actions.*;
import net.forthecrown.core.types.signs.preconditions.*;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class SignManager {
    static final Map<Location, FtcSign> SIGNS = new HashMap<>();
    public static NamespacedKey SIGN_KEY = new NamespacedKey(FtcCore.getInstance(), "useableSign");

    static final Map<String, Supplier<SignAction>> ACTIONS = new HashMap<>();
    static final Map<String, Supplier<SignPrecondition>> PRECONDITIONS = new HashMap<>();

    public static void init(){
        //Actions
        registerAction("command_user", () -> new SignActionCommand(false));
        registerAction("command_console", () -> new SignActionCommand(true));

        registerAction("remove_balance", () -> new SignActionRemoveThing(true));
        registerAction("remove_gems", () -> new SignActionRemoveThing(false));

        registerAction("give_item", SignActionGiveItem::new);
        registerAction("show_text", SignActionShowText::new);
        registerAction("teleport_user", SignActionTeleport::new);

        //Preconditions
        registerPrecondition("required_balance", () -> new SignCheckNumber(true));
        registerPrecondition("required_gems", () -> new SignCheckNumber(false));

        registerPrecondition("required_rank", SignCheckRank::new);
        registerPrecondition("required_branch", SignCheckBranch::new);
        registerPrecondition("required_permission", SignCheckPermission::new);
        registerPrecondition("cooldown", SignCheckCooldown::new);
    }

    public static CrownSign getSign(Location l){
        if(SIGNS.containsKey(l)) return SIGNS.get(l);

        try {
            return new FtcSign(l, false);
        } catch (IllegalStateException e) {
            return null;
        }
    }

    public static CrownSign createSign(Sign l){
        l.getPersistentDataContainer().set(SIGN_KEY, PersistentDataType.BYTE, (byte) 1);
        l.update();

        return new FtcSign(l.getLocation(), true);
    }

    public static void registerPrecondition(String name, Supplier<SignPrecondition> precondition){
        Validate.notNull(precondition);
        Validate.notNull(precondition.get());

        PRECONDITIONS.put(formatName(name), precondition);
    }

    public static void registerAction(String name, Supplier<SignAction> action){
        Validate.notNull(action);
        Validate.notNull(action.get());

        ACTIONS.put(formatName(name), action);
    }

    private static String formatName(String name){
        return name.replaceAll(" ", "_").toLowerCase();
    }

    public static SignAction getAction(String name){
        if(!ACTIONS.containsKey(name)) throw new NullPointerException("Actions does not contain key: " + name);
        return ACTIONS.get(name).get();
    }

    public static SignPrecondition getPrecondition(String name){
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
        return ((Sign) block.getState()).getPersistentDataContainer().has(SIGN_KEY, PersistentDataType.BYTE);
    }

    public static void reloadAll(){
        SIGNS.values().forEach(AbstractJsonSerializer::reload);
    }

    public static void saveAll(){
        SIGNS.values().forEach(AbstractJsonSerializer::save);
    }
}
