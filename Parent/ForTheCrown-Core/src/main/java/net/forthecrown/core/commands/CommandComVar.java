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
    @Override
    protected void registerCommand(BrigadierCommand command) {
        command.then(argument(varArg, StringArgumentType.word())
                .suggests((c, b) -> suggestMatching(b, ComVars.getVariables()))

                .executes(c -> {
                    String name = c.getArgument("variable", String.class);
                    try {
                        c.getSource().base.sendMessage(new ChatComponentText(name + ": " + ComVars.getString(name)), null);
                    } catch (Exception e){
                        throw new CrownCommandException(e.getMessage());
                    }
                    return 0;
                })

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

    private static final String varArg = "variable";
    private static ComVarType<?> getComType(CommandContext<CommandListenerWrapper> c){
        return ComVars.getType(c.getArgument(varArg, String.class));
    }
}
