package net.forthecrown.commands;

import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserInteractions;
import net.forthecrown.user.manager.UserManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.UUID;

public class CommandIgnoreList extends FtcCommand {

    public CommandIgnoreList() {
        super("ignorelist");

        setPermission(Permissions.IGNORE);
        setDescription("Displays all the ignored players");
        setAliases(
                "blocked", "blockedplayers", "blockedlist",
                "ignoring", "ignored",
                "ignoredlist", "ignoredplayers",
                "ignorelist", "ignores",
                "listignores", "listignored"
        );

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /<command> [user]
     *
     * Permissions used:
     * ftc.commands.ignore
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> displayIgnored(c.getSource(), getUserSender(c)))

                .then(argument("user", UserArgument.user())
                        .requires(s -> s.hasPermission(Permissions.HELPER))
                        .executes(c -> displayIgnored(c.getSource(), UserArgument.getUser(c, "user")))
                );
    }

    private int displayIgnored(CommandSource source, CrownUser user){
        UserInteractions interactions = user.getInteractions();

        TextComponent.Builder builder = Component.text()
                .append(Component.translatable("commands.ignoredList").color(NamedTextColor.GOLD));

        for (UUID id: interactions.getBlockedUsers()){
            CrownUser blocked = UserManager.getUser(id);

            builder
                    .append(Component.space())
                    .append(blocked.nickDisplayName());
        }

        source.sendMessage(builder.build());
        return 0;
    }
}