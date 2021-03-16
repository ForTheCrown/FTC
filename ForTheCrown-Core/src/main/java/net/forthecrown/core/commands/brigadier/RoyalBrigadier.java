package net.forthecrown.core.commands.brigadier;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.commands.*;
import net.forthecrown.core.commands.emotes.*;
import net.minecraft.server.v1_16_R3.CommandDispatcher;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;

//cool name lul

/**
 * ForTheCrown's command handler, The RoyalBrigadier lol
 * <p>Allows the usage of the server's native Brigadier command engine.</p>
 */
public final class RoyalBrigadier {

    private static CommandDispatcher dispatcher;
    private final FtcCore plugin;

    public RoyalBrigadier(FtcCore plugin){
        this.plugin = plugin;
        dispatcher = ((CraftServer) plugin.getServer()).getServer().getCommandDispatcher();

        loadCommands();
    }

    public FtcCore getPlugin() {
        return plugin;
    }

    public void loadCommands(){
        //admin commands
        new CommandKingMaker();
        new CommandBroadcast();
        new CommandCore();
        new CommandStaffChat();
        new CommandStaffChatToggle();
        new CommandHologram();

        //utility / misc commands
        new CommandGems();
        new CommandProfile();
        new CommandRank();
        new CommandGrave();

        //economy commands
        new CommandShop();
        new CommandBalance();
        new CommandBalanceTop();
        new CommandPay();
        new CommandWithdraw();
        new CommandDeposit();
        new CommandBecomeBaron();

        //help commands
        new HelpDiscord();
        new HelpFindPost();
        new HelpPost();
        new HelpSpawn();
        new HelpMap();

        //tpa commands
        new CommandTpask();
        new CommandTpaskHere();

        //emotes
        new EmoteToggle();
        new EmoteBonk();
        new EmoteSmooch();
        new EmotePoke();
        new EmoteScare();
        new EmoteJingle();
        new EmoteHug();
        new EmotePog();
    }

    public static CommandDispatcher getDispatcher() {
        return dispatcher;
    }
}
