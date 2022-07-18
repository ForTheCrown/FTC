package net.forthecrown.squire;

import net.kyori.adventure.key.Key;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceLocation;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;

public interface Squire extends Plugin {

    static Squire inst(){ return SquireMain.main; }

    static NamespacedKey createKey(String namespace, String key){
        return new NamespacedKey(namespace, key);
    }

    static NamespacedKey createRoyalKey(String value){ return createKey("royals", value); }

    static ResourceLocation keyToNMS(Key key){
        return new ResourceLocation(key.namespace(), key.value());
    }

    static void unfreezeRegistry(MappedRegistry registry) {
        try {
            Field frozen = registry.getClass().getDeclaredField("ca");
            frozen.setAccessible(true);

            frozen.set(registry, false);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
