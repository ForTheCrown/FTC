package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;

import java.util.Map;
import java.util.UUID;

public class ResetBalance extends CrownCommandBuilder {
    public ResetBalance(){
        super("removebalance", FtcCore.getInstance());

        setAliases("removebal");
        setUsage("&7Usage:&r /removebal <player>");
        register();
    }

    @Override
    protected void registerCommand(LiteralArgumentBuilder<CommandListenerWrapper> command) {
        command
                .then(argument("player", StringArgumentType.word())
                        .executes(c ->{
                            String name = c.getArgument("player", String.class);
                            UUID id = getUUID(name);

                            Balances bals = FtcCore.getBalances();
                            Map<UUID, Integer> balMap = bals.getBalanceMap();

                            balMap.remove(id);
                            bals.setBalanceMap(balMap);

                            c.getSource().getBukkitSender().sendMessage("Reset balance of " + name);
                            return 0;
                        })
                );
    }


    /*@Override
    public boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) throws CrownException {
        if(args.length < 1) return false;

        UUID id = FtcCore.getOffOnUUID(args[0]);
        if(id == null) throw new InvalidPlayerInArgument(sender, args[0]);

        Map<UUID, Integer> bals = FtcCore.getBalances().getBalanceMap();
        bals.remove(id);
        FtcCore.getBalances().setBalanceMap(bals);

        sender.sendMessage(args[1] + "'s balance has been reset");
        return true;
    }*/
}
