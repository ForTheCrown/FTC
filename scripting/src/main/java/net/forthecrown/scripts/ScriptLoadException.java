package net.forthecrown.scripts;

public class ScriptLoadException extends RuntimeException {

  public ScriptLoadException() {
  }

  public ScriptLoadException(String message) {
    super(message);
  }

  public ScriptLoadException(String message, Throwable cause) {
    super(message, cause);
  }

  public ScriptLoadException(Throwable cause) {
    super(cause);
  }
}