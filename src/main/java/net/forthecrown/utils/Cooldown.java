package net.forthecrown.utils;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.Exceptions;
import net.kyori.adventure.audience.Audience;
import org.apache.commons.lang.Validate;
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
    private Cooldown(){}
    /**
     * The general category, literally just "general"
     */
    public static final String GENERAL = "general";

    /**
     * A cooldown time which never ends
     */
    public static final int NO_END_COOLDOWN = -1;

    private static final Map<String, Set<Audience>> COOLDOWN_MAP = new HashMap<>();

    /**
     * Checks whether the sender is in the {@link Cooldown#GENERAL} category
     * @param sender The sender to check for
     * @return If the sender is on {@link Cooldown#GENERAL} cooldown
     */
    public static boolean contains(@NotNull Audience sender) {
        return contains(sender, GENERAL);
    }

    /**
     * Checks whether the sender is on cooldown in the given category
     * @param sender The sender to check
     * @param category The category to check
     * @return If the sender is on cooldown for the given category
     */
    public static boolean contains(@NotNull Audience sender, @NotNull String category){
        Validate.notNull(sender, "Sender was null");
        Validate.notNull(category, "Category was null");

        var set = COOLDOWN_MAP.get(category);

        // The set is null, therefor the category
        // doesn't even exist in the map
        if (set == null) {
            return false;
        }

        return set.contains(sender);
    }

    /**
     * Same as {@link Cooldown#containsOrAdd(Audience, String, int)} except for {@link Cooldown#GENERAL}
     * @param sender The sender to check
     * @param ticks The duration of the cooldown
     * @return Same as {@link Cooldown#containsOrAdd(Audience, String, int)}
     */
    public static boolean containsOrAdd(Audience sender, @Nonnegative int ticks) {
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
    public static boolean containsOrAdd(Audience sender, @NotNull String category, @Nonnegative int ticks) {
        boolean contains = contains(sender, category);

        // Sender is not in the map, add them lol
        if(!contains) {
            add(sender, category, ticks);
        }

        return contains;
    }

    /**
     * Adds the given sender to the {@link Cooldown#GENERAL} cooldown for the given ticks
     * @param sender The sender to add
     * @param timeInTicks The time to add them for
     */
    public static void add(@NotNull Audience sender, @Nonnegative int timeInTicks) {
        add(sender, GENERAL, timeInTicks);
    }

    /**
     * Adds the given sender into the {@link Cooldown#GENERAL} category for forever
     * @param sender The sender to add
     */
    public static void add(@NotNull Audience sender){
        add(sender, GENERAL, NO_END_COOLDOWN);
    }

    /**
     * Adds the given sender into the given category for forever
     * @param sender The sender to add
     * @param category The category to add them to
     */
    public static void add(@NotNull Audience sender, @NotNull String category){
        add(sender, category, NO_END_COOLDOWN);
    }

    /**
     * Adds the given sender into the given category for the given duration
     * @param sender The sender to add
     * @param category The category to add the sender to
     * @param timeInTicks The cooldown's duration
     */
    public static void add(@NotNull Audience sender, @NotNull String category, int timeInTicks) {
        Validate.notNull(sender, "Sender was null");
        Validate.notNull(category, "Category was null");

        // Don't enter cooldown if the cooldown length
        // is 0
        if (timeInTicks == 0) {
            return;
        }

        var set = COOLDOWN_MAP.computeIfAbsent(category, k -> new HashSet<>());
        set.add(sender);

        if (timeInTicks != NO_END_COOLDOWN) {
            Tasks.runLaterAsync(() -> remove(sender, category), timeInTicks);
        }
    }

    /**
     * Removes the given sender from any cooldown
     * @param sender The sender to remove
     */
    public static void remove(@NotNull Audience sender){
        remove(sender, GENERAL);
    }

    /**
     * Removes the given sender from the given cooldown category
     * @param sender The sender to remove
     * @param category The category to remove the sender from
     */
    public static void remove(@NotNull Audience sender, @NotNull String category){
        Validate.notNull(sender, "Sender was null");
        Validate.notNull(category, "Category was null");

        var set1 = COOLDOWN_MAP.computeIfAbsent(category, k -> new HashSet<>());
        set1.remove(sender);

        // Since we just removed an entry from the category's list
        // we'll check if it's empty, if it is, remove it from the map
        if(set1.isEmpty()) {
            COOLDOWN_MAP.remove(category);
        }
    }

    public static void testAndThrow(Audience audience) throws CommandSyntaxException {
        testAndThrow(audience, GENERAL, NO_END_COOLDOWN);
    }

    public static void testAndThrow(Audience audience, int ticks) throws CommandSyntaxException {
        testAndThrow(audience, GENERAL, ticks);
    }

    public static void testAndThrow(Audience audience, String category, int ticks) throws CommandSyntaxException {
        if (containsOrAdd(audience, category, ticks)) {
            throw Exceptions.onCooldown(Time.ticksToMillis(ticks));
        }
    }
}