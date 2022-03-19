package net.forthecrown.core.battlepass;

import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.forthecrown.core.Crown;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.inventory.ItemStackBuilder;
import net.forthecrown.registry.Registries;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Set;
import java.util.UUID;

public final class Rewards {
    private Rewards() {}

    public static final Reward GIVE_GEMS = register(
            new Reward("give_gems") {
                @Override
                public Component display(RewardInstance instance) {
                    return FtcFormatter.gems(instance.data().getAsInt());
                }

                @Override
                protected boolean onClaim(CrownUser user, RewardInstance instance) {
                    user.addGems(instance.data().getAsInt());
                    return true;
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
                protected boolean onClaim(CrownUser user, RewardInstance instance) {
                    user.addBalance(instance.data().getAsInt());
                    return true;
                }
            }
    );

    public static final Reward GIVE_ITEM = register(
            new Reward("give_item") {
                ItemStack getItem(RewardInstance instance) {
                    return JsonUtils.readItem(instance.data());
                }

                @Override
                public Component display(RewardInstance instance) {
                    return FtcFormatter.itemAndAmount(getItem(instance));
                }

                @Override
                protected boolean onClaim(CrownUser user, RewardInstance instance) {
                    if(user.getInventory().firstEmpty() == -1) return false;

                    user.getInventory().addItem(getItem(instance));
                    return true;
                }
            }
    );

    public static final Reward GIVE_HEAD = register(
            new Reward("give_head") {
                @Override
                public Component display(RewardInstance instance) {
                    JsonWrapper json = JsonWrapper.of(instance.data().getAsJsonObject());
                    return json.getComponent("display");
                }

                @Override
                protected boolean onClaim(CrownUser user, RewardInstance instance) {
                    if(user.getInventory().firstEmpty() == -1) return false;

                    JsonWrapper json = JsonWrapper.of(instance.data().getAsJsonObject());
                    String textureLink = json.getString("texture");

                    ItemStackBuilder builder = new ItemStackBuilder(Material.PLAYER_HEAD, json.getInt("amount", 1))
                            .setProfile(FtcUtils.profileWithTextureID(json.getString("name", null), UUID.randomUUID(), textureLink))
                            .setName(
                                    Component.text()
                                            .decoration(TextDecoration.ITALIC, false)
                                            .append(json.getComponent("display"))
                                            .build()
                            );

                    user.getInventory().addItem(builder.build());
                    return true;
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

        for (RewardInstance i: Crown.getBattlePass().getCurrentRewards()) {
            if(i.level() == level) result.add(i);
        }

        return result;
    }
}