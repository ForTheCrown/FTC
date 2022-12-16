package net.forthecrown.core.script2;

import lombok.Getter;

@Getter
public class ScriptLoadException extends RuntimeException {
    private final Script script;

    public ScriptLoadException(Script script, Throwable cause) {
        super(
                String.format(
                        "Couldn't load script '%s' reason: %s",
                        script.getName(),
                        cause.getMessage()
                ),
                cause
        );
        this.script = script;
    }
}