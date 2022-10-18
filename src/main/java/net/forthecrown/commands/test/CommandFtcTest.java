package net.forthecrown.commands.test;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.text.Text;

public class CommandFtcTest extends FtcCommand {

    public CommandFtcTest() {
        super("FtcTest");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /FtcTest
     *
     * Permissions used:
     *
     * Main Author:
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(literal("test_permission")
                        .then(argument("permission", StringArgumentType.string())
                                .then(argument("user", Arguments.USER)
                                        .executes(c -> {
                                            var user = Arguments.getUser(c, "user");
                                            var perm = StringArgumentType.getString(c, "permission");

                                            c.getSource().sendMessage(
                                                    Text.format("{0, user} has permission '{1}': {2}",
                                                            user,
                                                            perm,
                                                            user.hasPermission(perm)
                                                    )
                                            );
                                            return 0;
                                        })
                                )
                        )
                );
    }
}