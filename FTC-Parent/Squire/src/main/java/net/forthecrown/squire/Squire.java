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
    static NamespacedKey createVikingsKey(String value){ return createKey("vikings", value); }
    static NamespacedKey createPiratesKey(String value){ return createKey("pirate", value); }
    static NamespacedKey createCosmeticsKey(String value){ return createKey("cosmetics", value); }

    static ResourceLocation keyToNMS(Key key){
        return new ResourceLocation(key.namespace(), key.value());
    }
}
