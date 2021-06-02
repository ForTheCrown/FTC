package net.forthecrown.emperor.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.manager.CrownCommandBuilder;
import net.forthecrown.emperor.commands.arguments.ComVarArgument;
import net.forthecrown.emperor.comvars.ComVar;
import net.forthecrown.emperor.comvars.ComVars;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class CommandComVar extends CrownCommandBuilder {

    public CommandComVar(){
        super("comvar", CrownCore.inst());

        setPermission(Permissions.CORE_ADMIN);
        setAliases("convar", "consolevar", "commandvar", "commandvariables");
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
     * Author: Botul
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.then(argument("var", ComVarArgument.comVar())
                .executes(c -> { //Just var name -> show var value
                    ComVar<?> var = c.getArgument("var", ComVar.class);

                    c.getSource().sendMessage(var.getName() + ": " + var.toString());
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
                            ComVar<?> var = c.getArgument("var", ComVar.class);
                            String toParse = c.getArgument("value", String.class);

                            ComVars.parseVar(var.getName(), toParse);
                            broadcastAdmin(c.getSource(), "Set " + var.getName() + " to " + var.toString());
                            return 0;
                        })
                )
        );
    }
}