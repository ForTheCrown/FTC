package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.exceptions.CrownCommandException;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import org.bukkit.Bukkit;

import java.util.UUID;

public class KingMakerCommand extends CrownCommandBuilder {

    public KingMakerCommand(){
        super("kingmaker", FtcCore.getInstance());

        setDescription("This command is used to assign and unassign a king or queen");
        setUsage("&7Usage:&r /kingmaker <remove | player> [king | queen]");
        register();
    }

    @Override
    protected void registerCommand(LiteralArgumentBuilder<CommandListenerWrapper> command) {
        command
                .executes(c -> {
                    c.getSource().getBukkitSender().sendMessage("The Current king is " + Bukkit.getOfflinePlayer(FtcCore.getKing()).getName());
                    return 0;
                })
                .then(argument("remove")
                        .executes(c ->{
                            if(FtcCore.getKing() == null) throw new CrownCommandException("There is already no king");

                            FtcCore.setKing(null);
                            c.getSource().getBukkitSender().sendMessage("King has been removed");
                            return 0;
                        })
                )
                .then(argument("player", StringArgumentType.word())
                        .suggests((c, b) -> getPlayerList(b).buildFuture())
                        .executes(c -> makeKing(c, false))

                        .then(argument("queen").executes(c -> makeKing(c, true)))
                        .then(argument("king").executes(c -> makeKing(c, false)))
                );
    }

    private int makeKing(CommandContext<CommandListenerWrapper> c, boolean isQueen) throws CrownCommandException {
        if(FtcCore.getKing() != null) throw new CrownCommandException("There already is a king");

        String playerName = c.getArgument("player", String.class);
        UUID id = getUUID(playerName);

        FtcCore.setKing(id);
        c.getSource().getBukkitSender().sendMessage(playerName + " is now the new king :D");

        String prefix = "&l[&e&lKing&r&l] &r";
        if(isQueen) prefix = "&l[&e&lQueen&r&l] &r";
        Bukkit.dispatchCommand(c.getSource().getBukkitSender(), "tab player " + playerName + " tabprefix " + prefix);
        return 0;
    }

    /*@Override
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
        return argList;
    }*/
}
