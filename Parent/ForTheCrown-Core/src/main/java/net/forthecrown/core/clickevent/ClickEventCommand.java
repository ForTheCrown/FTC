package net.forthecrown.core.clickevent;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import org.bukkit.entity.Player;

public class ClickEventCommand extends CrownCommandBuilder {

    public ClickEventCommand(){
        super("npcconverse", FtcCore.getInstance());
        setDescription("The Command used by the ClickEventManager to execute code ran by clickable text");register();
        register();
    }

    @Override
    public boolean test(CommandSource sender) {
        if(!testPermissionSilent(sender.asBukkit())) return false;
        if(!sender.isPlayer()) return false;
        return ClickEventHandler.isAllowedToUseCommand((Player) sender.asBukkit());
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.then(argument("args", StringArgumentType.greedyString())
                .executes(c -> {
                    Player p = getPlayerSender(c);
                    if(!ClickEventHandler.isAllowedToUseCommand(p)) return 0;
                    String arg = c.getArgument("args", String.class);
                    String[] args = arg.split(" ");
                    if(args.length < 1) return 0;
                    if(!ClickEventHandler.getRegisteredClickEvents().contains(args[0])) return 0;

                    ClickEventHandler.callClickEvent(args[0], args, p);
                    return 0;
                })
        );
    }
}
