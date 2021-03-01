package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;

import java.util.UUID;

public class GemsCommand extends CrownCommandBuilder {

    public GemsCommand(){
        super("gems", FtcCore.getInstance());

        setUsage("&7Usage: &r/gems <player>");
        setDescription("Shows the amount of gems you have or another player has.");
        register();
    }

    @Override
    protected void registerCommand(LiteralArgumentBuilder<CommandListenerWrapper> command) {
        command
                .then(argument("player", StringArgumentType.word())
                        .executes(c ->{
                            CrownUser user = getUserSender(c);

                            UUID id = getUUID(c.getArgument("player", String.class));
                            CrownUser other = FtcCore.getUser(id);

                            user.sendMessage("&e" + other.getName() + " &7has &e" + other.getGems() + " Gems");
                            return -1000;
                        })
                )
                .executes(c ->{
                    CrownUser user = getUserSender(c);
                    user.sendMessage("&7You have &e" + user.getGems() + " Gems");
                    return 1000;
                });
    }

    /*@Override
    public boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if(!(sender instanceof Player)) throw new NonPlayerExecutor(sender);

        CrownUser user = FtcCore.getUser(((Player) sender).getUniqueId());

        if(args.length < 1){
            user.sendMessage("&7You have &e" + user.getGems() + " Gems");
            return true;
        }

        CrownUser target;
        try {
            target = FtcCore.getUser(FtcCore.getOffOnUUID(args[0]));
        } catch (NullPointerException e){ throw new InvalidPlayerInArgument(sender, args[0]); }

        user.sendMessage("&e" + args[0] + " &7has &e" + target.getGems() + " Gems");
        return true;
    }*/
}
