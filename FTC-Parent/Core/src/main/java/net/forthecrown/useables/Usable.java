package net.forthecrown.core.useables;

import net.forthecrown.core.CrownCore;
import net.forthecrown.core.serializer.CrownSerializer;
import net.forthecrown.core.serializer.Deleteable;
import org.bukkit.entity.Player;

import java.util.function.Predicate;

public interface Usable extends Predicate<Player>, CrownSerializer<CrownCore>, Deleteable, Preconditionable, Actionable {
    void interact(Player player);

    boolean sendFailMessage();

    void setSendFail(boolean send);
}
