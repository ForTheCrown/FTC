package net.forthecrown.core.api;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.enums.Branch;
import net.forthecrown.core.enums.Rank;
import net.forthecrown.core.types.signs.SignAction;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.Predicate;

public interface CrownSign extends CrownSerializer<FtcCore>, Predicate<Player>, Deleteable {
    void interact(Player player);

    Location getLocation();

    int getRequiredGems();

    void setRequiredGems(int requiredGems);

    int getRequiredBal();

    void setRequiredBal(int requiredBal);

    int getCooldown();

    void setCooldown(int cooldown);

    String getRequiredPermission();

    void setRequiredPermission(String requiredPermission);

    Branch getRequiredBranch();

    void setRequiredBranch(Branch requiredBranch);

    Rank getRequiredRank();

    void setRequiredRank(Rank requiredRank);

    List<SignAction> getActions();

    void addAction(SignAction action);

    void removeAction(int index);

    void clearActions();

    Sign getSign();

    boolean sendFailMessage();

    void setFailSendMessage(boolean send);

    boolean sendCooldownMessage();

    void setSendCooldownMessage(boolean send);

    @Override
    boolean test(Player player);
}
