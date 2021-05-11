package net.forthecrown.core.api;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.types.interactable.InteractionAction;
import net.forthecrown.core.types.interactable.InteractionCheck;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public interface Interactable extends Predicate<Player>, CrownSerializer<FtcCore>, Deleteable {
    void interact(Player player);

    void addAction(InteractionAction action);
    void removeAction(int index);

    List<InteractionAction> getActions();

    void clearActions();

    List<InteractionCheck> getPreconditions();

    void addPrecondition(InteractionCheck precondition);
    void removePrecondition(String name);

    void clearPreconditions();

    Set<String> getPreconditionTypes();

    boolean sendFailMessage();

    void setSendFail(boolean send);
}
