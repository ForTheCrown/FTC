package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.exceptions.CrownCommandException;
import net.forthecrown.core.comvars.ComVars;
import net.forthecrown.core.comvars.types.ComVarType;
import net.minecraft.server.v1_16_R3.ChatComponentText;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;

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
    protected void registerCommand(BrigadierCommand command) {
        command.then(argument(varArg, StringArgumentType.word())
                .suggests(suggest(ComVars.getVariables()))

                .executes(c -> { //Just var name -> show var value
                    String name = c.getArgument("variable", String.class);
                    try {
                        c.getSource().base.sendMessage(new ChatComponentText(name + ": " + ComVars.getString(name)), null);
                    } catch (Exception e){
                        throw new CrownCommandException(e.getMessage());
                    }
                    return 0;
                })

                //value stated -> attempt parsing
                .then(argument("value", StringArgumentType.greedyString())
                        .suggests((c, b) -> getComType(c).suggests(c, b))

                        .executes(c -> {
                            String name = c.getArgument("variable", String.class);
                            String toParse = c.getArgument("value", String.class);

                            ComVars.parseVar(name, toParse);
                            broadcastAdmin(c.getSource(), "Set " + name + " to " + ComVars.getString(name));
                            return 0;
                        })
                )
        );
    }

    //Used for result suggesting
    private static final String varArg = "variable";
    private static ComVarType<?> getComType(CommandContext<CommandListenerWrapper> c){
        return ComVars.getType(c.getArgument(varArg, String.class));
    }
}
