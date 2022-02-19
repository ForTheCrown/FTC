package net.forthecrown.registry;

import net.kyori.adventure.key.Key;
import org.apache.commons.lang.Validate;
import org.bukkit.NamespacedKey;

public class CloseableRegistryImpl<T> extends RegistryImpl<T> implements CloseableRegistry<T> {

    private boolean open = true;

    public CloseableRegistryImpl(NamespacedKey key) {
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
    public T register(Key key, T value) {
        validateOpen();
        return super.register(key, value);
    }

    @Override
    public T remove(Key key) {
        validateOpen();
        return super.remove(key);
    }

    @Override
    public void clear() {
        validateOpen();
        super.clear();
    }

    private void validateOpen(){ Validate.isTrue(open, "Registry is closed"); }
}
