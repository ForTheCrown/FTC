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
    if (args.length > 3) {
      throw ScriptRuntime.typeError("Too many arguments! (Given: " + args.length + ", max 3)");
    }
    if (args.length == 0) {
      return Context.getUndefinedValue();
    }

    Component baseText = ScriptUtils.toText(cx, scope, args, 0);
    CommandSource viewer = ScriptUtils.toSource(args, 1);

    PlaceholderRenderer renderer = Placeholders.newRenderer();

    if (args.length == 3) {
      Object placeholderTable = args[2];
      if (!(placeholderTable instanceof ScriptableObject object)) {
        throw ScriptRuntime.typeError("Expected third parameter to be JS object");
      }

      var ids = object.getIds();
      for (Object id : ids) {
        if (!(id instanceof String str)) {
          continue;
        }

        Object value = ScriptableObject.getProperty(object, str);
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
