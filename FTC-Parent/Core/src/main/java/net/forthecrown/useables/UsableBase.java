package net.forthecrown.useables;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.serializer.JsonBuf;
import net.forthecrown.useables.actions.UsageActionInstance;
import net.forthecrown.utils.CrownUtils;
import net.forthecrown.utils.InterUtils;
import net.kyori.adventure.key.Key;

import java.util.ArrayList;
import java.util.List;

public abstract class UsableBase extends CheckableBase implements Actionable, Preconditionable{

    protected final List<UsageActionInstance> actions = new ArrayList<>();

    protected UsableBase(){}

    public void saveInto(JsonObject json){
        saveChecksInto(json);

        JsonArray array = new JsonArray();

        for (UsageActionInstance a: getActions()){
            JsonObject object = new JsonObject();
            object.add("type", new JsonPrimitive(a.typeKey().asString()));
            object.add("value", InterUtils.writeAction(a));

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
            JsonBuf j = JsonBuf.of(e.getAsJsonObject());

            try {
                addAction(InterUtils.readAction(j.getString("type"), j.get("value")));
            } catch (CommandSyntaxException exception) {
                exception.printStackTrace();
            }
        }
    }

    @Override
    public void addAction(UsageActionInstance action) {
        actions.add(action);
    }

    @Override
    public void removeAction(int index) {
        actions.remove(index);
    }

    @Override
    public List<UsageActionInstance> getActions() {
        return actions;
    }

    @Override
    public void clearActions() {
        actions.clear();
    }

    @Override
    public <T extends UsageActionInstance> T getAction(Key key, Class<T> clazz) {
        key = CrownUtils.checkNotBukkit(key);
        for (UsageActionInstance a: actions){
            if(!a.typeKey().equals(key)) continue;
            if(!clazz.isAssignableFrom(a.getClass())) continue;
            return (T) a;
        }

        return null;
    }
}
