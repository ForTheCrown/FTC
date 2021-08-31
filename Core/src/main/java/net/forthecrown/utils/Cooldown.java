package net.forthecrown.utils;

import net.forthecrown.core.Crown;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnegative;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A class for placing players or entities on cooldowns
 * <p>Cooldowns are divided into categories</p>
 */
public final class Cooldown {
    public static final String GENERAL = "general";

    private static final Map<String, Set<CommandSender>> COOLDOWN_MAP = new HashMap<>();

    private Cooldown(){}

    public static boolean contains(@NotNull CommandSender sender){
        return contains(sender, GENERAL);
    }

    public static boolean contains(@NotNull CommandSender sender, @NotNull String category){
        Validate.notNull(sender, "Sender was null");
        Validate.notNull(category, "Category was null");

        return COOLDOWN_MAP.computeIfAbsent(category, k -> new HashSet<>()).contains(sender);
    }

    public static boolean containsOrAdd(CommandSender sender, @Nonnegative int ticks) {
        return containsOrAdd(sender, GENERAL, ticks);
    }

    public static boolean containsOrAdd(CommandSender sender, @NotNull String category, @Nonnegative int ticks) {
        boolean contains = contains(sender, category);

        if(!contains) add(sender, category, ticks);

        return contains;
    }

    public static void add(@NotNull CommandSender sender, @Nonnegative int timeInTicks){
        add(sender, GENERAL, timeInTicks);
    }

    public static void add(@NotNull CommandSender sender){
        add(sender, GENERAL, -1);
    }

    public static void add(@NotNull CommandSender sender, @NotNull String category){
        add(sender, category, -1);
    }

    public static void add(@NotNull CommandSender sender, @NotNull String category, int timeInTicks){
        Validate.notNull(sender, "Sender was null");
        Validate.notNull(category, "Category was null");

        Set<CommandSender> set = COOLDOWN_MAP.computeIfAbsent(category, k -> new HashSet<>());
        set.add(sender);

        if(timeInTicks != -1){
            Bukkit.getScheduler().runTaskLater(Crown.inst(), () -> remove(sender, category), timeInTicks);
        }
    }

    public static void remove(@NotNull CommandSender sender){
        remove(sender, GENERAL);
    }

    public static void remove(@NotNull CommandSender sender, @NotNull String category){
        Validate.notNull(sender, "Sender was null");
        Validate.notNull(category, "Category was null");

        Set<CommandSender> set1 = COOLDOWN_MAP.computeIfAbsent(category, k -> new HashSet<>());
        set1.remove(sender);
    }

    public static Set<CommandSender> getCategory(String s){
        COOLDOWN_MAP.computeIfAbsent(s, k -> new HashSet<>());
        return COOLDOWN_MAP.get(s);
    }
}
