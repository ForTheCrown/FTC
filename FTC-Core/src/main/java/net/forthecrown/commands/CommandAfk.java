package net.forthecrown.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.admin.MuteStatus;
import net.forthecrown.core.chat.BannedWords;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;

public class CommandAfk extends FtcCommand {
    public CommandAfk(){
        super("afk", Crown.inst());

        setDescription("Marks or un-marks you as AFK");
        setPermission(Permissions.DEFAULT);

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> afk(getUserSender(c), null))

                .then(literal("-other")
                        .requires(s -> s.hasPermission(Permissions.ADMIN))

                        .then(argument("user", UserArgument.onlineUser())
                                .requires(s -> s.hasPermission(Permissions.ADMIN))

                                .executes(c -> afk(
                                        UserArgument.getUser(c, "user"),
                                        null
                                ))
                        )
                )

                .then(argument("msg", StringArgumentType.greedyString())
                        .executes(c -> afk(
                                getUserSender(c),
                                c.getArgument("msg", String.class)
                        ))
                );
    }

    private int afk(CrownUser user, String message){
        boolean alreadyAFK = user.isAfk();
        boolean hasMessage = message != null;

        Component userMsg;
        Component broadcastMsg;

        if(alreadyAFK){
            userMsg = Component.translatable("unafk.self").color(NamedTextColor.GRAY);
            broadcastMsg = Component.translatable("unafk.others", user.nickDisplayName())
                    .hoverEvent(Component.text("Click to welcome them back!"))
                    //.clickEvent(ChatFormatter.unAfkClickEvent())
                    .color(NamedTextColor.GRAY);
        } else {
            MuteStatus status = Crown.getPunishmentManager().checkMuteSilent(user.getUniqueId());

            userMsg = Component.translatable("afk.self",
                    (hasMessage ? Component.text(": " + message) : Component.empty())
            ).color(NamedTextColor.GRAY);

            broadcastMsg = Component.translatable("afk.others", user.nickDisplayName(),
                    hasMessage && status.maySpeak ? Component.text(": " + message) : Component.empty())
                    .color(NamedTextColor.GRAY);

            broadcastMsg = status.maySpeak && BannedWords.contains(broadcastMsg) ? broadcastMsg : null;
        }

        user.sendMessage(userMsg);
        user.setAfk(!alreadyAFK, message);

        Component finalBroadcastMsg = broadcastMsg;
        Bukkit.getOnlinePlayers().stream()
                .filter(plr -> !plr.getUniqueId().equals(user.getUniqueId()))
                .forEach(p -> p.sendMessage(finalBroadcastMsg));
        return 0;
    }
}
