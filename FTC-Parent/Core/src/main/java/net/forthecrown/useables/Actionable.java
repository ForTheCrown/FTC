package net.forthecrown.useables;

import net.forthecrown.useables.actions.UsageActionInstance;
import net.kyori.adventure.key.Key;

import java.util.List;

public interface Actionable {
    void addAction(UsageActionInstance action);
    void removeAction(int index);

    List<UsageActionInstance> getActions();

    void clearActions();

    <T extends UsageActionInstance> T getAction(Key key, Class<T> clazz) throws ClassCastException;
}
