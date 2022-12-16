package net.forthecrown.commands.manager;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.*;
import net.forthecrown.commands.admin.*;
import net.forthecrown.commands.click.CommandClickableText;
import net.forthecrown.commands.economy.*;
import net.forthecrown.commands.emotes.EmotePog;
import net.forthecrown.commands.guild.GuildCommands;
import net.forthecrown.commands.help.HelpCommand;
import net.forthecrown.commands.home.CommandDeleteHome;
import net.forthecrown.commands.home.CommandHome;
import net.forthecrown.commands.home.CommandHomeList;
import net.forthecrown.commands.home.CommandSetHome;
import net.forthecrown.commands.item.ItemModCommands;
import net.forthecrown.commands.markets.*;
import net.forthecrown.commands.marriage.*;
import net.forthecrown.commands.punish.*;
import net.forthecrown.commands.test.TestCommands;
import net.forthecrown.commands.tpa.*;
import net.forthecrown.commands.usables.InteractableCommands;
import net.forthecrown.commands.usables.UseCmdCommand;
import net.forthecrown.commands.user.UserCommands;
import net.forthecrown.commands.waypoint.*;
import net.forthecrown.core.FTC;
import net.forthecrown.core.module.OnEnable;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.user.data.RankTitle;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.text.format.page.PageEntryIterator;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public final class Commands {
    public static final StringReader EMPTY_READER = new StringReader("");

    public static final Map<String, FtcCommand> BY_NAME = new HashMap<>();
    public static final EnumArgument<RankTitle> RANK = EnumArgument.of(RankTitle.class);

    private Commands(){}

    //Command loading
    @OnEnable
    private static void init() {
        if (FTC.inDebugMode()) {
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
        new CommandScripts();

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
        new CommandHat();
        new CommandSay();
        new CommandChallenges();

        CommandDumbThing.createCommands();
        ToolBlockCommands.createCommands();
        CommandSelfOrUser.createCommands();
        ToggleCommand.createCommands();
        CommandLeaderboard.createCommands();
        UserMapCommand.createCommands();
        UserMapTopCommand.createCommands();
        UseCmdCommand.createCommands();

        // Guilds
        GuildCommands.createCommands();

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

        // Waypoint commands
        new CommandVisit();
        new CommandWaypoints();
        new CommandMoveIn();
        new CommandInvite();
        new CommandHomeWaypoint();
        new CommandCreateWaypoint();

        //emote, other emotes are initialized by cosmetics in CosmeticEmotes.init()
        new EmotePog();

        //help commands
        HelpCommand.createCommands();
    }

    /* ----------------------------- UTILITY ------------------------------ */

    public static void ensureIndexValid(int index, int size)
            throws CommandSyntaxException
    {
        if (index > size) {
            throw Exceptions.invalidIndex(index, size);
        }
    }

    public static void ensurePageValid(int page, int pageSize, int size)
            throws CommandSyntaxException
    {
        if (size == 0) {
            throw Exceptions.NOTHING_TO_LIST;
        }

        var max = PageEntryIterator.getMaxPage(pageSize, size);

        if (page >= max) {
            throw Exceptions.invalidPage(page + 1, max);
        }
    }

    public static ItemStack getHeldItem(Player player)
            throws CommandSyntaxException
    {
        var item = player.getInventory().getItemInMainHand();

        if (ItemStacks.isEmpty(item)) {
            throw Exceptions.MUST_HOLD_ITEM;
        }

        return item;
    }

    public static String findInput(String argument, CommandContext<?> context) {
        for (var parsedNode: context.getNodes()) {
            if (parsedNode.getNode().getName().equals(argument)) {
                return parsedNode.getRange().get(context.getInput());
            }
        }

        return null;
    }
}