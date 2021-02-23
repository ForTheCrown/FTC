package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.exceptions.InvalidArgumentException;
import net.forthecrown.core.exceptions.InvalidPlayerInArgument;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class KingMakerCommand extends CrownCommand implements TabCompleter {

    public KingMakerCommand(){
        super("kingmaker", FtcCore.getInstance());

        setDescription("This command is used to assign and unassign a king or queen");
        setUsage("&7Usage:&r /kingmaker <remove | player> [king | queen]");
        setTabCompleter(this);
        register();
    }

    @Override
    public boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if(args.length < 1) return false;

        if(args[0].contains("remove")){
            if(FtcCore.getKing() == null) throw new InvalidArgumentException(sender, "There is already no king");

            Bukkit.dispatchCommand(sender, "tab player " + Bukkit.getOfflinePlayer(FtcCore.getKing()).getName() + " tabprefix");
            FtcCore.setKing(null);
            sender.sendMessage("King has been removed!");
            return true;

        } else {
            OfflinePlayer player = Bukkit.getOfflinePlayerIfCached(args[0]);
            if(player == null) throw new InvalidPlayerInArgument(sender, args[0]);

            if(FtcCore.getKing() != null){
                sender.sendMessage(ChatColor.GRAY + "There is already a king!");
                return true;
            }

            String prefix = "&l[&e&lKing&r&l] &r";
            if(args.length == 2 && args[1].contains("queen")) prefix = "&l[&e&lQueen&r&l] &r";

            Bukkit.dispatchCommand(sender, "tab player " + player.getName() + " tabprefix " + prefix);
            sender.sendMessage("King has been set!");
            FtcCore.setKing(player.getUniqueId());
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> argList = new ArrayList<>();

        if(args.length == 1){
            argList.add("remove");
            argList.addAll(getPlayerNameList());
        }
        if(args.length == 2 && !args[0].equals("remove")){
            argList.add("queen");
            argList.add("king");
        }
        return StringUtil.copyPartialMatches(args[args.length - 1], argList, new ArrayList<>());
    }
}
