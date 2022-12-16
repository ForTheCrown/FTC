package net.forthecrown.dungeons.boss;

import lombok.Getter;
import net.forthecrown.user.User;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.Users;
import net.forthecrown.utils.math.WorldBounds3i;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public abstract class KeyedBossImpl extends AbstractBoss implements KeyedBoss {
    @Getter
    private final String key;

    public KeyedBossImpl(String name, Location spawn, WorldBounds3i room, SpawnTest requirement) {
        super(name, spawn, room, requirement);

        this.key = name.toLowerCase().replaceAll(" ", "_");
    }

    protected void giveRewards(Player player) {}

    protected void finalizeKill(BossContext context) {
        for (Player p: context.players()) {
            // Players outside of the room during the kill,
            // or alt accounts, cannot earn rewards
            if (!getRoom().contains(p)) {
                continue;
            }

            if (UserManager.get()
                    .getAlts()
                    .isAltForAny(p.getUniqueId(), context.players())
            ) {
                continue;
            }

            User user = Users.get(p);

            // Give the advancement and
            // any other awards
            awardAdvancement(p);
            giveRewards(p);
        }
    }
}