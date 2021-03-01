package net.forthecrown.core.clickevent;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import org.bukkit.entity.Player;

public class ClickEventCommand extends CrownCommandBuilder {

    public ClickEventCommand(){
        super("npcconverse", FtcCore.getInstance());
        setDescription("The Command used by the ClickEventManager to execute code ran by clickable text");register();
        register();
    }

    @Override
    public boolean test(CommandListenerWrapper sender) {
        if(!testPermissionSilent(sender.getBukkitSender())) return false;
        if(!(sender.getBukkitSender() instanceof Player)) return false;
        if(!ClickEventHandler.isAllowedToUseCommand((Player) sender.getBukkitSender())) return false;
        return true;
    }

    @Override
    protected void registerCommand(LiteralArgumentBuilder<CommandListenerWrapper> command) {
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

    /*@Override
    public boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if(!(sender instanceof Player)) return false;
        if(!ClickEventHandler.isAllowedToUseCommand((Player) sender)) return false;
        if(args.length < 1) return false;
        if(!ClickEventHandler.getRegisteredClickEvents().contains(args[0])) return false;

        ClickEventHandler.callClickEvent(args[0], args, ((Player) sender));
        return true;
    }*/
}
