package net.forthecrown.core.types.signs;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownSign;
import net.forthecrown.core.serialization.AbstractJsonSerializer;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

public class SignManager {
    static final Map<Location, FtcSign> SIGNS = new HashMap<>();
    public static NamespacedKey SIGN_KEY = new NamespacedKey(FtcCore.getInstance(), "useableSign");

    public static CrownSign getSign(Location l){
        if(SIGNS.containsKey(l)) return SIGNS.get(l);

        try {
            return new FtcSign(l);
        } catch (IllegalStateException e) {
            return null;
        }
    }

    public static CrownSign createSign(Sign l, SignAction action){
        l.getPersistentDataContainer().set(SIGN_KEY, PersistentDataType.BYTE, (byte) 1);
        l.update();

        return new FtcSign(l.getLocation(), action);
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
