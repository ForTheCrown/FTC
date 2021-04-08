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
 * Our own, very hacky, implementation of Mojang's own Brigadier Command Engine, the Royal Brigadier
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
        new CommandEditShop();

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

    /**
     * Resends all the commands packets, for stuff like the permissions and the test() method
     * @param p The player to resend the packets to
     */
    public void resendCommandPackets(Player p){
        CraftPlayer player = (CraftPlayer) p;
        getServerCommands().a(player.getHandle());
    }

    /**
     * Resends command packets for every player on the server
     * @see RoyalBrigadier#resendCommandPackets(Player)
     */
    public void resendAllCommandPackets(){
        for (Player p: Bukkit.getOnlinePlayers()){
            resendCommandPackets(p);
        }
    }

    /**
     *
     * Gets the server's command class, that handles the all things related to commands, type name is misleading
     * <p>MCCoderPack devs were dumb AF and called this a dispatcher</p>
     * <p>The actual dispatcher is in the command registry</p>
     * @return The server's commands class
     */
    public static CommandDispatcher getServerCommands() {
        return dispatcher;
    }
}
