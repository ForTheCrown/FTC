package net.forthecrown.core.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.events.RankGuiUseEvent;
import net.forthecrown.core.inventories.RankInventory;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import org.bukkit.entity.Player;

public class RankCommand extends CrownCommandBuilder {

    public RankCommand(){
        super("rank", FtcCore.getInstance());

        setAliases("ranks");
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Gives the executor the RankGUI and allows them to switch ranks
     *
     *
     * Valid usages of command:
     * - /rank
     * - /ranks
     *
     * Author: Wout
     */

    @Override
    protected void registerCommand(LiteralArgumentBuilder<CommandListenerWrapper> command) {
        command.executes(c ->{
            Player player = getPlayerSender(c);

            FtcCore.getInstance().getServer().getPluginManager().registerEvents(new RankGuiUseEvent(player), FtcCore.getInstance());
            player.openInventory(new RankInventory(FtcCore.getUser(player)).getUsersRankGUI());
            return 0;
        });
    }

    /*@Override
    public boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if(!(sender instanceof Player)) throw new NonPlayerExecutor(sender);
        Player player = (Player) sender;

        FtcCore.getInstance().getServer().getPluginManager().registerEvents(new RankGuiUseEvent(player), FtcCore.getInstance());
        player.openInventory(new RankInventory(FtcCore.getUser(player)).getUsersRankGUI());
        return true;
    }*/
}
