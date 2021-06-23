package net.forthecrown.datafixers;

import java.util.logging.Level;

public interface DataFixer<T> {
    DataFixer<T> begin();

    T get(String fileName);

    void log(Level level, String info);

    boolean needsFix(T toCheck);
    void fix(T toFix);

    void complete();
}