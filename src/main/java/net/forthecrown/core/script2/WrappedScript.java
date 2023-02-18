package net.forthecrown.core.script2;

import java.util.Collection;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Validate;
import org.openjdk.nashorn.api.scripting.JSObject;

@RequiredArgsConstructor
public class WrappedScript implements JSObject {

  @Getter
  private final Script script;

  public void ensureCompiled() {
    Validate.validState(script.isCompiled(), "Script not compiled");
  }

  @Override
  public Object call(Object thiz, Object... args) {
    ensureCompiled();

    if (args == null || args.length < 1) {
      var evalResult = script.eval().throwIfError();
      return evalResult.result().orElse(null);
    }

    return script.getMirror().call(thiz, args);
  }

  @Override
  public Object newObject(Object... args) {
    ensureCompiled();
    return script.getMirror().newObject(args);
  }

  @Override
  public Object eval(String s) {
    ensureCompiled();
    return script.getMirror().eval(s);
  }

  @Override
  public Object getMember(String name) {
    ensureCompiled();
    return script.getMirror().getMember(name);
  }

  @Override
  public Object getSlot(int index) {
    ensureCompiled();
    return script.getMirror().getSlot(index);
  }

  @Override
  public boolean hasMember(String name) {
    ensureCompiled();
    return script.getMirror().hasMember(name);
  }

  @Override
  public boolean hasSlot(int slot) {
    ensureCompiled();
    return script.getMirror().hasSlot(slot);
  }

  @Override
  public void removeMember(String name) {
    ensureCompiled();
    script.getMirror().removeMember(name);
  }

  @Override
  public void setMember(String name, Object value) {
    ensureCompiled();
    script.getMirror().setMember(name, value);
  }

  @Override
  public void setSlot(int index, Object value) {
    ensureCompiled();
    script.getMirror().setSlot(index, value);
  }

  @Override
  public Set<String> keySet() {
    ensureCompiled();
    return script.getMirror().keySet();
  }

  @Override
  public Collection<Object> values() {
    ensureCompiled();
    return script.getMirror().values();
  }

  @Override
  public boolean isInstance(Object instance) {
    ensureCompiled();
    return script.getMirror().isInstance(instance);
  }

  @Override
  public boolean isInstanceOf(Object clazz) {
    ensureCompiled();
    return script.getMirror().isInstanceOf(clazz);
  }

  @Override
  public String getClassName() {
    return getClass().getName();
  }

  @Override
  public boolean isFunction() {
    return true;
  }

  @Override
  public boolean isStrictFunction() {
    return false;
  }

  @Override
  public boolean isArray() {
    return false;
  }
}