package net.forthecrown.useables;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.forthecrown.useables.checks.UsageCheckInstance;
import net.forthecrown.utils.FtcUtils;
import net.kyori.adventure.key.Key;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class CheckableBase implements Checkable {
    protected final Object2ObjectMap<Key, UsageCheckInstance> checks = new Object2ObjectOpenHashMap<>();

    @Override
    public List<UsageCheckInstance> getChecks() {
        return new ArrayList<>(checks.values());
    }

    @Override
    public void addCheck(UsageCheckInstance precondition) {
        checks.put(FtcUtils.ensureBukkit(precondition.typeKey()), precondition);
    }

    @Override
    public void removeCheck(Key name) {
        checks.remove(FtcUtils.ensureBukkit(name));
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
        key = FtcUtils.ensureBukkit(key);
        if(!checks.containsKey(key)) return null;

        UsageCheckInstance c = checks.get(key);
        if(!clazz.isAssignableFrom(c.getClass())) return null;
        return (T) c;
    }
}
