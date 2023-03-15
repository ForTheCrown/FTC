package net.forthecrown.core.script2;

import java.util.Objects;
import lombok.Getter;
import org.openjdk.nashorn.api.scripting.AbstractJSObject;

@Getter
class CallbackWrapper extends AbstractJSObject {
  private final JsCallback callback;
  private final Script script;

  public CallbackWrapper(Script script, JsCallback callback) {
    this.callback = Objects.requireNonNull(callback);
    this.script = script;
  }

  @Override
  public Object call(Object thiz, Object... args) {
    return callback.invoke(script, thiz, args);
  }

  @Override
  public boolean isFunction() {
    return true;
  }
}