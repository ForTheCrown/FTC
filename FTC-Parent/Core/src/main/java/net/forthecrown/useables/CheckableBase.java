package net.forthecrown.useables;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.CrownCore;
import net.forthecrown.utils.CrownUtils;
import net.kyori.adventure.key.Key;

import java.util.*;

public abstract class CheckableBase implements Preconditionable {
    protected Map<Key, UsageCheck> checks = new HashMap<>();

    protected void saveChecksInto(JsonObject json){
        JsonObject preconditions = new JsonObject();
        for (UsageCheck p: getChecks()){
            preconditions.add(p.key().asString(), p.serialize());
        }
        json.add("preconditions", preconditions);
    }

    protected void reloadChecksFrom(JsonObject json) throws CommandSyntaxException {
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
    }

    @Override
    public List<UsageCheck> getChecks() {
        return new ArrayList<>(checks.values());
    }

    @Override
    public void addCheck(UsageCheck precondition) {
        checks.put(CrownUtils.checkNotBukkit(precondition.key()), precondition);
    }

    @Override
    public void removeCheck(Key name) {
        checks.remove(CrownUtils.checkNotBukkit(name));
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
    public <T extends UsageCheck> T getCheck(Key key, Class<T> clazz) {
        key = CrownUtils.checkNotBukkit(key);
        if(!checks.containsKey(key)) return null;

        UsageCheck c = checks.get(key);
        if(!clazz.isAssignableFrom(c.getClass())) return null;
        return (T) c;
    }
}
