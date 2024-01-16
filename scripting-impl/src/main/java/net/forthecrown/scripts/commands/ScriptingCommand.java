package net.forthecrown.scripts.commands;

import com.google.common.base.Strings;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Map;
import java.util.Objects;
import net.forthecrown.Loggers;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.arguments.RegistryArguments;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.annotations.Argument;
import net.forthecrown.grenadier.annotations.CommandData;
import net.forthecrown.grenadier.annotations.VariableInitializer;
import net.forthecrown.grenadier.types.options.ArgumentOption;
import net.forthecrown.grenadier.types.options.FlagOption;
import net.forthecrown.grenadier.types.options.Options;
import net.forthecrown.grenadier.types.options.OptionsArgument;
import net.forthecrown.grenadier.types.options.ParsedOptions;
import net.forthecrown.registry.Holder;
import net.forthecrown.scripts.CachingScriptLoader;
import net.forthecrown.scripts.ExecResult;
import net.forthecrown.scripts.Script;
import net.forthecrown.scripts.ScriptLoadException;
import net.forthecrown.scripts.ScriptService;
import net.forthecrown.scripts.ScriptingPlugin;
import net.forthecrown.scripts.Scripts;
import net.forthecrown.scripts.pack.ScriptPack;
import net.forthecrown.text.Text;
import net.forthecrown.utils.io.source.Source;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;

@CommandData("file = scripts.gcn")
public class ScriptingCommand {

  public static final FlagOption KEEP_OPEN = Options.flag("keep-open");

  public static final ArgumentOption<String[]> SCRIPT_ARGS
      = Options.argument(ScriptArgsArgument.SCRIPT_ARGS)
      .setLabel("args")
      .setDefaultValue(new String[0])
      .build();

  public static final ArgumentOption<String> METHOD
      = Options.argument(StringArgumentType.string())
      .setLabel("method")
      .build();

  public static final OptionsArgument OPTIONS = OptionsArgument.builder()
      .addFlag(KEEP_OPEN)
      .addOptional(SCRIPT_ARGS)
      .addOptional(METHOD)
      .build();

  private final ScriptingPlugin plugin;

  public ScriptingCommand(ScriptingPlugin plugin) {
    this.plugin = plugin;
  }

  @VariableInitializer
  void initVars(Map<String, Object> vars) {
    vars.put("script_argument", ScriptArgument.SCRIPT);
    vars.put("run_options", OPTIONS);

    var packs = plugin.getPacks();
    vars.put("active_script", new RegistryArguments<>(packs.getPacks(), "Loaded Script"));
  }

  void configReload(CommandSource source) {
    plugin.reloadConfig();
    source.sendSuccess(Component.text("Reloaded scripting config"));
  }

  void scriptsReload(CommandSource source) {
    plugin.getPacks().reload();
    source.sendSuccess(Component.text("Reloaded scripting config"));
  }

  void listActive(CommandSource source) {

  }

  void reloadActive(CommandSource source) {

  }

  void closeActive(CommandSource source, @Argument("active") Holder<ScriptPack> holder) {
    holder.getRegistry().remove(holder.getKey());
    holder.getValue().close();

    source.sendSuccess(
        Text.format("Removed script pack '&e{0}&r'", NamedTextColor.GRAY, holder.getKey())
    );
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

  static void executeScript(
      CommandSource source,
      Source scriptSource,
      boolean keepOpen,
      String method,
      String... args
  ) throws CommandSyntaxException {
    ScriptService service = Scripts.getService();
    CachingScriptLoader loader = service.getGlobalLoader();

    Script script = service.newScript(loader, scriptSource);
    script.setArguments(args);

    try {
      script.compile();
    } catch (ScriptLoadException exc) {
      throw Exceptions.format("Couldn't compile script: {0}", exc.getMessage());
    }

    script.put("source", source);

    ExecResult<Object> result = script.evaluate().logError();

    if (result.error().isPresent()) {
      script.close();
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
      source.sendSuccess(Text.format("Script execution finished: &f{0}",
          NamedTextColor.GRAY, toText(o, source)
      ));
    }, () -> {
      source.sendSuccess(Text.format("Script execution finished", NamedTextColor.GRAY));
    });

    if (!keepOpen) {
      script.close();
      loader.remove(scriptSource);
    }
  }

  static Component toText(Object o, Audience viewer) {
    try {
      if (o instanceof Scriptable object) {
        var obj = ScriptRuntime.toString(object);
        return Text.valueOf(obj, viewer);
      }
    } catch (RuntimeException exc) {
      Loggers.getLogger().error("Error getting string from script object", exc);
      return Component.text("[Failed to convert to text]");
    }

    return Text.valueOf(o, viewer);
  }
}