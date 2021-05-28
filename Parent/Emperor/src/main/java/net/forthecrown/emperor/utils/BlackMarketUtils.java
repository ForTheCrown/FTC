package net.forthecrown.emperor.utils;

import net.forthecrown.emperor.comvars.ComVar;
import net.forthecrown.emperor.comvars.ComVars;
import net.forthecrown.emperor.comvars.types.ComVarType;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BlackMarketUtils, not API
 */
public class BlackMarketUtils {
    public static Map<Material, ComVar<Short>> pricesFromConfig(ConfigurationSection section, String name) {
        Map<Material, ComVar<Short>> returnVal = new HashMap<>();
        for (String s : section.getKeys(false)) {
            returnVal.put(
                    Material.valueOf(s.toUpperCase()),
                    ComVars.set(
                            "bm_price_" + name + "_" + s.toLowerCase(),
                            ComVarType.SHORT,
                            (short) section.getInt(s)
                    )
            );
        }
        return returnVal;
    }

    public static Map<Enchantment, ComVar<Integer>> enchantsFromConfig(ConfigurationSection section) {
        Map<Enchantment, ComVar<Integer>> result = new HashMap<>();
        for (String s : section.getKeys(false)) {
            result.put(
                    enchFromString(s),
                    ComVars.set(
                            "bm_price_ench_" + s.toLowerCase(),
                            ComVarType.INTEGER,
                            section.getInt(s)
                    )
            );
        }
        return result;
    }

    public static Enchantment enchFromString(String s){
        return Enchantment.getByKey(NamespacedKey.minecraft(s.toLowerCase()));
    }

    public static String enchToSerializable(Enchantment e){
        return e.getKey().getKey().toLowerCase();
    }

    public static <T> T getRandomEntry(List<T> from, List<T> notAllowed, CrownRandom random){
        T t = from.get(random.nextInt(from.size()));

        while(notAllowed.contains(t)){
            t = from.get(random.nextInt(from.size()));
        }

        return t;
    }
}
