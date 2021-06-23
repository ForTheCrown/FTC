package net.forthecrown.core.registry;

import net.forthecrown.useables.UsageCheck;
import net.kyori.adventure.key.Key;

import java.util.function.Supplier;

public interface CheckRegistry extends Registry<Supplier<UsageCheck>> {
    default UsageCheck getCheck(Key key){
        return get(key).get();
    }
}
