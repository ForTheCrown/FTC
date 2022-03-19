package net.forthecrown.core.battlepass;

import com.google.gson.JsonElement;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Keys;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

public abstract class Reward implements Keyed, JsonSerializable {
    private final Key key;

    public Reward(String keyVal) {
        this.key = Keys.forthecrown(keyVal);
    }

    public abstract Component display(RewardInstance instance);

    @Override
    public @NotNull Key key() {
        return key;
    }

    @Override
    public JsonElement serialize() {
        return JsonUtils.writeKey(key);
    }

    public boolean test(CrownUser user, RewardInstance instance) {
        if(instance.donatorExclusive() && !user.isGoalBookDonator()) return false;

        BattlePass.Progress progress = Crown.getBattlePass().getProgress(user.getUniqueId());
        return progress.availableRewards().contains(instance);
    }

    public final boolean award(CrownUser user, RewardInstance instance) {
        BattlePass.Progress progress = Crown.getBattlePass().getProgress(user.getUniqueId());

        if(!onClaim(user, instance)) {
            user.sendMessage(
                    Component.translatable("battlePass.reward.cannotClaim")
                            .color(NamedTextColor.RED)
            );
            return false;
        }

        progress.claimedRewards().add(instance);
        user.sendMessage(
                Component.translatable("battlePass.reward.got", NamedTextColor.YELLOW,
                        display(instance).color(NamedTextColor.GOLD)
                )
        );
        return true;
    }

    protected abstract boolean onClaim(CrownUser user, RewardInstance instance);
}
