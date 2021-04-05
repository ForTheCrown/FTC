package net.forthecrown.core.utils;

import net.forthecrown.core.comvars.ComVar;
import net.forthecrown.core.comvars.ComVars;
import net.forthecrown.core.comvars.types.ComVarType;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;

import java.util.HashMap;
import java.util.Map;

public class BMUtils {
    public static Map<Material, ComVar<Short>> pricesFromConfig(ConfigurationSection section, String name){
        Map<Material, ComVar<Short>> returnVal = new HashMap<>();
        for (String s: section.getKeys(false)){
            returnVal.put(Material.valueOf(s.toUpperCase()),
                    ComVars.set(
                            "bm_price_" + name + "_" + s.toLowerCase(),
                            ComVarType.SHORT,
                            (short) section.getInt(s))
            );
        }
        return returnVal;
    }

    public static Map<Enchantment, ComVar<Integer>> enchantsFromConfig(ConfigurationSection section){
        Map<Enchantment, ComVar<Integer>> result = new HashMap<>();
        for (String s :section.getKeys(false)){
            result.put(
                    EnchantmentWrapper.getByKey(NamespacedKey.minecraft(s.toLowerCase())),
                    ComVars.set(
                            "bm_price_ench_" + s.toLowerCase(),
                            ComVarType.INTEGER,
                            section.getInt(s))
            );
        }
        return result;
    }
}
