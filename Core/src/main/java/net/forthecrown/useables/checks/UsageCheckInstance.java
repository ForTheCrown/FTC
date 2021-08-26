package net.forthecrown.useables.checks;

import net.forthecrown.useables.UsageTypeInstance;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * An instance of a usage check
 */
public interface UsageCheckInstance extends Predicate<Player>, UsageTypeInstance {

    /**
     * Gets the message to show to a player when they fail this check
     * @return The failure message
     */
    @Nullable Component failMessage();

    /**
     * Gets the action to perform when all the checks for an interaction have passed successfully.
     * @return ^^^^^^^^^^
     */
    default Consumer<Player> onSuccess(){
        return null;
    }

    /**
     * Gets a personalized failure message
     * <p>
     * By default returns {@link UsageCheckInstance#failMessage()}
     * </p>
     * @param player The player to get the message of
     * @return the personalized failure message
     */
    default @Nullable Component personalizedMessage(Player player){
        return failMessage();
    }
}