package net.forthecrown.scripts;

import net.forthecrown.Loggers;
import net.forthecrown.text.Text;
import net.forthecrown.text.placeholder.PlaceholderContext;
import net.forthecrown.text.placeholder.Placeholders;
import net.forthecrown.text.placeholder.TextPlaceholder;
import net.forthecrown.utils.io.source.Source;
import net.forthecrown.utils.io.source.Sources;
import net.kyori.adventure.text.Component;

class ScriptPlaceholders {

  private static final String SCRIPT_PLACEHOLDER = "script";
  private static final String JS_PLACEHOLDER = "js";

  private static final TextPlaceholder SCRIPT = (match, render) -> {
    if (match.isEmpty()) {
      return null;
    }

    Script script = Scripts.fromScriptFile(match.trim());
    return compileAndExec(script, render);
  };

  private static final TextPlaceholder JS = (match, render) -> {
    if (match.isEmpty()) {
      return null;
    }

    Source source = Sources.direct(match, "placeholder");
    Script script = Scripts.newScript(source);

    return compileAndExec(script, render);
  };

  static void registerAll() {
    Placeholders.addDefault(SCRIPT_PLACEHOLDER, SCRIPT);
    Placeholders.addDefault(JS_PLACEHOLDER, JS);
  }

  static void removeAll() {
    Placeholders.removeDefault(SCRIPT_PLACEHOLDER);
    Placeholders.removeDefault(JS_PLACEHOLDER);
  }

  private static Component compileAndExec(Script script, PlaceholderContext ctx) {
    try {
      script.compile();
    } catch (ScriptLoadException exc) {
      Loggers.getLogger().error("Error compiling script {}", script, exc);
      return null;
    }

    script.put("viewer", ctx.viewer());
    script.put("renderer", ctx.renderer());

    ctx.context().forEach(script::put);

    ExecResult<Object> obj = script.evaluate().logError();

    if (!obj.isSuccess()) {
      return null;
    }

    return Text.valueOf(obj.result().orElse(null), ctx.viewer());
  }
}
