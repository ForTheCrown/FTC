package net.forthecrown.core.goalbook;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.registry.Registries;
import net.forthecrown.serializer.JsonSerializable;

public class CurrentChallenges extends ObjectArrayList<GoalBookChallenge> implements JsonSerializable {
    private final GoalBook.Category category;

    public CurrentChallenges(GoalBook.Category category) {
        this.category = category;
    }

    public GoalBook.Category getCategory() {
        return category;
    }

    public void startListeners() {
        forEach(GoalBookChallenge::initializeListener);
    }

    public void stopListeners() {
        forEach(GoalBookChallenge::shutdownListener);
    }

    @Override
    public JsonElement serialize() {
        JsonArray array = new JsonArray();

        for (GoalBookChallenge c: this) {
            array.add(c.serialize());
        }

        return array;
    }

    public static CurrentChallenges of(JsonElement element, GoalBook.Category category) {
        CurrentChallenges challenges = new CurrentChallenges(category);

        for (JsonElement e: element.getAsJsonArray()) {
            GoalBookChallenge c = Registries.GOAL_BOOK.read(e);
            challenges.add(c);
        }

        return challenges;
    }
}
