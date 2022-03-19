package net.forthecrown.dungeons.boss;

import net.forthecrown.core.Crown;
import net.forthecrown.core.Keys;
import net.forthecrown.core.battlepass.challenges.Challenges;
import net.forthecrown.dungeons.Bosses;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserDataContainer;
import net.forthecrown.user.UserManager;
import net.forthecrown.utils.transformation.FtcBoundingBox;
import net.kyori.adventure.key.Key;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class KeyedBossImpl extends AbstractBoss implements KeyedBoss {
    private final Key key;

    public KeyedBossImpl(String name, Location spawn, FtcBoundingBox room, SpawnRequirement requirement) {
        super(name, spawn, room, requirement);

        this.key = Keys.forthecrown(name.toLowerCase().replaceAll(" ", "_"));
    }

    @Override
    public @NotNull Key key() {
        return key;
    }

    protected void giveRewards(Player player) {}

    protected void finalizeKill(BossContext context) {
        for (Player p: context.players()) {
            // Players outside of the room during the kill,
            // or alt accounts, cannot earn rewards
            if(!getRoom().contains(p)) continue;
            if(Crown.getUserManager().isAltForAny(p.getUniqueId(), context.players())) continue;

            CrownUser user = UserManager.getUser(p);

            // This trigger must be ran before as it uses the data
            // accessor to check for the bosses
            Challenges.BEAT_4_DUNGEON_BOSSES.trigger(user, this);

            // I forgot why this exists, but it does lol
            UserDataContainer container = user.getDataContainer();
            Bosses.ACCESSOR.setStatus(container, this, true);

            // Give the advancement and
            // any other awards
            awardAdvancement(p);
            giveRewards(p);
        }
    }
}
