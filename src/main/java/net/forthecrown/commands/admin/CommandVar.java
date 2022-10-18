package net.forthecrown.commands.admin;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.vars.VarData;
import net.forthecrown.vars.types.VarType;
import net.kyori.adventure.text.Component;

public class CommandVar extends FtcCommand {

    public CommandVar(){
        super("var");

        setPermission(Permissions.ADMIN);
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Provides an easy way for plugins to have a
     * variable that can be viewed and modified ingame
     *
     * Valid usages of command:
     * - /var <variable
     * - /var <variable> <value>
     *
     * Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.then(argument("var", Arguments.COM_VAR)
                .executes(c -> { //Just var name -> show var value
                    VarData var = c.getArgument("var", VarData.class);

                    Component display = Component.text(var.getName())
                            .append(Component.text(": "))
                            .append(var);

                    c.getSource().sendMessage(display);
                    return 0;
                })

                //value stated -> attempt parsing
                .then(argument("value", StringArgumentType.greedyString())
                        .suggests((c, b) -> {
                            VarData<?> var = c.getArgument("var", VarData.class);
                            return var.getType()
                                    .getArgumentType()
                                    .listSuggestions(c, b);
                        })

                        .executes(c -> {
                            VarData var = c.getArgument("var", VarData.class);
                            VarType type = var.getType();

                            String toParse = c.getArgument("value", String.class);
                            StringReader reader = new StringReader(toParse);

                            Object o = type.getArgumentType().parse(reader);

                            if (reader.canRead()) {
                                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
                                        .dispatcherExpectedArgumentSeparator()
                                        .createWithContext(reader);
                            }

                            var.update(o);

                            c.getSource().sendAdmin(
                                    Component.text("Set value of " + var.getName() + " to ")
                                            .append(var)
                            );
                            return 0;
                        })
                )
        );
    }
}