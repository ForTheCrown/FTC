package net.forthecrown.core.battlepass.challenges;

import net.forthecrown.core.Crown;
import net.forthecrown.core.battlepass.BattlePass;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public abstract class SimpleChallenge extends BattlePassChallenge {
    public SimpleChallenge(String name, BattlePass.Category category, int target, int exp, Component... desc) {
        super(name, category, target, exp, desc);
    }

    public final void trigger(UUID uuid, int amount) {
        if(!BattlePass.ENABLED) return;
        if(!isEnabled()) return;

        onTrigger(Crown.getBattlePass().getProgress(uuid), amount);
    }

    public final void trigger(UUID uuid) {
        trigger(uuid, 1);
    }

    protected abstract void onTrigger(BattlePass.Progress progress, int amount);
}
