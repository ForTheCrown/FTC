package net.forthecrown.registry;

public interface CloseableRegistry<T> extends Registry<T> {
    void close();

    boolean isOpen();
}
