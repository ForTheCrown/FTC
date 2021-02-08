package net.forthecrown.core;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnegative;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class Cooldown {

    private static final Map<String, Set<CommandSender>> COOLDOWN_MAP = new HashMap<>();

    public static boolean contains(CommandSender sender){
        return contains(sender, "CategoryGeneral");
    }

    public static boolean contains(CommandSender sender, String category){
        if(COOLDOWN_MAP.get(category) == null){
            COOLDOWN_MAP.put(category, new HashSet<>());
            return false;
        }

        return COOLDOWN_MAP.get(category).contains(sender);
    }

    public static void add(CommandSender sender, @Nonnegative int timeInTicks){
        add(sender, "CategoryGeneral", timeInTicks);
    }

    public static void add(CommandSender sender, String category, @Nonnegative int timeInTicks){
        COOLDOWN_MAP.computeIfAbsent(category, k -> new HashSet<>());

        Set<CommandSender> set = COOLDOWN_MAP.get(category);
        set.add(sender);
        COOLDOWN_MAP.put(category, set);

        Bukkit.getScheduler().runTaskLater(FtcCore.getInstance(), () -> remove(sender, category), timeInTicks);
    }

    public static void remove(CommandSender sender){
        remove(sender, "CategoryGeneral");
    }

    public static void remove(CommandSender sender, String category){
        if(COOLDOWN_MAP.get(category) == null){
            COOLDOWN_MAP.put(category, new HashSet<>());
            return;
        }

        Set<CommandSender> set1 = COOLDOWN_MAP.get(category);
        set1.remove(sender);
        COOLDOWN_MAP.put(category, set1);
    }
}
