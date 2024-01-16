package net.forthecrown.scripts.builtin;

import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.scripts.ScriptUtils;
import net.forthecrown.text.placeholder.PlaceholderContext;
import net.forthecrown.text.placeholder.PlaceholderRenderer;
import net.forthecrown.text.placeholder.Placeholders;
import net.forthecrown.text.placeholder.TextPlaceholder;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class RenderPlaceholdersFunction implements Callable {

  @Override
  public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
    if (args.length > 4) {
      throw ScriptRuntime.typeError("Too many arguments! (Given: " + args.length + ", max 4)");
    }
    if (args.length == 0) {
      return Context.getUndefinedValue();
    }

    Component baseText = ScriptUtils.toText(cx, scope, args, 0);
    CommandSource viewer = ScriptUtils.toSource(args, 1);
    PlaceholderRenderer renderer = toRenderer(args, 3);

    Scriptable placeholderTable = getPlaceholderTable(args, 2);

    if (placeholderTable != null) {
      Object[] ids = placeholderTable.getIds();

      for (Object id : ids) {
        if (!(id instanceof String str)) {
          continue;
        }

        Object value = ScriptableObject.getProperty(placeholderTable, str);
        TextPlaceholder placeholder;

        if (value instanceof Callable callable) {
          placeholder = new CallablePlaceholder(callable, cx, scope, thisObj);
        } else {
          placeholder = new ValuePlaceholder(value, cx, scope);
        }

        renderer.add(str, placeholder);
      }
    }

    Component rendered = renderer.render(baseText, viewer);
    return new NativeJavaObject(scope, rendered, Component.class);
  }

  Scriptable getPlaceholderTable(Object[] args, int index) {
    if (args.length <= index) {
      return null;
    }

    Object arg = args[index];

    if (arg instanceof NativeJavaObject) {
      return null;
    }

    if (!(arg instanceof Scriptable scriptableObject)) {
      return null;
    }

    return scriptableObject;
  }

  private PlaceholderRenderer toRenderer(Object[] args, int index) {
    if (args.length <= index) {
      return Placeholders.newRenderer();
    }

    Object value = args[index];
    Object jValue = Context.jsToJava(value, Object.class);

    if (jValue instanceof PlaceholderRenderer renderer) {
      return renderer;
    }
    if (jValue instanceof PlaceholderContext context) {
      return context.renderer();
    }

    return Placeholders.newRenderer();
  }

  record ValuePlaceholder(Object value, Context cx, Scriptable scope) implements TextPlaceholder {

    @Override
    public @Nullable Component render(String match, PlaceholderContext render) {
      return ScriptUtils.toText(cx, scope, value);
    }
  }

  record CallablePlaceholder(Callable callable, Context cx, Scriptable scope, Scriptable thisObj)
      implements TextPlaceholder
  {

    @Override
    public @Nullable Component render(String match, PlaceholderContext render) {
      Object[] args = new Object[2];
      args[0] = match;
      args[1] = new NativeJavaObject(scope, render, PlaceholderContext.class);

      Object returned = callable.call(cx, scope, thisObj, args);
      return ScriptUtils.toText(cx, scope, returned);
    }
  }
}
