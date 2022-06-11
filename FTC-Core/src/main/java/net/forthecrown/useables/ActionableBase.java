package net.forthecrown.useables;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.useables.actions.UsageActionInstance;
import net.forthecrown.utils.FtcUtils;
import net.kyori.adventure.key.Key;

import java.util.List;

public abstract class ActionableBase extends CheckableBase implements Actionable, Checkable {

    protected final List<UsageActionInstance> actions = new ObjectArrayList<>();

    protected ActionableBase(){}

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
        key = FtcUtils.ensureBukkit(key);
        for (UsageActionInstance a: actions){
            if(!a.typeKey().equals(key)) continue;
            return (T) a;
        }

        return null;
    }
}