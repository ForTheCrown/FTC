package net.forthecrown.core.script2;


@FunctionalInterface
public interface JsCallback {
  Object invoke(Script script, Object invoker, Object... args);
}