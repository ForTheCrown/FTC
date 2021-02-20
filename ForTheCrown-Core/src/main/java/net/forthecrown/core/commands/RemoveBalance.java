package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.exceptions.CrownException;
import net.forthecrown.core.exceptions.InvalidPlayerInArgument;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;

public class RemoveBalance extends CrownCommand {
    public RemoveBalance(){
        super("removebalance", FtcCore.getInstance());

        setAliases("removebal");
        setUsage("&7Usage:&r /removebal <player>");
        register();
    }


    @Override
    public boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) throws CrownException {
        if(args.length < 1) return false;

        UUID id = FtcCore.getOffOnUUID(args[0]);
        if(id == null) throw new InvalidPlayerInArgument(sender, args[0]);

        Map<UUID, Integer> bals = FtcCore.getBalances().getBalanceMap();
        bals.remove(id);
        FtcCore.getBalances().setBalanceMap(bals);

        sender.sendMessage(args[1] + "'s balance has been reset");
        return true;
    }
}
