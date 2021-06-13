package net.forthecrown.squire;

import net.forthecrown.squire.enchantment.RoyalEnchants;
import net.kyori.adventure.key.Key;
import net.minecraft.server.v1_16_R3.MinecraftKey;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public interface Squire extends Plugin {

    static Squire inst(){
        return SquireMain.main;
    }

    static NamespacedKey createKey(String namespace, String key){
        return new NamespacedKey(namespace, key);
    }

    static NamespacedKey createFtcKey(String value){
        return new NamespacedKey("ftc", value);
    }

    static NamespacedKey createRoyalKey(String value){
        return new NamespacedKey("royals", value);
    }

    static MinecraftKey keyToNMS(Key key){
        return new MinecraftKey(key.namespace(), key.value());
    }

    static RoyalEnchants getEnchants(){
        return SquireMain.enchants;
    }
}
