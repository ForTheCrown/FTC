package net.forthecrown.emperor.useables;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.utils.CrownUtils;
import net.kyori.adventure.key.Key;

import java.util.ArrayList;
import java.util.List;

public abstract class UsableBase extends CheckableBase implements Actionable, Preconditionable{

    protected final List<UsageAction> actions = new ArrayList<>();

    protected UsableBase(){}

    public void saveInto(JsonObject json){
        saveChecksInto(json);

        JsonArray array = new JsonArray();

        for (UsageAction a: getActions()){
            JsonObject object = new JsonObject();
            object.add("type", new JsonPrimitive(a.key().asString()));
            object.add("value", a.serialize());

            array.add(object);
        }

        json.add("actions", array);
    }

    public void reloadFrom(JsonObject json) throws CommandSyntaxException {
        reloadChecksFrom(json);

        actions.clear();
        JsonElement actionsElement = json.get("actions");
        if(actionsElement == null || !actionsElement.isJsonArray()) return;

        for (JsonElement e: actionsElement.getAsJsonArray()){
            JsonObject j = e.getAsJsonObject();
            UsageAction action = CrownCore.getActionRegistry().getAction(CrownUtils.parseKey(j.get("type").getAsString()));

            try {
                action.parse(j.get("value"));
            } catch (CommandSyntaxException ignored) {}

            actions.add(action);
        }
    }

    @Override
    public void addAction(UsageAction action) {
        actions.add(action);
    }

    @Override
    public void removeAction(int index) {
        actions.remove(index);
    }

    @Override
    public List<UsageAction> getActions() {
        return actions;
    }

    @Override
    public void clearActions() {
        actions.clear();
    }

    @Override
    public <T extends UsageAction> T getAction(Key key, Class<T> clazz) {
        for (UsageAction a: actions){
            if(!a.key().equals(key)) continue;
            if(!clazz.isAssignableFrom(a.getClass())) continue;
            return (T) a;
        }

        return null;
    }
}
