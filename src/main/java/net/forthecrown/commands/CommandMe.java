package net.forthecrown.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.admin.BannedWords;
import net.forthecrown.core.admin.Mute;
import net.forthecrown.core.admin.Punishments;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.text.Text;
import net.forthecrown.user.Users;
import net.kyori.adventure.text.Component;

public class CommandMe extends FtcCommand {

    public CommandMe() {
        super("ftc_me");

        setAliases("me");
        setPermission(Permissions.DEFAULT);

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /me <action>
     *
     * Permissions used:
     * ftc.default
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("action", StringArgumentType.greedyString())
                        .executes(c -> {
                            CommandSource source = c.getSource();
                            boolean mayBroadcast = true;

                            if(source.isPlayer()){
                                Mute status = Punishments.checkMute(source.asBukkit());

                                if (status == Mute.HARD) {
                                    return 0;
                                }

                                if (status == Mute.SOFT) {
                                    mayBroadcast = false;
                                }
                            }

                            Component displayName = Text.sourceDisplayName(source);
                            Component action = Text.renderString(
                                    source.asBukkit(), c.getArgument("action", String.class)
                            );

                            //Check they didn't use a banned word
                            if (BannedWords.checkAndWarn(source.asBukkit(), action)) {
                                return 0;
                            }

                            Component formatted = Component.text()
                                    .append(Component.text("* "))
                                    .append(displayName)
                                    .append(Component.space())
                                    .append(action)
                                    .build();

                            source.sendMessage(formatted);

                            if (mayBroadcast) {
                                Users.getOnline()
                                        .stream()
                                        .filter(user -> !user.getName().equals(source.textName()))
                                        .forEach(user -> user.sendMessage(formatted));
                            }
                            return 0;
                        })
                );
    }
}