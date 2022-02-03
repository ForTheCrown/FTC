package net.forthecrown.core.goalbook;

import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.forthecrown.commands.click.ClickableTextNode;
import net.forthecrown.commands.click.ClickableTexts;
import net.forthecrown.core.Crown;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.registry.Registries;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;

import java.util.Set;

public final class Rewards {
    private Rewards() {}

    public static Reward GIVE_100_GEMS = register(
            new Reward("100_gems", "100 Gems", 2, false) {
                @Override
                public Component display() {
                    return FtcFormatter.gems(100);
                }

                @Override
                protected void onClaim(CrownUser user) {
                    user.addGems(100);
                }
            }
    );

    private static ClickableTextNode claimNode;

    public static void init() {
        Registries.REWARDS.close();

        claimNode = new ClickableTextNode("gb_reward_claiming");
        for (Reward r: Registries.REWARDS) {
            claimNode.addNode(r.getClaimingNode());
        }

        ClickableTexts.register(claimNode);

        Crown.logger().info("GoalBook Rewards initialized");
    }

    public static ClickableTextNode getClaimNode() {
        return claimNode;
    }

    private static Reward register(Reward r) {
        return Registries.REWARDS.register(r.key(), r);
    }

    public static Reward read(JsonElement element) {
        return Registries.REWARDS.read(element);
    }

    public static Set<Reward> getForLevel(int level) {
        Set<Reward> result = new ObjectOpenHashSet<>();

        for (Reward r: Registries.REWARDS) {
            if(r.getLevel() <= level) result.add(r);
        }

        return result;
    }
}