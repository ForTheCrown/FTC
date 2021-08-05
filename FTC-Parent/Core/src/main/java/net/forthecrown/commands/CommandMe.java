package net.forthecrown.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.forthecrown.core.ForTheCrown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.admin.MuteStatus;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.user.UserManager;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;

public class CommandMe extends FtcCommand {

    public CommandMe() {
        super("ftc_me", ForTheCrown.inst());

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
     * Main Author: Ants
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("action", StringArgumentType.greedyString())
                        .executes(c -> {
                            CommandSource source = c.getSource();
                            boolean mayBroadcast = true;

                            if(source.isPlayer()){
                                MuteStatus status = ForTheCrown.getPunishmentManager().checkMute(source.asBukkit());

                                if(status == MuteStatus.HARD) return 0;
                                if(status == MuteStatus.SOFT) mayBroadcast = false;
                            }

                            Component displayName = source.isPlayer() ? UserManager.getUser(source.asPlayer()).coloredNickDisplayName() : source.displayName();
                            Component action = FtcFormatter.formatIfAllowed(c.getArgument("action", String.class), source.asBukkit());

                            Component formatted = Component.text()
                                    .append(Component.text("* "))
                                    .append(displayName)
                                    .append(Component.space())
                                    .append(action)
                                    .build();

                            source.sendMessage(formatted);

                            if(mayBroadcast){
                                Bukkit.getOnlinePlayers().stream()
                                        .filter(p -> !p.getName().equalsIgnoreCase(source.textName()))
                                        .forEach(p -> p.sendMessage(formatted));
                            }
                            return 0;
                        })
                );
    }
}