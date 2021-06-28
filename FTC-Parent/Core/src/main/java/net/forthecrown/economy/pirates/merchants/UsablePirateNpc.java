package net.forthecrown.economy.pirates.merchants;

import net.forthecrown.user.CrownUser;
import net.kyori.adventure.key.Keyed;
import org.bukkit.entity.Entity;

public interface UsablePirateNpc extends Keyed {
    void onUse(CrownUser user, Entity entity);
}
