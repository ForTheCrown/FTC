package net.forthecrown.emperor.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.admin.MuteStatus;
import net.forthecrown.emperor.commands.manager.FtcCommand;
import net.forthecrown.emperor.user.UserManager;
import net.forthecrown.emperor.utils.ChatFormatter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;

public class CommandMe extends FtcCommand {

    public CommandMe() {
        super("ftc_me", CrownCore.inst());

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
                                MuteStatus status = CrownCore.getPunishmentManager().checkMute(source.asBukkit());

                                if(status == MuteStatus.HARD) return 0;
                                if(status == MuteStatus.SOFT) mayBroadcast = false;
                            }

                            Component displayName = source.isPlayer() ? UserManager.getUser(source.asPlayer()).coloredNickDisplayName() : source.displayName();
                            Component action = ChatFormatter.formatStringIfAllowed(c.getArgument("action", String.class), source.asBukkit());

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