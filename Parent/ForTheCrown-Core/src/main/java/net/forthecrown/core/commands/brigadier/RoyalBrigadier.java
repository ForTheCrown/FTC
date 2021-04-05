package net.forthecrown.core.commands.brigadier;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.clickevent.ClickEventCommand;
import net.forthecrown.core.commands.*;
import net.forthecrown.core.commands.emotes.*;
import net.minecraft.server.v1_16_R3.CommandDispatcher;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

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
        new CommandTestCore();
        new CommandComVar();

        //utility / misc commands
        new CommandGems();
        new CommandProfile();
        new CommandRank();
        new CommandGrave();
        new CommandLeave();

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
        new HelpBank();

        //tpa commands
        new CommandTpask();
        new CommandTpaskHere();

        //Click event command
        new ClickEventCommand();

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

    public void resendCommandPackets(Player p){
        CraftPlayer player = (CraftPlayer) p;
        getDispatcher().a(player.getHandle());
    }

    public void resendAllCommandPackets(){
        for (Player p: Bukkit.getOnlinePlayers()){
            resendCommandPackets(p);
        }
    }

    public static CommandDispatcher getDispatcher() {
        return dispatcher;
    }
}
