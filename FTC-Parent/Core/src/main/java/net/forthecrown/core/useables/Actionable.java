package net.forthecrown.core.useables;

import net.kyori.adventure.key.Key;

import java.util.List;

public interface Actionable {
    void addAction(UsageAction action);
    void removeAction(int index);

    List<UsageAction> getActions();

    void clearActions();

    <T extends UsageAction> T getAction(Key key, Class<T> clazz) throws ClassCastException;
}
