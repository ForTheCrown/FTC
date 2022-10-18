package net.forthecrown.useables;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.Player;

/**
 * A {@link UsageTypeHolder} that holds a list
 * of {@link UsageAction}s.
 */
public interface ActionHolder extends CheckHolder {
    /**
     * The NBT tag actions are saved to
     */
    String ACTIONS_TAG = "actions";

    /**
     * Gets the list of actions this holder has
     * @return The action list
     */
    UsageTypeList<UsageAction> getActions();

    /**
     * Runs all usage actions on the given player
     * @param player The player to actions on
     */
    default void onInteract(Player player) {
        for (var a: getActions()) {
            a.onUse(player, this);
        }
    }

    /**
     * Saves all actions into the given tag
     * @param tag The tag to save actions to
     */
    default void saveActions(CompoundTag tag) {
        // Don't serialize an empty list
        if (getActions().isEmpty()) {
            return;
        }

        tag.put(ACTIONS_TAG, getActions().save());
    }

    /**
     * Loads all actions from the given tag
     * @param tag The tag to laod from
     * @throws CommandSyntaxException idk lmao
     */
    default void loadActions(CompoundTag tag) throws CommandSyntaxException {
        // Clear checks list, because we're re-reading
        // its contents
        getActions().clear();

        // No actions tag? Don't load the list
        if (!tag.contains(ACTIONS_TAG)) {
            return;
        }

        // Load list with usage action registry
        getActions().load(tag.get(ACTIONS_TAG));
    }
}