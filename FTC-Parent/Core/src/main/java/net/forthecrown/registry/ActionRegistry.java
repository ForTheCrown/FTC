package net.forthecrown.registry;

import net.forthecrown.useables.UsageAction;
import net.kyori.adventure.key.Key;

import java.util.function.Supplier;

public interface ActionRegistry extends Registry<Supplier<UsageAction>> {
    default UsageAction getAction(Key key){
        return get(key).get();
    }
}
