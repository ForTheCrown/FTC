package net.forthecrown.useables;

import net.forthecrown.utils.ListUtils;
import net.kyori.adventure.key.Key;

import java.util.List;
import java.util.Set;

public interface Preconditionable {
    List<UsageCheck> getChecks();

    void addCheck(UsageCheck precondition);
    void removeCheck(Key name);
    void clearChecks();

    Set<Key> getCheckTypes();

    default Set<String> getStringCheckTypes(){
        return ListUtils.convertToSet(getCheckTypes(), Key::asString);
    }

    <T extends UsageCheck> T getCheck(Key key, Class<T> clazz) throws ClassCastException;
}
