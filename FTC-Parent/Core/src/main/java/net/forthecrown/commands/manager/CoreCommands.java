package net.forthecrown.commands.manager;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.forthecrown.commands.*;
import net.forthecrown.commands.arguments.*;
import net.forthecrown.commands.clickevent.ClickEventCommand;
import net.forthecrown.commands.emotes.*;
import net.forthecrown.commands.help.*;
import net.forthecrown.commands.marriage.*;
import net.forthecrown.commands.punishments.*;
import net.forthecrown.core.CrownCore;
import net.forthecrown.grenadier.RoyalArguments;
import net.forthecrown.grenadier.VanillaArgumentType;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.royalgrenadier.RoyalArgumentsImpl;
import net.forthecrown.user.enums.Branch;
import net.forthecrown.user.enums.Rank;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.ScoreHolderArgument;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public final class CoreCommands {

    public static final Map<String, FtcCommand> BY_NAME = new HashMap<>();
    public static final EnumArgument<Branch> BRANCH = EnumArgument.of(Branch.class);
    public static final EnumArgument<Rank> RANK = EnumArgument.of(Rank.class);

    private static Commands dispatcher;

    private CoreCommands(){}

    public static void init(){
        dispatcher = MinecraftServer.getServer().vanillaCommandDispatcher;

        VanillaArgumentType key = VanillaArgumentType.custom(ResourceLocationArgument::id);

        //ArgumentType registration
        RoyalArgumentsImpl.register(NbtType.class, NbtTagArgument::nbtTag, true);
        RoyalArguments.register(BaltopType.class, VanillaArgumentType.custom(() -> IntegerArgumentType.integer(1, BaltopType.MAX)));
        RoyalArguments.register(UserType.class, VanillaArgumentType.custom(ScoreHolderArgument::scoreHolders));
        RoyalArguments.register(ComVarArgument.class, VanillaArgumentType.WORD);
        RoyalArguments.register(PetType.class, VanillaArgumentType.WORD);
        RoyalArguments.register(ActionArgType.class, key);
        RoyalArguments.register(CheckArgType.class, key);
        RoyalArguments.register(WarpType.class, key);
        RoyalArguments.register(KitType.class, key);

        //Command loading

        //Debug command
        if(CrownCore.inDebugMode()) new CommandTestCore();

        //admin commands
        new CommandKingMaker();
        new CommandBroadcast();
        new CommandFtcCore();
        new CommandStaffChat();
        new CommandStaffChatToggle();
        new CommandHologram();
        new CommandComVar();
        new CommandGift();
        new CommandInteractable();
        new CommandSudo();
        new CommandSetSpawn();
        new CommandRoyals();
        new CommandPirate();
        new CommandDataFix();
        new CommandCosmeticEffects();

        //Admin utility
        new CommandSpeed();
        new CommandItemName();
        new CommandLore();
        new CommandSign();
        new CommandGameMode();
        new CommandWorld();
        new CommandWeather();
        new CommandEnchant();
        new CommandGetPos();
        new CommandTop();
        new CommandMemory();
        new CommandSkull();
        new CommandMakeAward();
        new CommandBlackMarketNpc();
        new CommandTeleportExact();
        CommandSpecificGameMode.init();

        //Policing commands
        new CommandEavesDrop();
        new CommandVanish();
        new CommandMute();
        new CommandSoftMute();
        new CommandJail();
        new CommandJails();
        new CommandPardon();
        new CommandTempBan();
        CommandPunishment.init();

        //utility / misc commands
        new CommandGems();
        new CommandProfile();
        new CommandRank();
        new CommandGrave();
        new CommandLeave();
        new CommandWild();
        new CommandIgnore();
        new CommandNear();
        new CommandNickname();
        new CommandSuicide();
        new CommandAfk();
        new CommandList();
        new CommandMe();
        new CommandVolleyBall();
        new CommandParrot();
        new CommandCosmetics();
        new CommandToggleRiding();
        new CommandIgnoreAC();

        CommandDumbThing.init();
        CommandToolBlock.init();
        CommandSelfOrUser.init();

        //top me daddy xD
        new CommandDeathTop();
        new CommandCrownTop();

        //economy commands
        new CommandShop();
        new CommandBalance();
        new CommandBalanceTop();
        new CommandPay();
        new CommandPayToggle();
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
        new HelpRules();

        //teleportation commands
        new CommandTpask();
        new CommandTpaskHere();
        new CommandTpaToggle();
        new CommandTpaAccept();
        new CommandTpaCancel();
        new CommandTpDenyAll();
        new CommandTpDeny();
        new CommandTpCancel();
        new CommandBack();
        new CommandTeleport();

        //warp commands
        new CommandWarp();
        new CommandWarpList();
        new CommandWarpEdit();

        //kit commands
        new CommandKit();
        new CommandKitEdit();
        new CommandKitList();

        //message commands
        new CommandTell();
        new CommandReply();

        //Click event command
        new ClickEventCommand();

        //Marriage commands ¬_¬
        new CommandDivorce();
        new CommandMarriageAccept();
        new CommandMarriageDeny();
        new CommandMarriageChat();
        new CommandMarriageChatToggle();
        new CommandMarry();
        new CommandMarryToggle();

        //Home commands
        new CommandHome();
        new CommandSetHome();
        new CommandDelHome();
        new CommandHomeList();

        //emotes
        new EmoteToggle();
        new EmotePog();

        CommandHelpType.MAX = Math.round(((float) CoreCommands.BY_NAME.size())/10);
        RoyalArguments.register(CommandHelpType.class, VanillaArgumentType.custom(() -> IntegerArgumentType.integer(1, CommandHelpType.MAX)));

        new HelpHelp();

        CrownCore.logger().log(Level.INFO, "All commands loaded and registered");
    }

    /**
     * Resends all the commands packets, for stuff like the permissions and the test() method
     * @param p The player to resend the packets to
     */
    public static void resendCommandPackets(Player p){
        CraftPlayer player = (CraftPlayer) p;
        getServerCommands().sendCommands(player.getHandle());
    }

    /**
     * Resends command packets for every player on the server
     * @see CoreCommands#resendCommandPackets(Player)
     */
    public static void resendAllCommandPackets(){
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
    public static Commands getServerCommands() {
        return dispatcher;
    }
}
