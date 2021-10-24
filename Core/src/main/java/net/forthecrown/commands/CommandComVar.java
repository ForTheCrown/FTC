package net.forthecrown.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import net.forthecrown.commands.arguments.ComVarArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.comvars.ComVar;
import net.forthecrown.comvars.types.ComVarType;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;

public class CommandComVar extends FtcCommand {

    public CommandComVar(){
        super("comvar", Crown.inst());

        setPermission(Permissions.FTC_ADMIN);
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
     * - /comvar <variable
     * - /comvar <variable> <value>
     *
     * Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.then(argument("var", ComVarArgument.comVar())
                .executes(c -> { //Just var name -> show var value
                    ComVar<?> var = c.getArgument("var", ComVar.class);

                    Component display = Component.text(var.getName())
                            .append(Component.text(": "))
                            .append(var.prettyDisplay());

                    c.getSource().sendMessage(display);
                    return 0;
                })

                //value stated -> attempt parsing
                .then(argument("value", StringArgumentType.greedyString())
                        .suggests((c, b) -> {
                            try {
                                ComVar<?> var = ComVarArgument.getComVar(c, "var");
                                return var.getType().getSuggestions(c, b);
                            } catch (Exception e) { return Suggestions.empty(); }
                        })

                        .executes(c -> {
                            ComVar var = c.getArgument("var", ComVar.class);
                            ComVarType type = var.getType();

                            String toParse = c.getArgument("value", String.class);
                            StringReader reader = new StringReader(toParse);

                            Object o = type.parse(reader);
                            if(reader.canRead()) {
                                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
                                        .dispatcherExpectedArgumentSeparator()
                                        .createWithContext(reader);
                            }

                            var.update(o);

                            c.getSource().sendAdmin(
                                    Component.text("Set value of " + var.getName() + " to ")
                                            .append(var.prettyDisplay())
                            );
                            return 0;
                        })
                )
        );
    }
}
