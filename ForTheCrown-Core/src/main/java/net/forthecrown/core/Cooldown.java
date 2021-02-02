package net.forthecrown.core;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class Cooldown {

    private static final Map<Player, Set<String>> DELAY_MAP = new HashMap<>();
    private static final Set<Player> DELAY_SET = new HashSet<>();

    public static boolean contains(Player player){
        return DELAY_SET.contains(player);
    }

    public static boolean contains(Player player, @Nonnull String s){
        return DELAY_MAP.get(player).contains(s);
    }

    public static void add(Player player, @Nonnull String s, int cooldownInTicks, boolean ignoreWithPermission){
        if(player.hasPermission("ftc.cooldown.ignore") && ignoreWithPermission) return;

        Set<String> temp = DELAY_MAP.getOrDefault(player, new HashSet<>());
        temp.add(s);

        DELAY_MAP.put(player, temp);

        Bukkit.getScheduler().runTaskLater(FtcCore.getInstance(), () -> {
            Set<String> temp1 = DELAY_MAP.get(player);
            temp1.remove(s);

            DELAY_MAP.put(player, temp1);
        }, cooldownInTicks);
    }

    public static void add(Player player, int cooldownInTicks, boolean ignoreWithPermissions){
        if(player.hasPermission("ftc.cooldown.ignore") && ignoreWithPermissions) return;

        DELAY_SET.add(player);
        Bukkit.getScheduler().runTaskLater(FtcCore.getInstance(), () -> DELAY_SET.remove(player), cooldownInTicks);
    }

    public static void add(Player player, int cooldownInTicks){
        add(player, cooldownInTicks, false);
    }

    public static void add(Player player, @Nonnull String s, int cooldownInTicks){
        add(player, s, cooldownInTicks, false);
    }
}
