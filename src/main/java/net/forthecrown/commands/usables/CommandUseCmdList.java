package net.forthecrown.commands.usables;

import net.forthecrown.commands.arguments.UseCmdArgument;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.TextJoiner;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.useables.command.CommandUsable;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandUseCmdList<T extends CommandUsable> extends FtcCommand {
    private final UseCmdArgument<T> argument;

    public CommandUseCmdList(String name, UseCmdArgument<T> argument) {
        super(name + "s");

        setAliases(name + "list");
        this.argument = argument;

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /UseCmdList
     *
     * Permissions used:
     *
     * Main Author:
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    var user = getUserSender(c);
                    var list = argument.getManager().getUsable(user.getPlayer());

                    if (list.isEmpty()) {
                        throw Exceptions.NOTHING_TO_LIST;
                    }

                    user.sendMessage(
                            Text.format("{0, class}s: &e{1}",
                                    NamedTextColor.GRAY,

                                    argument.getTypeClass(),
                                    TextJoiner.onComma()
                                            .add(list.stream().map(CommandUsable::displayName))
                            )
                    );
                    return 0;
                });
    }
}