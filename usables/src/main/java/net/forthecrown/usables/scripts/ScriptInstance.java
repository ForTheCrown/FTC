package net.forthecrown.usables.scripts;

import java.util.Arrays;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.scripts.ExecResults;
import net.forthecrown.scripts.Script;
import net.forthecrown.scripts.Scripts;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextJoiner;
import net.forthecrown.usables.Action;
import net.forthecrown.usables.Condition;
import net.forthecrown.usables.Interaction;
import net.forthecrown.usables.UsableComponent;
import net.forthecrown.usables.UsageType;
import net.forthecrown.utils.io.source.Source;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJSON;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.json.JsonParser;
import org.mozilla.javascript.json.JsonParser.ParseException;

@Getter
public class ScriptInstance implements Condition, Action {

  public static final UsageType<ScriptInstance> TYPE = new ScriptType();

  private final Source source;
  private final String[] args;

  @Setter
  private String dataString;

  public ScriptInstance(Source source, String... args) {
    this.source = source;
    this.args = args;
  }

  private Script compile(Interaction interaction) {
    Script script = Scripts.newScript(source);
    script.setArguments(args);
    script.compile();

    script.putConst("player", interaction.player());
    script.putConst("_holder", interaction.object());

    createScriptBindings(script, interaction.context());

    script.putConst("getDataObject", new GetData());
    script.putConst("setDataObject", new SetData());

    return script;
  }

  private void createScriptBindings(Script script, Map<String, Object> values) {
    values.forEach((string, o) -> {
      script.putValue("_" + string, () -> values.get(string), o1 -> values.put(string, o1));
    });
  }

  @Override
  public void onUse(Interaction interaction) {
    try (Script script = compile(interaction)) {
      script.evaluate()
          .flatMapScript(s -> s.invoke("onUse", interaction.user()))
          .logError();
    }
  }

  @Override
  public boolean test(Interaction interaction) {
    try (Script script = compile(interaction)) {
      var result = script.evaluate()
          .flatMapScript(s -> s.invoke("test", interaction.user()))
          .logError();

      if (!result.isSuccess()) {
        return false;
      }

      return ExecResults.toBoolean(result).result().orElse(false);
    }
  }

  @Override
  public Component failMessage(Interaction interaction) {
    try (Script script = compile(interaction)) {
      var result = script.evaluate()
          .flatMapScript(s -> {
            if (!s.hasMethod("getFailMessage")) {
              return null;
            }

            return s.invoke("getFailMessage", interaction.user());
          })
          .map(o -> Text.valueOf(o, interaction.player()))
          .logError();

      if (!result.isSuccess()) {
        return null;
      }

      return result.result().orElse(null);
    }
  }

  @Override
  public void afterTests(Interaction interaction) {
    try (Script script = compile(interaction)) {
      script.evaluate()
          .flatMapScript(s -> {
            if (!s.hasMethod("onTestsPassed")) {
              return null;
            }

            return s.invoke("onTestsPassed", interaction.user());
          })
          .logError();
    }
  }

  @Override
  public @Nullable Component displayInfo() {
    var builder = Component.text()
        .append(Component.text(source.name()));

    if (args.length > 0) {
      builder.append(Component.text(", args="))
          .append(
              TextJoiner.onComma()
                  .add(Arrays.stream(args).map(Component::text))
                  .setPrefix(Component.text("["))
                  .setSuffix(Component.text("]"))
                  .asComponent()
          );
    }

    return builder.build();
  }

  @Override
  public UsageType<? extends UsableComponent> getType() {
    return TYPE;
  }

  private class SetData implements Callable {

    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
      Object jsonData;

      if (args == null || args.length < 1 || args[0] == null) {
        setDataString(null);
        return Undefined.instance;
      } else if (args.length == 1) {
        jsonData = args[0];
      } else {
        jsonData = args;
      }

      String stringified = String.valueOf(NativeJSON.stringify(cx, scope, jsonData, null, null));
      setDataString(stringified);

      return Undefined.instance;
    }
  }

  private class GetData implements Callable {

    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
      String jsonData = getDataString();

      if (jsonData == null || jsonData.isEmpty()) {
        return Undefined.instance;
      }

      try {
        return new JsonParser(cx, scope).parseValue(jsonData);
      } catch (ParseException exc) {
        throw ScriptRuntime.constructError("SyntaxError", exc.getMessage());
      }
    }
  }
}
