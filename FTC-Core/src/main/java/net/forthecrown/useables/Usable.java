package net.forthecrown.useables;

import net.forthecrown.serializer.CrownSerializer;
import net.forthecrown.serializer.Deletable;
import org.bukkit.entity.Player;

import java.util.function.Predicate;

/**
 * Represents any usable object in the game world
 * <p>
 * UsableEntity and UsableBlock are currently the only sub classes of this
 * </p>
 */
public interface Usable extends Predicate<Player>, CrownSerializer, Deletable, Checkable, Actionable {

    /**
     * Interacts with this usable
     * @param player The player that's interacting
     */
    void interact(Player player);

    /**
     * Whether this usable should send failure messages when failing the checks
     * @return ^^^^^^
     */
    boolean sendFailMessage();

    /**
     * Set whether this usable should send failure messages
     * @param send Whether failure messages should be sent after failing a check
     */
    void setSendFail(boolean send);

    void setCancelVanilla(boolean cancelVanilla);

    boolean cancelVanillaInteraction();
}
