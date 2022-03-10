package net.forthecrown.core.battlepass;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.registry.Registries;
import net.forthecrown.serializer.JsonSerializable;

public class CurrentChallenges extends ObjectArrayList<BattlePassChallenge> implements JsonSerializable {
    private final BattlePass.Category category;

    public CurrentChallenges(BattlePass.Category category) {
        this.category = category;
    }

    public BattlePass.Category getCategory() {
        return category;
    }

    public void setListening(boolean listenersEnabled) {
        forEach(challenge -> challenge.setEnabled(listenersEnabled));
    }

    @Override
    public JsonElement serialize() {
        JsonArray array = new JsonArray();

        for (BattlePassChallenge c: this) {
            array.add(c.serialize());
        }

        return array;
    }

    public static CurrentChallenges of(JsonElement element, BattlePass.Category category) {
        CurrentChallenges challenges = new CurrentChallenges(category);

        for (JsonElement e: element.getAsJsonArray()) {
            BattlePassChallenge c = Registries.GOAL_BOOK.read(e);
            challenges.add(c);
        }

        return challenges;
    }
}
