package net.forthecrown.commands.click;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class CommandClickableText extends FtcCommand {
    public static final String NAME = "clickable_text";

    public CommandClickableText() {
        super(NAME);

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
                .then(argument("args", StringArgumentType.greedyString())
                        .executes(c -> {
                            String args = c.getArgument("args", String.class);
                            ClickableTexts.execute(getUserSender(c), args);

                            return 0;
                        })
                );
    }
}