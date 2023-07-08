package net.forthecrown.scripts.commands;

import com.google.common.base.Strings;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Map;
import java.util.Objects;
import net.forthecrown.command.Exceptions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.annotations.Argument;
import net.forthecrown.grenadier.annotations.CommandData;
import net.forthecrown.grenadier.annotations.VariableInitializer;
import net.forthecrown.grenadier.types.options.ArgumentOption;
import net.forthecrown.grenadier.types.options.FlagOption;
import net.forthecrown.grenadier.types.options.Options;
import net.forthecrown.grenadier.types.options.OptionsArgument;
import net.forthecrown.grenadier.types.options.ParsedOptions;
import net.forthecrown.scripts.ExecResult;
import net.forthecrown.scripts.Script;
import net.forthecrown.scripts.ScriptLoadException;
import net.forthecrown.scripts.ScriptService;
import net.forthecrown.scripts.Scripts;
import net.forthecrown.text.Text;
import net.forthecrown.utils.io.source.Source;
import net.forthecrown.utils.io.source.Sources;

@CommandData("file = scripts.gcn")
public class ScriptingCommand {

  public static final FlagOption KEEP_OPEN = Options.flag("keep-open");

  public static final ArgumentOption<String[]> SCRIPT_ARGS
      = Options.argument(ScriptArgsArgument.SCRIPT_ARGS)
      .setLabels("args")
      .setDefaultValue(new String[0])
      .build();

  public static final ArgumentOption<String> METHOD
      = Options.argument(StringArgumentType.string())
      .setLabels("method")
      .build();

  public static final OptionsArgument OPTIONS = OptionsArgument.builder()
      .addFlag(KEEP_OPEN)
      .addOptional(SCRIPT_ARGS)
      .addOptional(METHOD)
      .build();

  @VariableInitializer
  void initVars(Map<String, Object> vars) {
    vars.put("script_argument", ScriptArgument.SCRIPT);
    vars.put("run_options", OPTIONS);
  }

  void evaluate(CommandSource source, @Argument("js_code") String code)
      throws CommandSyntaxException
  {
    Source scriptSource = Sources.direct(code, "<command script>");
    executeScript(source, scriptSource, false, null);
  }

  void runScript(
      CommandSource source,
      @Argument("script_name") Source script,
      @Argument(value = "options", optional = true) ParsedOptions options
  ) throws CommandSyntaxException {
    options = Objects.requireNonNullElse(options, ParsedOptions.EMPTY);
    options.checkAccess(source);

    boolean keepOpen = options.has(KEEP_OPEN);
    String[] args = options.getValue(SCRIPT_ARGS);
    String method = options.getValue(METHOD);

    executeScript(source, script, keepOpen, method, args);
  }

  private void executeScript(
      CommandSource source,
      Source scriptSource,
      boolean keepOpen,
      String method,
      String... args
  ) throws CommandSyntaxException {
    ScriptService service = Scripts.getService();
    Script script = service.newScript(scriptSource);
    script.setArguments(args);

    try {
      script.compile();
    } catch (ScriptLoadException exc) {
      throw Exceptions.format("Couldn't compile script: {0}", exc.getMessage());
    }

    script.put("_source", source);

    ExecResult<Object> result = script.evaluate().logError();

    if (result.error().isPresent()) {
      script.close();
      result.logError();

      throw Exceptions.create(result.error().get());
    }

    if (!Strings.isNullOrEmpty(method)) {
      result = script.invoke(method);

      if (result.error().isPresent()) {
        result.logError();
        script.close();

        throw Exceptions.create(result.error().get());
      }
    }

    result.result().ifPresentOrElse(o -> {
      source.sendMessage(Text.format("Script execution finished, result={0}", o));
    }, () -> {
      source.sendMessage(Text.format("Script execution finished"));
    });

    if (!keepOpen) {
      script.close();
    } else {
      service.addActiveScript(script);
    }
  }
}