package net.forthecrown.useables;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.forthecrown.serializer.AbstractJsonSerializer;
import net.forthecrown.serializer.JsonBuf;
import net.forthecrown.useables.actions.UsageActionInstance;
import net.forthecrown.useables.checks.UsageCheckInstance;
import net.forthecrown.utils.FtcUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public abstract class AbstractUsable extends AbstractJsonSerializer implements Usable {

    protected final List<UsageActionInstance> actions = new ArrayList<>();
    private final Object2ObjectMap<Key, UsageCheckInstance> checks = new Object2ObjectOpenHashMap<>();
    protected boolean sendFail;

    protected AbstractUsable(String fileName, String directory, boolean stopIfFileDoesntExist) {
        super(fileName, directory, stopIfFileDoesntExist);
    }

    protected void deleteFile(){
        super.delete();
    }

    public abstract void delete();

    protected void saveInto(JsonBuf json){
        json.add("sendFail", new JsonPrimitive(sendFail));

        JsonObject preconditions = new JsonObject();
        for (UsageCheckInstance p: getChecks()){
            preconditions.add(p.typeKey().asString(), InteractionUtils.writeCheck(p));
        }
        json.add("preconditions", preconditions);

        JsonArray array = new JsonArray();

        for (UsageActionInstance a: getActions()){
            JsonObject object = new JsonObject();
            object.add("type", new JsonPrimitive(a.typeKey().asString()));
            object.add("value", InteractionUtils.writeAction(a));

            array.add(object);
        }

        json.add("actions", array);
    }

    protected void reloadFrom(JsonBuf json) {
        sendFail = json.get("sendFail").getAsBoolean();

        checks.clear();
        JsonElement precons = json.get("preconditions");
        if(precons != null && precons.isJsonObject()){
            for (Map.Entry<String, JsonElement> e: precons.getAsJsonObject().entrySet()){
                try {
                    addCheck(InteractionUtils.readCheck(e.getKey(), e.getValue()));
                } catch (CommandSyntaxException exception) {
                    exception.printStackTrace();
                }
            }
        }

        actions.clear();
        JsonElement actionsElement = json.get("actions");
        if(actionsElement == null || !actionsElement.isJsonArray()) return;

        for (JsonElement e: actionsElement.getAsJsonArray()){
            JsonBuf j = JsonBuf.of(e.getAsJsonObject());

            try {
                addAction(InteractionUtils.readAction(j.getString("type"), j.get("value")));
            } catch (CommandSyntaxException exception) {
                exception.printStackTrace();
            }
        }
    }

    @Override
    public void interact(Player player) {
        if(!test(player)) return;
        actions.forEach(a -> a.onInteract(player));
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
    public List<UsageCheckInstance> getChecks() {
        return new ArrayList<>(checks.values());
    }

    @Override
    public void addCheck(UsageCheckInstance precondition) {
        checks.put(FtcUtils.checkNotBukkit(precondition.typeKey()), precondition);
    }

    @Override
    public void removeCheck(Key name) {
        checks.remove(FtcUtils.checkNotBukkit(name));
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
        for (UsageCheckInstance p: checks.values()){
            if(!p.test(player)){
                if(sendFail){
                    Component failMsg = p.personalizedMessage(player);
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
    public <T extends UsageActionInstance> T getAction(Key key, Class<T> clazz) {
        key = FtcUtils.checkNotBukkit(key);
        for (UsageActionInstance a: actions){
            if(!a.typeKey().equals(key)) continue;
            if(!clazz.isAssignableFrom(a.getClass())) continue;
            return (T) a;
        }

        return null;
    }

    @Override
    public <T extends UsageCheckInstance> T getCheck(Key key, Class<T> clazz) {
        key = FtcUtils.checkNotBukkit(key);
        if(!checks.containsKey(key)) return null;

        UsageCheckInstance c = checks.get(key);
        if(!clazz.isAssignableFrom(c.getClass())) return null;
        return (T) c;
    }
}
