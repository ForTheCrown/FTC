package net.forthecrown.commands.punishments;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.forthecrown.core.ForTheCrown;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.admin.StaffChat;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;

import java.util.concurrent.atomic.AtomicInteger;

public class CommandKickAll extends FtcCommand {
    public CommandKickAll(){
        super("kickall", ForTheCrown.inst());

        setPermission(Permissions.CORE_ADMIN);
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> kickAll(c.getSource(), null))

                .then(argument("reason", StringArgumentType.greedyString())
                        .executes(c -> kickAll(c.getSource(), c.getArgument("reason", String.class)))
                );
    }

    private int kickAll(CommandSource source, String reason){
        Component cReason = reason == null ? null : ChatUtils.convertString(reason, true);

        AtomicInteger amount = new AtomicInteger();
        Bukkit.getOnlinePlayers().stream()
                .filter(p -> !p.getName().equalsIgnoreCase(source.textName()))
                .forEach(p -> {
                    p.kick(cReason);
                    amount.getAndIncrement();
                });

        StaffChat.sendCommand(
                source,
                Component.text("Kicked ")
                        .color(NamedTextColor.YELLOW)
                        .append(Component.text(amount.get() + " people").color(NamedTextColor.GOLD))
        );
        return 0;
    }
}
