package net.forthecrown.commands.admin;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.economy.CommandShopHistory;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.core.script2.Script;
import net.forthecrown.core.script2.ScriptLoadException;
import net.forthecrown.core.script2.ScriptManager;
import net.forthecrown.core.script2.ScriptResult;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.options.ArgumentOption;
import net.forthecrown.grenadier.types.options.Options;
import net.forthecrown.grenadier.types.options.OptionsArgument;
import net.forthecrown.grenadier.types.options.ParsedOptions;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.writer.TextWriters;

public class CommandScripts extends FtcCommand {

  public static final ArgumentOption<List<String>> ARGS_ARRAY
      = Options.argument(ArgumentTypes.array(StringArgumentType.string()))
      .addLabel("args")
      .setDefaultValue(Collections.emptyList())
      .build();

  public static final ArgumentOption<String> METHOD_NAME
      = Options.argument(StringArgumentType.string())
      .addLabel("method")
      .build();

  public static final ArgumentOption<Boolean> CLOSE_AFTER
      = Options.argument(BoolArgumentType.bool())
      .addLabel("close_after")
      .setDefaultValue(true)
      .build();

  public static final OptionsArgument ARGS = OptionsArgument.builder()
      .addOptional(ARGS_ARRAY)
      .addOptional(METHOD_NAME)
      .addOptional(CLOSE_AFTER)
      .build();

  public CommandScripts() {
    super("Scripts");

    setAliases("script");
    setPermission(Permissions.ADMIN);
    setDescription("Command to use scripts");

    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   *
   * Valid usages of command:
   * /Scripts
   *
   * Permissions used:
   *
   * Main Author:
   */

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("eval <java script code>")
        .addInfo("Runs the given JavaScript code");

    factory.usage("run <script file> [args=<args array>] [close_after=<true | false>] [method=<name>]")
        .addInfo("Runs the given script file's global function")
        .addInfo("If the <args> argument is present, the set string array")
        .addInfo("is added into the script.")

        .addInfo("If <close_after> is set to 'true' or not set at all, the")
        .addInfo("script is closed after execution, else, it stays loaded")

        .addInfo("<method> specifies the name of the method to run after")
        .addInfo("the global scope has been executed");

    factory.usage("delete <script file>")
        .addInfo("Deletes a <script file>");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        // /script eval <JavaScript>
        .then(literal("eval")
            .then(argument("input", StringArgumentType.greedyString())
                .executes(this::eval)
            )
        )

        // /script run <script> [method]
        .then(literal("run")
            .then(argument("script", Arguments.SCRIPT)
                .executes(c -> run(c, CommandShopHistory.EMPTY))

                .then(argument("args", ARGS)
                    .executes(c -> run(c, c.getArgument("args", ParsedOptions.class)))
                )
            )
        )

        // /script list
        .then(literal("list")
            .executes(c -> {
              var writer = TextWriters.newWriter();
              var scripts = ScriptManager.getInstance()
                  .findExistingScripts();

              var it = scripts.listIterator();

              while (it.hasNext()) {
                var next = it.next();
                int index = it.nextIndex();

                writer.formattedLine(
                    "&7{0, number}) &r{1}",
                    index, next
                );
              }

              c.getSource().sendMessage(writer);
              return 0;
            })
        )

        // /script delete <script>
        .then(literal("delete")
            .then(argument("script", Arguments.SCRIPT)
                .executes(this::delete)
            )
        );
  }

  private int delete(CommandContext<CommandSource> c) {
    String script = c.getArgument("script", String.class);

    Path path = ScriptManager.getInstance()
        .getScriptFile(script);

    PathUtil.safeDelete(path)
        .resultOrPartial(Loggers.getLogger()::error);

    c.getSource().sendSuccess(
        Text.format("Deleting script '{0}'", script)
    );
    return 0;
  }

  private int eval(CommandContext<CommandSource> c)
      throws CommandSyntaxException
  {
    String input = c.getArgument("input", String.class);
    Script script = Script.ofCode(input);

    return runScript(c.getSource(), script, true, null);
  }

  private int run(CommandContext<CommandSource> c, ParsedOptions args
  ) throws CommandSyntaxException {
    String scriptName = c.getArgument("script", String.class);
    Script script = Script.of(scriptName);

    List<String> stringArgsList = args.getValue(ARGS_ARRAY);

    String[] stringArgs = stringArgsList.toArray(String[]::new);

    boolean closeAfter = args.getValue(CLOSE_AFTER);
    String method = args.getValue(METHOD_NAME);

    return runScript(c.getSource(), script, closeAfter, method, stringArgs);
  }

  private int runScript(CommandSource source,
                        Script script,
                        boolean closeAfter,
                        String method,
                        String... args
  ) throws CommandSyntaxException {
    var scriptName = script.getSource().getName();
    ScriptResult result;

    try {
      result = script.compile(args).eval();
    } catch (ScriptLoadException exc) {
      exc.printStackTrace();

      throw Exceptions.format(
          "Couldn't evaluate script '{0}', reason: {1}",
          script, exc.getCause()
      );
    }

    if (result.error().isPresent()) {
      var exc = result.error().get();

      throw Exceptions.format(
          "Couldn't evaluate script '{0}', reason: {1}",
          scriptName,
          exc.getCause()
      );
    }

    if (method != null) {
      if (!script.hasMethod(method)) {
        script.close();

        throw Exceptions.format(
            "Script '{0}' does not have method '{1}'",
            scriptName,
            method
        );
      }

      result = script.invoke(method);

      if (result.error().isPresent()) {
        script.close();

        throw Exceptions.format(
            "Couldn't run '{0}' in '{1}': {2}",
            method,
            scriptName,
            result.error().get()
        );
      }
    }

    if (method != null) {
      source.sendSuccess(
          Text.format("Successfully ran {0} in script {1}, result={2}",
              method,
              scriptName,
              result.result().orElse(null)
          )
      );
    } else {
      source.sendSuccess(
          Text.format("Successfully ran script {0}, result={1}",
              scriptName,
              result.result().orElse(null)
          )
      );
    }

    if (closeAfter) {
      script.close();
    }
    return 0;
  }
}