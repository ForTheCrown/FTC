package net.forthecrown.useables;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.forthecrown.useables.preconditions.UsageCheckInstance;
import net.forthecrown.utils.FtcUtils;
import net.kyori.adventure.key.Key;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class CheckableBase implements Preconditionable {
    protected final Object2ObjectMap<Key, UsageCheckInstance> checks = new Object2ObjectOpenHashMap<>();

    protected void saveChecksInto(JsonObject json){
        JsonObject preconditions = new JsonObject();
        for (UsageCheckInstance p: getChecks()){
            preconditions.add(p.typeKey().asString(), InteractionUtils.writeCheck(p));
        }
        json.add("preconditions", preconditions);
    }

    protected void reloadChecksFrom(JsonObject json) throws CommandSyntaxException {
        checks.clear();
        JsonElement precons = json.get("preconditions");
        if(precons != null && precons.isJsonObject()){
            for (Map.Entry<String, JsonElement> e: precons.getAsJsonObject().entrySet()){
                addCheck(InteractionUtils.readCheck(e.getKey(), e.getValue()));
            }
        }
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
    public <T extends UsageCheckInstance> T getCheck(Key key, Class<T> clazz) {
        key = FtcUtils.checkNotBukkit(key);
        if(!checks.containsKey(key)) return null;

        UsageCheckInstance c = checks.get(key);
        if(!clazz.isAssignableFrom(c.getClass())) return null;
        return (T) c;
    }
}
