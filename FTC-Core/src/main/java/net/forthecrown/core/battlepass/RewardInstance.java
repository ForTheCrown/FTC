package net.forthecrown.core.battlepass;

import com.google.gson.JsonElement;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.user.CrownUser;

import java.util.function.Predicate;

public record RewardInstance(Reward reward, JsonElement data, int level, boolean donatorExclusive) implements JsonSerializable, Predicate<CrownUser> {
    public static RewardInstance read(JsonElement element) {
        JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());

        Reward reward = Rewards.read(json.get("reward"));

        return new RewardInstance(reward,
                json.get("data"),
                json.getInt("level"),
                json.getBool("donatorExclusive")
        );
    }

    @Override
    public JsonElement serialize() {
        JsonWrapper json = JsonWrapper.empty();
        json.addKey("reward", reward.key());
        json.add("data", data);
        json.add("level", level);
        json.add("donatorExclusive", donatorExclusive);

        return json.getSource();
    }

    public void award(CrownUser user) {
        reward.award(user, this);
    }

    @Override
    public boolean test(CrownUser user) {
        return reward.test(user, this);
    }
}
