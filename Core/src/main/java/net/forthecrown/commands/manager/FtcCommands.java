package net.forthecrown.commands.manager;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.forthecrown.commands.*;
import net.forthecrown.commands.arguments.*;
import net.forthecrown.commands.clickevent.ClickEventCommand;
import net.forthecrown.commands.emotes.EmotePog;
import net.forthecrown.commands.help.*;
import net.forthecrown.commands.marriage.*;
import net.forthecrown.commands.punishments.*;
import net.forthecrown.commands.regions.*;
import net.forthecrown.core.Crown;
import net.forthecrown.grenadier.RoyalArguments;
import net.forthecrown.grenadier.VanillaArgumentType;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.grenadier.types.KeyArgument;
import net.forthecrown.royalgrenadier.arguments.RoyalArgumentsImpl;
import net.forthecrown.user.enums.Faction;
import net.forthecrown.user.enums.Rank;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public final class FtcCommands {

    public static final Map<String, FtcCommand> BY_NAME = new HashMap<>();
    public static final EnumArgument<Faction> BRANCH = EnumArgument.of(Faction.class);
    public static final EnumArgument<Rank> RANK = EnumArgument.of(Rank.class);
    public static final KeyArgument FTC_KEY_PARSER = KeyArgument.key(Crown.inst());

    private static Commands dispatcher;

    private FtcCommands(){}

    public static void init(){
        dispatcher = MinecraftServer.getServer().vanillaCommandDispatcher;

        VanillaArgumentType key = VanillaArgumentType.custom(ResourceLocationArgument::id);

        //ArgumentType registration
        RoyalArgumentsImpl.register(UserArgument.class, UserArgument::getHandle, false);
        RoyalArgumentsImpl.register(NBTArgument.class, NBTArgument::getHandle, true);
        RoyalArgumentsImpl.register(TimeArgument.class, t -> t, true);

        RoyalArguments.register(BaltopArgument.class, VanillaArgumentType.custom(() -> IntegerArgumentType.integer(1, BaltopArgument.MAX)));
        RoyalArguments.register(ComVarArgument.class, VanillaArgumentType.WORD);
        RoyalArguments.register(PetArgument.class, VanillaArgumentType.WORD);

        RoyalArguments.register(RegistryArguments.class, key);

        RoyalArguments.register(WarpArgument.class, key);
        RoyalArguments.register(KitArgument.class, key);

        //Command loading

        //Debug command
        if(Crown.inDebugMode()) {
            new CommandTestCore();
        }

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
        new CommandTeleportExact();
        new CommandTime();
        new CommandNPC();
        new CommandGhStand();
        new CommandLaunch();
        new CommandAnimation();
        new CommandShopTemplate();
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
        new CommandSeparate();
        new CommandSmite();
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

        CommandDumbThing.init();
        CommandToolBlock.init();
        CommandSelfOrUser.init();
        StateChangeCommand.init();

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
        new HelpRules();

        //teleportation commands
        new CommandTpask();
        new CommandTpaskHere();
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
        new CommandMarry();

        //Home commands
        new CommandHome();
        new CommandSetHome();
        new CommandDeleteHome();
        new CommandHomeList();

        //region commands
        new CommandVisit();
        new CommandMovePole();
        new CommandResetRegion();
        new CommandRenameRegion();
        new CommandMoveToRegion();
        new CommandGotoRegion();
        new CommandHomePole();
        new CommandSetHomeRegion();
        new CommandRegionDescription();
        new CommandAddPole();
        new CommandCancelInvite();
        new CommandListRegions();
        new CommandRandomRegion();
        new CommandInvite();
        new CommandRegionData();
        new CommandWhichRegion();

        //emote
        new EmotePog();

        HelpPageArgument.MAX = Math.round(((float) FtcCommands.BY_NAME.size())/10);
        RoyalArguments.register(HelpPageArgument.class, VanillaArgumentType.custom(() -> IntegerArgumentType.integer(1, HelpPageArgument.MAX)));

        new HelpHelp();

        Crown.logger().log(Level.INFO, "Commands loaded");
    }

    public static KeyArgument ftcKeyType() {
        return FTC_KEY_PARSER;
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
     * @see FtcCommands#resendCommandPackets(Player)
     */
    public static void resendAllCommandPackets(){
        for (Player p: Bukkit.getOnlinePlayers()){
            resendCommandPackets(p);
        }
    }

    /**
     * Gets the server's command class, that handles the all things related to commands, type name is misleading
     * <p>The actual dispatcher is in the returned class</p>
     * @return The server's commands class
     */
    public static Commands getServerCommands() {
        return dispatcher;
    }
}