package net.forthecrown.core.battlepass;

import com.google.gson.JsonElement;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.user.CrownUser;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

public record RewardInstance(Reward reward, JsonElement data, int level, boolean donatorExclusive, long id) implements JsonSerializable, Predicate<CrownUser> {
    static final AtomicLong ID_GENERATOR = new AtomicLong(0L);

    public RewardInstance(Reward reward, JsonElement data, int level, boolean donatorExclusive) {
        this(reward, data, level, donatorExclusive, ID_GENERATOR.getAndIncrement());
    }

    public static RewardInstance read(JsonElement element) {
        JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());

        Reward reward = Rewards.read(json.get("reward"));
        long id = json.has("id") ? json.getLong("id") : ID_GENERATOR.getAndIncrement();

        ID_GENERATOR.set(Math.max(id + 1, ID_GENERATOR.get()));

        return new RewardInstance(reward,
                json.get("data"),
                json.getInt("level"),
                json.getBool("donatorExclusive"),
                id
        );
    }

    @Override
    public JsonElement serialize() {
        JsonWrapper json = JsonWrapper.empty();
        json.addKey("reward", reward.key());
        json.add("data", data);
        json.add("level", level);
        json.add("donatorExclusive", donatorExclusive);
        json.add("id", id);

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