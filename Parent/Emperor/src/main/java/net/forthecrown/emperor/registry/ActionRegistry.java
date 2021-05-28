package net.forthecrown.emperor.registry;

import net.forthecrown.emperor.useables.UsageAction;
import net.kyori.adventure.key.Key;

import java.util.function.Supplier;

public interface ActionRegistry extends Registry<Supplier<UsageAction>> {
    default UsageAction getAction(Key key){
        return get(key).get();
    }
}
