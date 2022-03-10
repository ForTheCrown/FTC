package net.forthecrown.commands.admin;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import net.forthecrown.commands.arguments.GlobalVarArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.vars.Var;
import net.forthecrown.vars.types.VarType;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;

public class CommandVar extends FtcCommand {

    public CommandVar(){
        super("var", Crown.inst());

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
     * - /comvar <variable
     * - /comvar <variable> <value>
     *
     * Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.then(argument("var", GlobalVarArgument.comVar())
                .executes(c -> { //Just var name -> show var value
                    Var var = c.getArgument("var", Var.class);

                    Component display = Component.text(var.getName())
                            .append(Component.text(": "))
                            .append(var);

                    c.getSource().sendMessage(display);
                    return 0;
                })

                //value stated -> attempt parsing
                .then(argument("value", StringArgumentType.greedyString())
                        .suggests((c, b) -> {
                            try {
                                Var<?> var = GlobalVarArgument.getComVar(c, "var");
                                return var.getType().getSuggestions(c, b);
                            } catch (Exception e) { return Suggestions.empty(); }
                        })

                        .executes(c -> {
                            Var var = c.getArgument("var", Var.class);
                            VarType type = var.getType();

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
                                            .append(var)
                            );
                            return 0;
                        })
                )
        );
    }
}
