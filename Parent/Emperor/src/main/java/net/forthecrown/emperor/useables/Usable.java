package net.forthecrown.emperor.useables;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.serialization.CrownSerializer;
import net.forthecrown.emperor.serialization.Deleteable;
import org.bukkit.entity.Player;

import java.util.function.Predicate;

public interface Usable extends Predicate<Player>, CrownSerializer<CrownCore>, Deleteable, Preconditionable, Actionable {
    void interact(Player player);

    boolean sendFailMessage();

    void setSendFail(boolean send);
}
