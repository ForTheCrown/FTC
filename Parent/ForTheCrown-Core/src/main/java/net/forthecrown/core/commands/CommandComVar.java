package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.types.ComVarArgument;
import net.forthecrown.core.comvars.ComVar;
import net.forthecrown.core.comvars.ComVars;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class CommandComVar extends CrownCommandBuilder {

    public CommandComVar(){
        super("comvar", FtcCore.getInstance());

        setPermission("ftc.admin");
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
                                return var.getType().suggests(c, b);
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
