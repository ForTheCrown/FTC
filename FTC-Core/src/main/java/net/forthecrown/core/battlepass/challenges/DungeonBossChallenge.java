package net.forthecrown.core.battlepass.challenges;

import net.forthecrown.core.Crown;
import net.forthecrown.core.battlepass.BattlePass;
import net.forthecrown.dungeons.Bosses;
import net.forthecrown.dungeons.boss.KeyedBoss;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;

public class DungeonBossChallenge extends BattlePassChallenge {
    public DungeonBossChallenge(String name, BattlePass.Category category, int target, int exp, Component... desc) {
        super(name, category, target, exp, desc);
    }

    public void trigger(CrownUser user, KeyedBoss boss) {
        if(!BattlePass.ENABLED) return;
        if(!isEnabled()) return;
        if(Bosses.ACCESSOR.getStatus(user.getDataContainer(), boss)) return;

        Crown.getBattlePass().getProgress(user.getUniqueId()).increment(this, 1);
    }

    @Override
    public void onReset() {
        if(!BattlePass.ENABLED) return;
        // Reset all bosses to allow people to kill them
        // again
        Crown.getUserManager().getAllUsers()
                .whenComplete((users, throwable) -> {
                    if(throwable != null) {
                        Crown.logger().error("Could not load all users", throwable);
                        return;
                    }

                    for (CrownUser user: users) {
                        Bosses.ACCESSOR.clear(user.getDataContainer());
                    }

                    Crown.logger().info("Reset all users' battlepass dungeon boss progress");
                    Crown.getUserManager().unloadOffline();
                });
    }
}
