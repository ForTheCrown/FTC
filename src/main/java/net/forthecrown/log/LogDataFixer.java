package net.forthecrown.log;

import com.mojang.serialization.Dynamic;

public interface LogDataFixer {
    <S> Dynamic<S> update(Dynamic<S> dynamic);

    static LogDataFixer rename(String element, String newName) {
        return new LogDataFixer() {
            @Override
            public <S> Dynamic<S> update(Dynamic<S> dynamic) {
                var gotten = dynamic.get(element);

                if (gotten.result().isEmpty()) {
                    return dynamic;
                }

                var result = gotten.result().get();
                return dynamic.set(newName, result);
            }
        };
    }
}