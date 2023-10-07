package net.forthecrown.scripts;

import org.mozilla.javascript.Scriptable;

public class ScriptObject implements Scriptable {

  private final Script script;

  public ScriptObject(Script script) {
    this.script = script;
  }

  @Override
  public String getClassName() {
    return script.getScriptObject().getClassName();
  }

  @Override
  public Object get(String name, Scriptable start) {
    return script.getScriptObject().get(name, start);
  }

  @Override
  public Object get(int index, Scriptable start) {
    return script.getScriptObject().get(index, start);
  }

  @Override
  public boolean has(String name, Scriptable start) {
    return script.getScriptObject().has(name, start);
  }

  @Override
  public boolean has(int index, Scriptable start) {
    return script.getScriptObject().has(index, start);
  }

  @Override
  public void put(String name, Scriptable start, Object value) {
    script.getScriptObject().put(name, start, value);
  }

  @Override
  public void put(int index, Scriptable start, Object value) {
    script.getScriptObject().put(index, start, value);
  }

  @Override
  public void delete(String name) {
    script.getScriptObject().delete(name);
  }

  @Override
  public void delete(int index) {
    script.getScriptObject().delete(index);
  }

  @Override
  public Scriptable getPrototype() {
    return script.getScriptObject().getPrototype();
  }

  @Override
  public void setPrototype(Scriptable prototype) {
    script.getScriptObject().setPrototype(prototype);
  }

  @Override
  public Scriptable getParentScope() {
    return script.getScriptObject().getParentScope();
  }

  @Override
  public void setParentScope(Scriptable parent) {
    script.getScriptObject().setParentScope(parent);
  }

  @Override
  public Object[] getIds() {
    return script.getScriptObject().getIds();
  }

  @Override
  public Object getDefaultValue(Class<?> hint) {
    return script.getScriptObject().getDefaultValue(hint);
  }

  @Override
  public boolean hasInstance(Scriptable instance) {
    return script.getScriptObject().hasInstance(instance);
  }
}
