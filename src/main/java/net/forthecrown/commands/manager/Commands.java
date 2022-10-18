package net.forthecrown.commands.manager;

import com.mojang.brigadier.StringReader;
import net.forthecrown.commands.*;
import net.forthecrown.commands.admin.*;
import net.forthecrown.commands.click.CommandClickableText;
import net.forthecrown.commands.economy.*;
import net.forthecrown.commands.emotes.EmotePog;
import net.forthecrown.commands.help.HelpCommand;
import net.forthecrown.commands.home.CommandDeleteHome;
import net.forthecrown.commands.home.CommandHome;
import net.forthecrown.commands.home.CommandHomeList;
import net.forthecrown.commands.home.CommandSetHome;
import net.forthecrown.commands.item.ItemModCommands;
import net.forthecrown.commands.markets.*;
import net.forthecrown.commands.marriage.*;
import net.forthecrown.commands.punish.*;
import net.forthecrown.commands.regions.*;
import net.forthecrown.commands.test.TestCommands;
import net.forthecrown.commands.tpa.*;
import net.forthecrown.commands.usables.InteractableCommands;
import net.forthecrown.commands.usables.UseCmdCommand;
import net.forthecrown.commands.user.UserCommands;
import net.forthecrown.core.Crown;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.user.data.RankTitle;

import java.util.HashMap;
import java.util.Map;

public final class Commands {
    public static final StringReader EMPTY_READER = new StringReader("");

    public static final Map<String, FtcCommand> BY_NAME = new HashMap<>();
    public static final EnumArgument<RankTitle> RANK = EnumArgument.of(RankTitle.class);

    private Commands(){}

    //Command loading
    public static void init() {
        if (Crown.inDebugMode()) {
            TestCommands.createCommands();
        }

        //Market commands
        new CommandMarket();
        new CommandShopTrust();
        new CommandMergeShop();
        new CommandUnmerge();
        new CommandTransferShop();
        new CommandUnclaimShop();
        new CommandMarketWarning();
        new CommandMarketEditing();
        new CommandMarketAppeal();

        //admin commands
        new CommandBroadcast();
        new CommandIllegalWorlds();
        new CommandJoinInfo();
        new CommandStaffChat();
        new CommandResourceWorld();
        new CommandHologram();
        new CommandVar();
        new CommandGift();
        new CommandSudo();
        new CommandSetSpawn();
        new CommandDungeons();
        new CommandPlayerTime();
        new CommandHolidays();
        new CommandEndOpener();
        new CommandSpeed();
        new CommandSign();
        new CommandRoyalSword();
        new CommandGameMode();
        new CommandWorld();
        new CommandWeather();
        new CommandGetPos();
        new CommandTop();
        new CommandMemory();
        new CommandSkull();
        new CommandMakeAward();
        new CommandTeleportExact();
        new CommandTime();
        new CommandNPC();
        new CommandLaunch();
        new CommandFtcStruct();
        new CommandGetOffset();
        new CommandStructFunction();

        InteractableCommands.createCommands();
        CommandSpecificGameMode.createCommands();
        ItemModCommands.createCommands();
        SaveReloadCommands.createCommands();
        UserCommands.createCommands();

        //Policing commands
        new CommandVanish();
        new CommandPunish();
        new CommandSeparate();
        new CommandSmite();
        new CommandNotes();
        PunishmentCommand.createCommands();

        //utility / misc commands
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
        new CommandMail();
        new CommandCosmetics();
        new CommandClickableText();
        new CommandSettings();

        CommandDumbThing.createCommands();
        ToolBlockCommands.createCommands();
        CommandSelfOrUser.createCommands();
        ToggleCommand.createCommands();
        CommandLeaderboard.createCommands();
        UserMapCommand.createCommands();
        UserMapTopCommand.createCommands();
        UseCmdCommand.createCommands();

        //economy commands
        new CommandPay();
        new CommandWithdraw();
        new CommandDeposit();
        new CommandBecomeBaron();
        new CommandEditShop();
        new CommandShopHistory();
        new CommandShop();

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
        new CommandAddPole();
        new CommandCancelInvite();
        new CommandListRegions();
        new CommandRandomRegion();
        new CommandInvite();
        new CommandRegionData();
        new CommandWhichRegion();
        new CommandRegionProperties();
        new CommandRegionResidents();

        //emote, other emotes are initialized by cosmetics in CosmeticEmotes.init()
        new EmotePog();

        //help commands
        HelpCommand.createCommands();
    }
}