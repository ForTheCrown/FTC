package net.forthecrown.commands.manager;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.forthecrown.commands.*;
import net.forthecrown.commands.admin.*;
import net.forthecrown.commands.arguments.*;
import net.forthecrown.commands.click.CommandClickableText;
import net.forthecrown.commands.economy.*;
import net.forthecrown.commands.emotes.EmotePog;
import net.forthecrown.commands.help.*;
import net.forthecrown.commands.markets.*;
import net.forthecrown.commands.marriage.*;
import net.forthecrown.commands.punishments.*;
import net.forthecrown.commands.regions.*;
import net.forthecrown.core.Crown;
import net.forthecrown.grenadier.RoyalArguments;
import net.forthecrown.grenadier.VanillaArgumentType;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.royalgrenadier.arguments.RoyalArgumentsImpl;
import net.forthecrown.user.RankTitle;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.TimeArgument;

import java.util.HashMap;
import java.util.Map;

public final class FtcCommands {

    public static final Map<String, FtcCommand> BY_NAME = new HashMap<>();
    public static final EnumArgument<RankTitle> RANK = EnumArgument.of(RankTitle.class);

    private FtcCommands(){}

    public static void init(){
        //ArgumentType registration
        VanillaArgumentType key = VanillaArgumentType.custom(ResourceLocationArgument::id);

        RoyalArgumentsImpl.register(UserArgument.class, UserArgument::getHandle, false);
        RoyalArgumentsImpl.register(TimeArgument.class, t -> t, true);

        RoyalArguments.register(BaltopArgument.class, VanillaArgumentType.custom(() -> IntegerArgumentType.integer(1, BaltopArgument.MAX)));
        RoyalArguments.register(ChatArgument.class, VanillaArgumentType.GREEDY_STRING);
        RoyalArguments.register(ComVarArgument.class, VanillaArgumentType.WORD);

        RoyalArguments.register(RegistryArguments.class, key);

        RoyalArguments.register(WarpArgument.class, key);
        RoyalArguments.register(KitArgument.class, key);

        //Command loading

        //Debug command
        if(Crown.inDebugMode()) {
            new CommandTestCore();
        }

        //Market commands
        new CommandMarket();
        new CommandShopTrust();
        new CommandMergeShop();
        new CommandUnmerge();
        new CommandTransferShop();
        new CommandUnclaimShop();
        new CommandMarketWarning();

        //admin commands
        new CommandKingMaker();
        new CommandBroadcast();
        new CommandFtcCore();
        new CommandStaffChat();
        new CommandResourceWorld();
        new CommandStaffChatToggle();
        new CommandHologram();
        new CommandComVar();
        new CommandGift();
        new CommandInteractable();
        new CommandSudo();
        new CommandSetSpawn();
        new CommandRoyals();
        new CommandFtcUser();
        new CommandPlayerTime();

        //Admin utility

        new CommandEndOpener();
        new CommandFtcUser();
        new CommandSpeed();
        new CommandItemName();
        new CommandLore();
        new CommandSign();
        new CommandRoyalSword();
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
        new CommandLaunch();
        new CommandAnimation();
        new CommandFtcStruct();
        new CommandGetOffset();
        new CommandItemStacks();
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
        new CommandLeave();
        new CommandWild();
        new CommandIgnore();
        new CommandIgnoreList();
        new CommandNear();
        new CommandNickname();
        new CommandSuicide();
        new CommandAfk();
        new CommandList();
        new CommandMe();
        new CommandArkBox();
        new CommandMail();
        new CommandCosmetics();
        new CommandClickableText();
        new CommandSettings();

        CommandDumbThing.init();
        CommandToolBlock.init();
        CommandSelfOrUser.init();
        StateChangeCommand.init();
        CommandLeaderboard.init();

        //economy commands
        new CommandShop();
        new CommandBalance();
        new CommandBalanceTop();
        new CommandPay();
        new CommandWithdraw();
        new CommandDeposit();
        new CommandBecomeBaron();
        new CommandEditShop();
        new CommandShopHistory();

        //help commands
        new HelpDiscord();
        new HelpFindPost();
        new HelpPost();
        new HelpSpawn();
        new HelpMap();
        new HelpBank();
        new HelpShop();
        new HelpRules();
        new HelpIp();

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
        new CommandRegionProperties();

        //emote, other emotes are initialized by cosmetics in CosmeticEmotes.init()
        new EmotePog();

        HelpPageArgument.MAX = Math.round(((float) FtcCommands.BY_NAME.size())/10);
        RoyalArguments.register(HelpPageArgument.class, VanillaArgumentType.custom(() -> IntegerArgumentType.integer(1, HelpPageArgument.MAX)));

        new HelpHelp();

        Crown.logger().info("Commands loaded");
    }
}
