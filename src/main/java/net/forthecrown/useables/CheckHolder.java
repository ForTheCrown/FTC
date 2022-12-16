package net.forthecrown.useables;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.Player;

/**
 * A {@link UsageTypeHolder} which holds a list of
 * usage checks
 */
public interface CheckHolder extends UsageTypeHolder {
    /**
     * The NBT tag this holder uses
     */
    String CHECKS_TAG = "checks";

    /**
     * Gets the list of checks this holder has
     * @return The check list
     */
    UsageTypeList<UsageTest> getChecks();

    /**
     * Checks if this check holder should send failure message
     * when checks fail for players
     * @return True, if this check holder should NOT send messages
     */
    boolean isSilent();

    /**
     * Sets if this check holder should tell
     * players why they failed checks
     * @param silent
     */
    void setSilent(boolean silent);

    /**
     * Gets the first {@link UsageTest} that fails
     * for the given player
     * @param player The player to check
     * @return The failed check instance, null, if all passed
     */
    default UsageTest getFail(Player player) {
        for (var v: getChecks()) {
            // Test if we fail this instance's check
            // If we do, return it
            if (!v.test(player, this)) {
                return v;
            }
        }

        return null;
    }

    default boolean test(Player player) {
        return getFail(player) == null;
    }

    default boolean testInteraction(Player player) {
        // Get the test they failed on
        var failed = getFail(player);

        // Null means they passed all
        if (failed == null) {
            // Call the onPass function for all checks
            for (var v: getChecks()) {
                v.postTests(player, this);
            }

            return true;
        }

        var message = failed.getFailMessage(player, this);

        // Silent -> Don't send message
        // Or message is null lol
        if (isSilent() || message == null) {
            return false;
        }

        player.sendMessage(message);
        return false;
    }

    /**
     * Saves checks into the given tag.
     * This will add the {@link #CHECKS_TAG} entry
     * to the given tag and save all checks
     * to that entry.
     *
     * Also saves the {@link #isSilent()} into
     * the given tag.
     *
     * @param tag The tag to save to
     */
    default void saveChecks(CompoundTag tag) {
        tag.putBoolean("silent", isSilent());

        // If the checks are empty, don't save
        if (getChecks().isEmpty()) {
            return;
        }

        tag.put(CHECKS_TAG, getChecks().save());
    }

    /**
     * Loads all checks from the given tag.
     *
     * Loads {@link #isSilent()} from the given
     * tag as well.
     *
     * @param tag The tag to load from
     * @throws CommandSyntaxException idk lol
     */
    default void loadChecks(CompoundTag tag) throws CommandSyntaxException {
        setSilent(tag.getBoolean("silent"));

        // Clear checks list, because we're re-reading
        // its contents
        getChecks().clear();

        // If there's no checks tag, just clear
        // the checks list
        if (!tag.contains(CHECKS_TAG)) {
            return;
        }

        // Load checks
        getChecks().load(tag.get(CHECKS_TAG));
    }
}