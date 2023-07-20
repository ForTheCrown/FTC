package net.forthecrown.scripts;

import static org.mozilla.javascript.Context.VERSION_ES6;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.Loggers;
import net.forthecrown.utils.io.source.Source;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeJavaClass;
import org.mozilla.javascript.NativeJavaMethod;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.slf4j.Logger;

@Accessors(chain = true)
class RhinoScript implements Script {

  public static final String[] EMPTY_STRING_ARRAY = {};
  public static final Object[] EMPTY_OBJECT_ARRAY = {};

  @Getter
  private final Source source;

  @Getter
  private final Logger logger;

  @Getter
  private String[] arguments = EMPTY_STRING_ARRAY;

  NativeObject scriptObject;
  private org.mozilla.javascript.Script compiled;

  @Getter @Setter
  private Script parent;
  private final List<Script> children = new ObjectArrayList<>();

  private boolean useClassGen;

  @Getter @Setter
  private Path workingDirectory;

  private Map<String, ScriptExtension> extensions;

  private final List<Class> imported = new ArrayList<>();

  public RhinoScript(Source source) {
    this.source = Objects.requireNonNull(source, "Null source");
    this.logger = Loggers.getLogger(getName());
  }

  @Override
  public Script setArguments(String... arguments) {
    if (arguments == null || arguments.length < 1) {
      this.arguments = EMPTY_STRING_ARRAY;
    } else {
      this.arguments = arguments.clone();
    }

    return this;
  }

  @Override
  public boolean useClassGen() {
    return useClassGen;
  }

  @Override
  public Script setClassGen(boolean classGen) {
    this.useClassGen = classGen;
    return this;
  }

  @Override
  public boolean isCompiled() {
    return compiled != null;
  }

  private void ensureCompiled() throws IllegalStateException {
    Preconditions.checkState(
        compiled != null,
        "Script '%s' is not compiled", getName()
    );
  }

  @Override
  public Object get(@NotNull String bindingName) throws IllegalStateException {
    ensureCompiled();
    Object value = scriptObject.get(bindingName, scriptObject);

    if (value == Context.getUndefinedValue() || value == null) {
      return null;
    }

    if (value == Scriptable.NOT_FOUND) {
      return NOT_FOUND;
    }

    return toJava(value);
  }

  @Override
  public boolean hasMethod(String methodName) throws IllegalStateException {
    return get(methodName) instanceof Function;
  }

  @Override
  public Script put(@NotNull String bindingName, @Nullable Object binding)
      throws IllegalStateException
  {
    ensureCompiled();
    Object wrapped = toScriptObject(bindingName, binding);
    scriptObject.put(bindingName, scriptObject, wrapped);

    if (binding instanceof RhinoScript script) {
      addChild(script);
    }

    return this;
  }

  @Override
  public Script putConst(@NotNull String bindingName, @Nullable Object binding)
      throws IllegalStateException
  {
    ensureCompiled();
    Object wrapped = toScriptObject(bindingName, binding);
    scriptObject.putConst(bindingName, scriptObject, wrapped);

    if (binding instanceof RhinoScript script) {
      addChild(script);
    }

    return this;
  }

  Object toScriptObject(String label, Object o) {
    if (o instanceof Class<?> clazz) {
      return new NativeJavaClass(scriptObject, clazz);
    }

    if (o instanceof ScriptCallback callback) {
      return new CallbackWrapper(callback, this);
    }

    if (o instanceof Method method) {
      return new NativeJavaMethod(method, label);
    }

    if (o instanceof RhinoScript other) {
      other.ensureCompiled();
      return other.scriptObject;
    }

    return o;
  }

  @Override
  public Object remove(@NotNull String bindingName) throws IllegalStateException {
    ensureCompiled();
    Object existing = get(bindingName);
    scriptObject.delete(bindingName);

    if (existing instanceof Script script) {
      removeChild(script);
    }

    return existing;
  }

  @Override
  public Script importClass(@NotNull Class<?> clazz) {
    imported.add(clazz);

    if (compiled != null) {
      runtimeImport(clazz);
    }

    return this;
  }

  void runtimeImport(Class<?> importedClass) {
    runtimeImport(importedClass.getSimpleName(), importedClass);
  }

  void runtimeImport(String name, Class<?> importedClass) {
    NativeJavaClass njc = new NativeJavaClass(scriptObject, importedClass);
    scriptObject.put(name, scriptObject, njc);
  }

  private Context enterContext() {
    Context ctx = Context.enter();
    ctx.setLanguageVersion(VERSION_ES6);
    ctx.setOptimizationLevel(useClassGen ? 9 : -1);
    ctx.setApplicationClassLoader(getClass().getClassLoader());
    return ctx;
  }

  @Override
  public Script compile() {
    if (compiled != null) {
      close();
    }

    StringBuffer buf;

    try {
      buf = source.read();
    } catch (IOException exc) {
      throw new ScriptLoadException(exc);
    }

    PreProcessor processor = new PreProcessor(buf);
    String str = processor.run();

    try (Context ctx = enterContext()) {
      compiled = ctx.compileString(str, getName(), 1, null);
      scriptObject = new NativeObject();
      ctx.initStandardObjects(scriptObject);

      put("args", arguments);
      put("logger", logger);
      put("_script", this);

      if (extensions != null) {
        extensions.forEach((s, extension) -> {
          extension.onScriptCompile();
          put(s, extension);
        });
      }

      if (!imported.isEmpty()) {
        imported.forEach(this::runtimeImport);
      }

      processor.runCallbacks(this);
    } catch (Throwable t) {
      var message = getMessage(t);
      throw new ScriptLoadException(message, t);
    }

    return this;
  }

  @Override
  public ExecResult<Object> evaluate() {
    if (compiled == null) {
      return ExecResultImpl.error("Script not compiled", this);
    }

    try (Context ctx = enterContext()) {
      try {
        Object o = compiled.exec(ctx, scriptObject);
        return ExecResultImpl.success(filterUndefined(o), this);
      } catch (Throwable t) {
        return ExecResultImpl.wrap(t, this);
      }
    }
  }

  @Override
  public ExecResult<Object> invoke(String methodName, Object... arguments) {
    Objects.requireNonNull(methodName);

    if (compiled == null) {
      return ExecResultImpl.error("Script not compiled", this);
    }

    Object value = ScriptableObject.getProperty(scriptObject, methodName);

    if (value == Scriptable.NOT_FOUND) {
      return ExecResultImpl.error("No such method found", methodName, this);
    }

    if (!(value instanceof Callable callable)) {
      return ExecResultImpl.error("Value is not a callable", methodName, this);
    }

    try (Context ctx = enterContext()) {
      Object[] args;

      if (arguments.length > 0) {
        args = new Object[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
          args[i] = Context.javaToJS(arguments[i], scriptObject, ctx);
        }
      } else {
        args = EMPTY_OBJECT_ARRAY;
      }

      try {
        Object returnValue = callable.call(ctx, scriptObject, scriptObject, args);
        return ExecResultImpl.success(filterUndefined(returnValue), methodName, this);
      } catch (Throwable exc) {
        return ExecResultImpl.wrap(exc, methodName, this);
      }
    }
  }

  static Object filterUndefined(Object o) {
    if (o instanceof Undefined) {
      return null;
    }

    return o;
  }

  static String getMessage(Throwable throwable) {
    String message = throwable.getMessage();

    while (message == null && throwable.getCause() != null) {
      var thr = throwable.getCause();
      message = thr.getMessage();
    }

    return Objects.requireNonNullElse(message, throwable.toString());
  }

  @Override
  public ImmutableList<Script> getChildren() {
    if (children.isEmpty()) {
      return ImmutableList.of();
    } else {
      return ImmutableList.copyOf(children);
    }
  }

  @Override
  public Script addChild(Script child) {
    if (children.add(child)) {
      ((RhinoScript) child).setParent(this);
    }

    return this;
  }

  @Override
  public Script removeChild(Script child) {
    if (children.remove(child)) {
      ((RhinoScript) child).setParent(null);
    }

    return this;
  }

  @Override
  public ImmutableMap<String, ScriptExtension> getExtensions() {
    if (extensions == null || extensions.isEmpty()) {
      return ImmutableMap.of();
    }

    return ImmutableMap.copyOf(extensions);
  }

  @Override
  public boolean addExtension(String name, ScriptExtension extension) {
    Objects.requireNonNull(name, "Null extension name");
    Objects.requireNonNull(extension, "Null extension");

    if (extensions == null) {
      extensions = new Object2ObjectOpenHashMap<>();
    } else if (extensions.containsKey(name)) {
      return false;
    }

    extensions.put(name, extension);
    extension.setScript(this);

    if (isCompiled()) {
      extension.onScriptCompile();
      put(name, extension);
    }

    return true;
  }

  @Override
  public ScriptExtension removeExtension(String name) {
    Objects.requireNonNull(name, "Null extension name");

    if (extensions == null || extensions.isEmpty()) {
      return null;
    }

    var ext = extensions.remove(name);

    if (ext != null) {
      ext.setScript(null);

      if (compiled != null) {
        remove(name);
      }
    }

    return ext;
  }

  @Override
  public void close() {
    if (compiled == null) {
      return;
    }

    if (parent != null) {
      parent.removeChild(this);
    }

    children.forEach(Script::close);

    compiled = null;
    scriptObject = null;

    if (extensions != null) {
      extensions.values().forEach(ScriptExtension::onScriptClose);
    }
  }

  static Object toJava(Object jsObject) {
    return Context.jsToJava(jsObject, Object.class);
  }
}