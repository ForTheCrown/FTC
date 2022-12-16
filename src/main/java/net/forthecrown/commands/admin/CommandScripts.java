package net.forthecrown.commands.admin;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.FTC;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.script2.Script;
import net.forthecrown.core.script2.ScriptLoadException;
import net.forthecrown.core.script2.ScriptManager;
import net.forthecrown.core.script2.ScriptResult;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.writer.TextWriters;
import org.jetbrains.annotations.Nullable;

import javax.script.ScriptException;
import java.nio.file.Path;

public class CommandScripts extends FtcCommand {
    public CommandScripts() {
        super("Scripts");

        setAliases("script");
        setPermission(Permissions.ADMIN);

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
    protected void createCommand(BrigadierCommand command) {
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
                                .executes(c -> run(c, null, true))

                                .then(literal("-doNotClose")
                                        .executes(c -> run(c, null, false))
                                )

                                .then(argument("func", StringArgumentType.string())
                                        .executes(c -> {
                                            String func = c.getArgument(
                                                    "func",
                                                    String.class
                                            );

                                            return run(c, func, true);
                                        })

                                        .then(literal("-doNotClose")
                                                .executes(c -> {
                                                    String func = c.getArgument(
                                                            "func",
                                                            String.class
                                                    );

                                                    return run(c, null, false);
                                                })
                                        )
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
                .resultOrPartial(FTC.getLogger()::error);

        c.getSource().sendAdmin(
                Text.format("Deleting script '{0}'", script)
        );
        return 0;
    }

    private int eval(CommandContext<CommandSource> c)
            throws CommandSyntaxException
    {
        String input = c.getArgument("input", String.class);

        try {
            var engine = ScriptManager.getInstance()
                    .createEngine("command-script", "");

            engine.eval(input);
        } catch (ScriptException exc) {
            throw Exceptions.format(
                    "Error running script: {0}",
                    exc.getMessage()
            );
        }

        c.getSource()
                .sendAdmin("Successfully ran script");

        return 0;
    }

    private int run(CommandContext<CommandSource> c,
                    @Nullable String method,
                    boolean closeAfter
    ) throws CommandSyntaxException {
        String scriptName = c.getArgument("script", String.class);
        Script script;

        try {
            script = Script.read(scriptName);
        } catch (ScriptLoadException exc) {
            exc.printStackTrace();

            throw Exceptions.format(
                    "Couldn't evaluate script '{0}', reason: {1}",
                    exc.getScript().getName(),
                    exc.getCause()
            );
        }

        Object result = script.getEvalResult();

        if (method != null) {
            if (!script.hasMethod(method)) {
                script.close();

                throw Exceptions.format(
                        "Script '{0}' does not have method '{1}'",
                        scriptName,
                        method
                );
            }

            ScriptResult invoke = script.invoke(method);

            if (invoke.error().isPresent()) {
                script.close();

                throw Exceptions.format(
                        "Couldn't run '{0}' in '{1}': {2}",
                        method,
                        scriptName,
                        invoke.error().get()
                );
            } else if (invoke.result().isPresent()) {
                result = invoke.result().get();
            } else {
                result = null;
            }
        }

        if (method != null) {
            c.getSource().sendAdmin(
                    Text.format("Successfully ran {0} in script {1}, result={2}",
                            method,
                            scriptName,
                            result
                    )
            );
        } else {
            c.getSource().sendAdmin(
                    Text.format("Successfully ran script {0}, result={1}",
                            scriptName,
                            result
                    )
            );
        }

        if (closeAfter) {
            script.close();
        }
        return 0;
    }
}