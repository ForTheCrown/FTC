package net.forthecrown.core.commands.brigadier;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.clickevent.ClickEventCommand;
import net.forthecrown.core.commands.*;
import net.forthecrown.core.commands.brigadier.types.ComVarArgument;
import net.forthecrown.core.commands.brigadier.types.PetType;
import net.forthecrown.core.commands.brigadier.types.UserType;
import net.forthecrown.core.commands.emotes.*;
import net.forthecrown.core.enums.Branch;
import net.forthecrown.core.enums.Rank;
import net.forthecrown.grenadier.RoyalArguments;
import net.forthecrown.grenadier.VanillaArgumentType;
import net.forthecrown.grenadier.types.EnumArgument;
import net.minecraft.server.v1_16_R3.ArgumentScoreholder;
import net.minecraft.server.v1_16_R3.CommandDispatcher;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public final class CoreCommands {

    public static final EnumArgument<Branch> BRANCH = EnumArgument.of(Branch.class);
    public static final EnumArgument<Rank> RANK = EnumArgument.of(Rank.class);

    private static CommandDispatcher dispatcher;
    private final FtcCore plugin;

    public CoreCommands(FtcCore plugin){
        this.plugin = plugin;
        dispatcher = ((CraftServer) plugin.getServer()).getServer().getCommandDispatcher();

        registerArguments();
        loadCommands();
    }

    public FtcCore getPlugin() {
        return plugin;
    }

    public void registerArguments(){
        RoyalArguments.register(PetType.class, VanillaArgumentType.WORD);
        RoyalArguments.register(UserType.class, VanillaArgumentType.custom(ArgumentScoreholder::b));
        RoyalArguments.register(ComVarArgument.class, VanillaArgumentType.WORD);
    }

    public void loadCommands(){
        //admin commands
        new CommandKingMaker();
        new CommandBroadcast();
        new CommandCore();
        new CommandStaffChat();
        new CommandStaffChatToggle();
        new CommandHologram();
        new CommandComVar();
        new CommandGift();
        new CommandUseableSign();

        if(FtcCore.inDebugMode.getValue(false)){
            new CommandTestCore();
            new HelpHelp();
        }

        //utility / misc commands
        new CommandGems();
        new CommandProfile();
        new CommandRank();
        new CommandGrave();
        new CommandLeave();
        new CommandWild();

        //top me daddy xD
        new CommandDeathTop();
        new CommandCrownTop();

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
        new HelpShop();

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

        plugin.getLogger().info("All commands registered");
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
     * @see CoreCommands#resendCommandPackets(Player)
     */
    public void resendAllCommandPackets(){
        for (Player p: Bukkit.getOnlinePlayers()){
            resendCommandPackets(p);
        }
    }

    /**
     * Gets the server's command class, that handles the all things related to commands, type name is misleading
     * <p>MCCoderPack devs were dumb AF and called this a dispatcher</p>
     * <p>The actual dispatcher is in the returned class</p>
     * @return The server's commands class
     */
    public static CommandDispatcher getServerCommands() {
        return dispatcher;
    }
}
