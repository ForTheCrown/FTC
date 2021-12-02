package net.forthecrown.economy.pirates.merchants;

import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.npc.InteractableNPC;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.data.Faction;
import net.forthecrown.user.manager.UserManager;
import net.kyori.adventure.key.Keyed;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public interface UsablePirateNpc extends Keyed, InteractableNPC {
    void onUse(CrownUser user, Entity entity);

    @Override
    default void run(Player player, Entity entity) throws RoyalCommandException {
        CrownUser user = UserManager.getUser(player);

        if(user.getFaction() != Faction.PIRATES) throw FtcExceptionProvider.realPirate(entity);

        onUse(user, entity);
    }
}
