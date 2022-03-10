package net.forthecrown.core.battlepass;

import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.forthecrown.core.Crown;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.registry.Registries;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;

import java.util.Set;

public final class Rewards {
    private Rewards() {}

    public static final Reward GIVE_GEMS = register(
            new Reward("give_gems") {
                @Override
                public Component display(RewardInstance instance) {
                    return FtcFormatter.gems(instance.data().getAsInt());
                }

                @Override
                protected void onClaim(CrownUser user, RewardInstance instance) {
                    user.addGems(instance.data().getAsInt());
                }
            }
    );

    public static final Reward GIVE_RHINES = register(
            new Reward("give_rhines") {
                @Override
                public Component display(RewardInstance instance) {
                    return FtcFormatter.rhines(instance.data().getAsInt());
                }

                @Override
                protected void onClaim(CrownUser user, RewardInstance instance) {
                    user.addBalance(instance.data().getAsInt());
                }
            }
    );

    public static void init() {
        Registries.REWARDS.close();

        Crown.logger().info("GoalBook Rewards initialized");
    }

    private static Reward register(Reward r) {
        return Registries.REWARDS.register(r.key(), r);
    }

    public static Reward read(JsonElement element) {
        return Registries.REWARDS.read(element);
    }

    public static Set<RewardInstance> getForLevel(int level) {
        Set<RewardInstance> result = new ObjectOpenHashSet<>();

        for (Reward r: Registries.REWARDS) {
        }

        return result;
    }
}