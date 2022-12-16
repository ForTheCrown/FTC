package net.forthecrown.core.script2;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.FTC;

import java.util.Optional;

@Builder(builderClassName = "Builder")
@RequiredArgsConstructor(staticName = "of")
public class ScriptResult {
    @Getter
    private final Script script;

    @Getter
    private final String method;

    private final Object result;

    private final Throwable exception;

    public Optional<Object> result() {
        return Optional.ofNullable(result);
    }

    public Optional<Throwable> error() {
        return Optional.ofNullable(exception);
    }

    public ScriptResult logIfError() {
        error().ifPresent(e -> {
            FTC.getLogger().error(
                    "Couldn't invoke method '{}' in '{}'",
                    method, script.getName(),
                    e
            );
        });

        return this;
    }

    public void close() {
        getScript().close();
    }

    public Optional<Boolean> asBoolean() {
        return result().flatMap(o -> {
            if (o instanceof Boolean b) {
                return Optional.of(b);
            }

            if (o instanceof Number number) {
                return Optional.of(
                        number.longValue() != 0
                );
            }

            if (o instanceof String str) {
                return Optional.of(
                        Boolean.parseBoolean(str)
                );
            }

            return Optional.empty();
        });
    }
}