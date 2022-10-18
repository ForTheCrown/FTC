package net.forthecrown.useables;

import net.forthecrown.text.writer.TextWriter;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.Player;

/**
 * The all-in-one usable object with both usage actions and checks.
 */
public interface Usable extends ActionHolder, CheckHolder {
    /**
     * Saves the usable into the given tag
     * @param tag The tag to save to
     */
    void save(CompoundTag tag);

    /**
     * Loads the usable from the given tag
     * @param tag The tag to load from
     */
    void load(CompoundTag tag);

    /**
     * Writes admin info about this usable into the given
     * writer
     * @param writer The writer to write to
     */
    default void adminInfo(TextWriter writer) {
        writer.field("Silent", isSilent());

        writer.field("Actions", "");
        getActions().write(writer);

        writer.field("Checks", "");
        getChecks().write(writer);
    }

    /**
     * Runs the interaction check and, if those pass,
     * runs the usage actions of this usable
     * @param player The player interacting with this usable
     * @return True, if all checks passed, false otherwise
     */
    default boolean interact(Player player) {
        if (!testInteraction(player)) {
            return false;
        }

        onInteract(player);
        return true;
    }
}