package net.forthecrown.registry;

import net.kyori.adventure.key.Key;
import org.apache.commons.lang.Validate;

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
        validateOpen();
        return super.register(key, raw);
    }

    @Override
    public void remove(Key key) {
        validateOpen();
        super.remove(key);
    }

    @Override
    public void clear() {
        validateOpen();
        super.clear();
    }

    private void validateOpen(){ Validate.isTrue(open, "Registry is currently closed"); }
}
