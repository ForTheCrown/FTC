package net.forthecrown.registry;

import net.kyori.adventure.key.Key;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.Validate;

public class CloseableRegistryBase<T> extends BaseRegistry<T> implements CloseableRegistry<T> {

    private boolean open = true;

    public CloseableRegistryBase(Key key) {
        super(key);
    }

    @Override
    public void close(){
        open = false;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public T register(Key key, T raw) {
        Validate.isTrue(open, "Registry is no longer accepting registrations.");
        return super.register(key, raw);
    }

    @Override
    public void remove(Key key) {
        Validate.isTrue(open, "Registry is currently closed");
        super.remove(key);
    }

    @Override
    public void clear() {
        Validate.isTrue(open, "Registry is currently closed");
        super.clear();
    }
}
