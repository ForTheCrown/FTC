package net.forthecrown.core.goalbook;

import com.google.gson.JsonElement;
import net.forthecrown.core.Keys;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.utils.JsonUtils;
import net.forthecrown.utils.Nameable;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class GoalBookChallenge implements Keyed, Nameable, JsonSerializable {
    private final String name;
    private final Key key;
    private final Component[] description;
    private final GoalBook.Category category;
    private final int target;
    private final int exp;
    private final Function<GoalBookChallenge, GoalBookListener> listenerFactory;

    private GoalBookListener listener;

    public GoalBookChallenge(String name,
                             GoalBook.Category category,
                             int target, int exp,
                             Function<GoalBookChallenge, GoalBookListener> listenerFactory,
                             Component... desc
    ) {
        this.name = name;
        this.category = category;
        this.description = desc;
        this.target = target;
        this.exp = exp;
        this.listenerFactory = listenerFactory;
        this.key = Keys.forthecrown(name.toLowerCase().replaceAll(" ", "_"));
    }

    public void initializeListener() {
        shutdownListener();

        listener = listenerFactory.apply(this);
        listener.register();
    }

    public void shutdownListener() {
        if(listener == null) return;

        listener.unregister();
        listener = null;
    }

    public int getTarget() {
        return target;
    }

    public int getExp() {
        return exp;
    }

    @Override
    public String getName() {
        return name;
    }

    public GoalBook.Category getCategory() {
        return category;
    }

    @Override
    public @NotNull Key key() {
        return key;
    }

    public Component[] getDescription() {
        return description;
    }

    @Override
    public JsonElement serialize() {
        return JsonUtils.writeKey(key);
    }
}
