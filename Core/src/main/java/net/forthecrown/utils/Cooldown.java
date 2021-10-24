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
 * A class that helps adding cooldowns to things.
 */
public final class Cooldown {
    /**
     * The general category, literally just "general"
     */
    public static final String GENERAL = "general";

    private static final Map<String, Set<CommandSender>> COOLDOWN_MAP = new HashMap<>();

    private Cooldown(){}

    /**
     * Checks whether the sender is in the {@link Cooldown#GENERAL} category
     * @param sender The sender to check for
     * @return If the sender is on {@link Cooldown#GENERAL} cooldown
     */
    public static boolean contains(@NotNull CommandSender sender){
        return contains(sender, GENERAL);
    }

    /**
     * Checks whether the sender is on cooldown in the given category
     * @param sender The sender to check
     * @param category The category to check
     * @return If the sender is on cooldown for the given category
     */
    public static boolean contains(@NotNull CommandSender sender, @NotNull String category){
        Validate.notNull(sender, "Sender was null");
        Validate.notNull(category, "Category was null");

        return COOLDOWN_MAP.computeIfAbsent(category, k -> new HashSet<>()).contains(sender);
    }

    /**
     * Same as {@link Cooldown#containsOrAdd(CommandSender, String, int)} except for {@link Cooldown#GENERAL}
     * @param sender The sender to check
     * @param ticks The duration of the cooldown
     * @return Same as {@link Cooldown#containsOrAdd(CommandSender, String, int)}
     */
    public static boolean containsOrAdd(CommandSender sender, @Nonnegative int ticks) {
        return containsOrAdd(sender, GENERAL, ticks);
    }

    /**
     * Checks whether the given category contains the given sender, if it does, returns true, else
     * adds sender to category for the given duration and returns false
     * @param sender The sender to check
     * @param category The category to check and potentially add to
     * @param ticks The duration of the cooldown
     * @return True if category contains sender, false if it doesn't, if false, adds sender to category
     */
    public static boolean containsOrAdd(CommandSender sender, @NotNull String category, @Nonnegative int ticks) {
        boolean contains = contains(sender, category);

        if(!contains) add(sender, category, ticks);

        return contains;
    }

    /**
     * Adds the given sender to the {@link Cooldown#GENERAL} cooldown for the given ticks
     * @param sender The sender to add
     * @param timeInTicks The time to add them for
     */
    public static void add(@NotNull CommandSender sender, @Nonnegative int timeInTicks){
        add(sender, GENERAL, timeInTicks);
    }

    /**
     * Adds the given sender into the {@link Cooldown#GENERAL} category for forever
     * @param sender The sender to add
     */
    public static void add(@NotNull CommandSender sender){
        add(sender, GENERAL, -1);
    }

    /**
     * Adds the given sender into the given category for forever
     * @param sender The sender to add
     * @param category The category to add them to
     */
    public static void add(@NotNull CommandSender sender, @NotNull String category){
        add(sender, category, -1);
    }

    /**
     * Adds the given sender into the given category for the given duration
     * @param sender The sender to add
     * @param category The category to add the sender to
     * @param timeInTicks The cooldown's duration
     */
    public static void add(@NotNull CommandSender sender, @NotNull String category, int timeInTicks){
        Validate.notNull(sender, "Sender was null");
        Validate.notNull(category, "Category was null");

        Set<CommandSender> set = COOLDOWN_MAP.computeIfAbsent(category, k -> new HashSet<>());
        set.add(sender);

        if(timeInTicks != -1) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(Crown.inst(), () -> remove(sender, category), timeInTicks);
        }
    }

    /**
     * Removes the given sender from any cooldown
     * @param sender The sender to remove
     */
    public static void remove(@NotNull CommandSender sender){
        remove(sender, GENERAL);
    }

    /**
     * Removes the given sender from the given cooldown category
     * @param sender The sender to remove
     * @param category The category to remove the sender from
     */
    public static void remove(@NotNull CommandSender sender, @NotNull String category){
        Validate.notNull(sender, "Sender was null");
        Validate.notNull(category, "Category was null");

        Set<CommandSender> set1 = COOLDOWN_MAP.computeIfAbsent(category, k -> new HashSet<>());
        set1.remove(sender);

        if(set1.isEmpty()) COOLDOWN_MAP.remove(category);
    }
}
