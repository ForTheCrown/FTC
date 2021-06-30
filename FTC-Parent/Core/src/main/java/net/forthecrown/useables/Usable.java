package net.forthecrown.useables;

import net.forthecrown.serializer.CrownSerializer;
import net.forthecrown.serializer.Deletable;
import org.bukkit.entity.Player;

import java.util.function.Predicate;

public interface Usable extends Predicate<Player>, CrownSerializer, Deletable, Preconditionable, Actionable {
    void interact(Player player);

    boolean sendFailMessage();

    void setSendFail(boolean send);
}
