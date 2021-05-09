package net.forthecrown.core.api;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.types.signs.SignAction;
import net.forthecrown.core.types.signs.SignPrecondition;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public interface CrownSign extends CrownSerializer<FtcCore>, Predicate<Player>, Deleteable {
    void interact(Player player);

    Location getLocation();

    List<SignAction> getActions();

    void addAction(SignAction action);

    void removeAction(int index);

    void clearActions();

    Sign getSign();

    boolean sendFailMessage();

    void setSendFail(boolean send);

    List<SignPrecondition> getPreconditions();

    Set<String> getPreconditionTypes();

    void addPrecondition(SignPrecondition precondition);

    void removePrecondition(String name);

    void clearPreconditions();

    @Override
    boolean test(Player player);
}
