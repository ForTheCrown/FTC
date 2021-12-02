package net.forthecrown.squire;

import net.kyori.adventure.key.Key;
import net.minecraft.resources.ResourceLocation;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public interface Squire extends Plugin {

    static Squire inst(){ return SquireMain.main; }

    static NamespacedKey createKey(String namespace, String key){
        return new NamespacedKey(namespace, key);
    }

    static NamespacedKey createFtcKey(String value){ return createKey("ftc", value); }
    static NamespacedKey createRoyalKey(String value){ return createKey("royals", value); }

    static ResourceLocation keyToNMS(Key key){
        return new ResourceLocation(key.namespace(), key.value());
    }
}
