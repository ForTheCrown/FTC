package net.forthecrown.scripts.builtin;

import lombok.RequiredArgsConstructor;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.scripts.ScriptUtils;
import net.kyori.adventure.text.Component;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;

@RequiredArgsConstructor
public class SendMessageFunction implements Callable {

  private final boolean actionbar;

  @Override
  public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
    if (args.length < 2) {
      throw ScriptRuntime.typeError("Less than 2 arguments provided");
    }

    CommandSource source = ScriptUtils.toSource(args, 0);
    assert source != null;

    for (int i = 1; i < args.length; i++) {
      Component text = ScriptUtils.toText(cx, scope, args, i);
      assert text != null;

      if (actionbar) {
        source.sendActionBar(text);
      } else {
        source.sendMessage(text);
      }
    }

    return Context.getUndefinedValue();
  }
}
