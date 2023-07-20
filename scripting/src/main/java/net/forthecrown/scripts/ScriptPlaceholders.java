package net.forthecrown.scripts;

import net.forthecrown.text.Text;
import net.forthecrown.text.placeholder.PlaceholderList;
import net.forthecrown.text.placeholder.TextPlaceholder;
import net.forthecrown.utils.io.source.Source;
import net.forthecrown.utils.io.source.Sources;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;

class ScriptPlaceholders {

  private static final String SCRIPT_PLACEHOLDER = "script";
  private static final String JS_PLACEHOLDER = "js";

  private static final TextPlaceholder SCRIPT = (match, viewer) -> {
    if (match.isEmpty()) {
      return null;
    }

    Script script = Scripts.fromScriptFile(match);
    return compileAndExec(script, viewer);
  };

  private static final TextPlaceholder JS = (match, viewer) -> {
    if (match.isEmpty()) {
      return null;
    }

    Source source = Sources.direct(match, "placeholder");
    Script script = Scripts.newScript(source);

    return compileAndExec(script, viewer);
  };

  static void registerAll() {
    PlaceholderList.addDefault(SCRIPT_PLACEHOLDER, SCRIPT);
    PlaceholderList.addDefault(JS_PLACEHOLDER, JS);
  }

  static void removeAll() {
    PlaceholderList.removeDefault(SCRIPT_PLACEHOLDER);
    PlaceholderList.removeDefault(JS_PLACEHOLDER);
  }

  private static Component compileAndExec(Script script, Audience viewer) {
    script.compile();
    script.put("viewer", viewer);

    ExecResult<Object> obj = script.evaluate();

    if (!obj.isSuccess()) {
      return null;
    }

    return Text.valueOf(obj.result().orElse(null), viewer);
  }
}
