package net.forthecrown.emperor.useables;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.serializer.AbstractJsonSerializer;
import net.forthecrown.emperor.utils.CrownUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Consumer;

public abstract class AbstractUsable extends AbstractJsonSerializer<CrownCore> implements Usable {

    protected final List<UsageAction> actions = new ArrayList<>();
    protected final Map<Key, UsageCheck> checks = new HashMap<>();
    protected boolean sendFail;

    protected AbstractUsable(String fileName, String directory, boolean stopIfFileDoesntExist) {
        super(fileName, directory, stopIfFileDoesntExist, CrownCore.inst());
    }

    protected void deleteFile(){
        super.delete();
    }

    public abstract void delete();

    protected void saveInto(JsonObject json){
        json.add("sendFail", new JsonPrimitive(sendFail));

        JsonObject preconditions = new JsonObject();
        for (UsageCheck p: getChecks()){
            preconditions.add(p.key().asString(), p.serialize());
        }
        json.add("preconditions", preconditions);

        JsonArray array = new JsonArray();

        for (UsageAction a: getActions()){
            JsonObject object = new JsonObject();
            object.add("type", new JsonPrimitive(a.key().asString()));
            object.add("value", a.serialize());

            array.add(object);
        }

        json.add("actions", array);
    }

    protected void reloadFrom(JsonObject json) throws CommandSyntaxException {
        sendFail = json.get("sendFail").getAsBoolean();

        checks.clear();
        JsonElement precons = json.get("preconditions");
        if(precons != null && precons.isJsonObject()){
            for (Map.Entry<String, JsonElement> e: precons.getAsJsonObject().entrySet()){
                UsageCheck pre = CrownCore.getCheckRegistry().getCheck(CrownUtils.parseKey(e.getKey()));

                try {
                    pre.parse(e.getValue());
                } catch (CommandSyntaxException ignored) {}

                checks.put(pre.key(), pre);
            }
        }

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
    public void interact(Player player) {
        if(!test(player)) return;
        actions.forEach(a -> a.onInteract(player));
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
    public List<UsageCheck> getChecks() {
        return new ArrayList<>(checks.values());
    }

    @Override
    public void addCheck(UsageCheck precondition) {
        checks.put(precondition.key(), precondition);
    }

    @Override
    public void removeCheck(Key name) {
        checks.remove(name);
    }

    @Override
    public void clearChecks() {
        checks.clear();
    }

    @Override
    public Set<Key> getCheckTypes() {
        return checks.keySet();
    }

    @Override
    public boolean sendFailMessage() {
        return sendFail;
    }

    @Override
    public void setSendFail(boolean send) {
        this.sendFail = send;
    }

    @Override
    public boolean test(Player player) {
        List<Consumer<Player>> onSuccesses = new ArrayList<>();
        for (UsageCheck p: checks.values()){
            if(!p.test(player)){
                if(sendFail){
                    Component failMsg = p.getPersonalizedFailMessage(player);
                    if(failMsg != null) player.sendMessage(failMsg);
                }
                return false;
            }

            Consumer<Player> s = p.onSuccess();
            if(s != null) onSuccesses.add(s);
        }

        onSuccesses.forEach(c -> c.accept(player));
        return true;
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

    @Override
    public <T extends UsageCheck> T getCheck(Key key, Class<T> clazz) {
        if(!checks.containsKey(key)) return null;

        UsageCheck c = checks.get(key);
        if(!clazz.isAssignableFrom(c.getClass())) return null;
        return (T) c;
    }
}
