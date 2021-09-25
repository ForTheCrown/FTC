package net.forthecrown.commands.click;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class CommandClickableText extends FtcCommand {

    public CommandClickableText() {
        super("clickable_text");

        setPermission(Permissions.DEFAULT);
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /clickable_text
     *
     * Permissions used:
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("firstID", IntegerArgumentType.integer())
                        .executes(cmd(false))

                        .then(argument("string", StringArgumentType.greedyString())
                                .executes(cmd(true))
                        )
                );
    }

    private Command<CommandSource> cmd(boolean args) {
        return c -> {
            ClickableTexts.execute(
                    getUserSender(c),
                    c.getArgument("firstID", Integer.class),
                    args ? c.getArgument("string", String.class) : null
            );

            return 0;
        };
    }
}