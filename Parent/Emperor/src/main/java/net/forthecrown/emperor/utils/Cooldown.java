package net.forthecrown.emperor.utils;

import net.forthecrown.emperor.CrownCore;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A class for placing players or entities on cooldowns
 * <p>Cooldowns are divided into categories</p>
 */
public final class Cooldown {

    private static final Map<String, Set<CommandSender>> COOLDOWN_MAP = new HashMap<>();

    private Cooldown(){}

    public static boolean contains(@NotNull CommandSender sender){
        return contains(sender, "general");
    }

    public static boolean contains(@NotNull CommandSender sender, @NotNull String category){
        Validate.notNull(sender, "Sender was null");
        Validate.notNull(category, "Category was null");

        if(COOLDOWN_MAP.get(category) == null){
            COOLDOWN_MAP.put(category, new HashSet<>());
            return false;
        }

        return COOLDOWN_MAP.get(category).contains(sender);
    }

    public static void add(@NotNull CommandSender sender, @Nonnegative int timeInTicks){
        add(sender, "general", timeInTicks);
    }

    public static void add(@NotNull CommandSender sender, @NotNull String category){
        add(sender, category, null);
    }

    public static void add(@NotNull CommandSender sender, @NotNull String category, @Nullable @Nonnegative Integer timeInTicks){
        Validate.notNull(sender, "Sender was null");
        Validate.notNull(category, "Category was null");

        COOLDOWN_MAP.computeIfAbsent(category, k -> new HashSet<>());

        Set<CommandSender> set = COOLDOWN_MAP.get(category);
        set.add(sender);
        COOLDOWN_MAP.put(category, set);

        if(timeInTicks != null){
            Bukkit.getScheduler().runTaskLater(CrownCore.inst(), () -> remove(sender, category), timeInTicks);
        }
    }

    public static void remove(@NotNull CommandSender sender){
        remove(sender, "general");
    }

    public static void remove(@NotNull CommandSender sender, @NotNull String category){
        Validate.notNull(sender, "Sender was null");
        Validate.notNull(category, "Category was null");

        if(COOLDOWN_MAP.get(category) == null){
            COOLDOWN_MAP.put(category, new HashSet<>());
            return;
        }

        Set<CommandSender> set1 = COOLDOWN_MAP.get(category);
        set1.remove(sender);
        COOLDOWN_MAP.put(category, set1);
    }

    public static Set<CommandSender> getCategory(String s){
        COOLDOWN_MAP.computeIfAbsent(s, k -> new HashSet<>());
        return COOLDOWN_MAP.get(s);
    }
}
