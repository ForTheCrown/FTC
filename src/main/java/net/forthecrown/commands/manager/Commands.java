package net.forthecrown.commands.manager;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.CommandAfk;
import net.forthecrown.commands.CommandBack;
import net.forthecrown.commands.CommandChallenges;
import net.forthecrown.commands.CommandCosmetics;
import net.forthecrown.commands.CommandDumbThing;
import net.forthecrown.commands.CommandHat;
import net.forthecrown.commands.CommandIgnore;
import net.forthecrown.commands.CommandIgnoreList;
import net.forthecrown.commands.CommandLeaderboard;
import net.forthecrown.commands.CommandLeave;
import net.forthecrown.commands.CommandList;
import net.forthecrown.commands.CommandMail;
import net.forthecrown.commands.CommandMe;
import net.forthecrown.commands.CommandNear;
import net.forthecrown.commands.CommandNickname;
import net.forthecrown.commands.CommandNpcDialogue;
import net.forthecrown.commands.CommandProfile;
import net.forthecrown.commands.CommandRank;
import net.forthecrown.commands.CommandReply;
import net.forthecrown.commands.CommandSay;
import net.forthecrown.commands.CommandSelfOrUser;
import net.forthecrown.commands.CommandSetSpawn;
import net.forthecrown.commands.CommandSettings;
import net.forthecrown.commands.CommandSuicide;
import net.forthecrown.commands.CommandTell;
import net.forthecrown.commands.CommandWild;
import net.forthecrown.commands.ToggleCommand;
import net.forthecrown.commands.ToolBlockCommands;
import net.forthecrown.commands.UserMapCommand;
import net.forthecrown.commands.UserMapTopCommand;
import net.forthecrown.commands.admin.CommandBroadcast;
import net.forthecrown.commands.admin.CommandCooldown;
import net.forthecrown.commands.admin.CommandDungeons;
import net.forthecrown.commands.admin.CommandEndOpener;
import net.forthecrown.commands.admin.CommandFtcStruct;
import net.forthecrown.commands.admin.CommandFtcVersion;
import net.forthecrown.commands.admin.CommandGameMode;
import net.forthecrown.commands.admin.CommandGetOffset;
import net.forthecrown.commands.admin.CommandGetPos;
import net.forthecrown.commands.admin.CommandGift;
import net.forthecrown.commands.admin.CommandHologram;
import net.forthecrown.commands.admin.CommandIllegalWorlds;
import net.forthecrown.commands.admin.CommandInvStore;
import net.forthecrown.commands.admin.CommandJoinInfo;
import net.forthecrown.commands.admin.CommandLaunch;
import net.forthecrown.commands.admin.CommandMakeAward;
import net.forthecrown.commands.admin.CommandMemory;
import net.forthecrown.commands.admin.CommandNPC;
import net.forthecrown.commands.admin.CommandPlayerTime;
import net.forthecrown.commands.admin.CommandResourceWorld;
import net.forthecrown.commands.admin.CommandRoyalSword;
import net.forthecrown.commands.admin.CommandScripts;
import net.forthecrown.commands.admin.CommandSign;
import net.forthecrown.commands.admin.CommandSkull;
import net.forthecrown.commands.admin.CommandSpecificGameMode;
import net.forthecrown.commands.admin.CommandSpeed;
import net.forthecrown.commands.admin.CommandStaffChat;
import net.forthecrown.commands.admin.CommandStructFunction;
import net.forthecrown.commands.admin.CommandSudo;
import net.forthecrown.commands.admin.CommandTeleport;
import net.forthecrown.commands.admin.CommandTeleportExact;
import net.forthecrown.commands.admin.CommandTime;
import net.forthecrown.commands.admin.CommandTop;
import net.forthecrown.commands.admin.CommandVanish;
import net.forthecrown.commands.admin.CommandWorld;
import net.forthecrown.commands.admin.SaveReloadCommands;
import net.forthecrown.commands.click.CommandClickableText;
import net.forthecrown.commands.economy.CommandBecomeBaron;
import net.forthecrown.commands.economy.CommandDeposit;
import net.forthecrown.commands.economy.CommandEditShop;
import net.forthecrown.commands.economy.CommandPay;
import net.forthecrown.commands.economy.CommandSell;
import net.forthecrown.commands.economy.CommandShop;
import net.forthecrown.commands.economy.CommandShopHistory;
import net.forthecrown.commands.economy.CommandWithdraw;
import net.forthecrown.commands.emotes.EmotePet;
import net.forthecrown.commands.emotes.EmotePog;
import net.forthecrown.commands.guild.GuildCommands;
import net.forthecrown.commands.help.CommandHelp;
import net.forthecrown.commands.help.FtcHelpMap;
import net.forthecrown.commands.help.HelpCommand;
import net.forthecrown.commands.home.CommandDeleteHome;
import net.forthecrown.commands.home.CommandHome;
import net.forthecrown.commands.home.CommandHomeList;
import net.forthecrown.commands.home.CommandSetHome;
import net.forthecrown.commands.item.ItemModCommands;
import net.forthecrown.commands.markets.CommandMarket;
import net.forthecrown.commands.markets.CommandMarketAppeal;
import net.forthecrown.commands.markets.CommandMarketEditing;
import net.forthecrown.commands.markets.CommandMarketWarning;
import net.forthecrown.commands.markets.CommandMergeShop;
import net.forthecrown.commands.markets.CommandShopTrust;
import net.forthecrown.commands.markets.CommandTransferShop;
import net.forthecrown.commands.markets.CommandUnclaimShop;
import net.forthecrown.commands.markets.CommandUnmerge;
import net.forthecrown.commands.marriage.CommandDivorce;
import net.forthecrown.commands.marriage.CommandMarriageAccept;
import net.forthecrown.commands.marriage.CommandMarriageChat;
import net.forthecrown.commands.marriage.CommandMarriageDeny;
import net.forthecrown.commands.marriage.CommandMarry;
import net.forthecrown.commands.punish.CommandNotes;
import net.forthecrown.commands.punish.CommandPunish;
import net.forthecrown.commands.punish.CommandSeparate;
import net.forthecrown.commands.punish.CommandSmite;
import net.forthecrown.commands.punish.PunishmentCommand;
import net.forthecrown.commands.test.TestCommands;
import net.forthecrown.commands.tpa.CommandTpCancel;
import net.forthecrown.commands.tpa.CommandTpDeny;
import net.forthecrown.commands.tpa.CommandTpDenyAll;
import net.forthecrown.commands.tpa.CommandTpaAccept;
import net.forthecrown.commands.tpa.CommandTpaCancel;
import net.forthecrown.commands.tpa.CommandTpask;
import net.forthecrown.commands.tpa.CommandTpaskHere;
import net.forthecrown.commands.usables.InteractableCommands;
import net.forthecrown.commands.usables.UseCmdCommand;
import net.forthecrown.commands.user.UserCommands;
import net.forthecrown.commands.waypoint.CommandCreateWaypoint;
import net.forthecrown.commands.waypoint.CommandHomeWaypoint;
import net.forthecrown.commands.waypoint.CommandInvite;
import net.forthecrown.commands.waypoint.CommandListWaypoints;
import net.forthecrown.commands.waypoint.CommandMoveIn;
import net.forthecrown.commands.waypoint.CommandVisit;
import net.forthecrown.commands.waypoint.CommandWaypoints;
import net.forthecrown.core.FTC;
import net.forthecrown.core.module.OnEnable;
import net.forthecrown.grenadier.Readers;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.text.format.page.PageEntryIterator;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.math.GenericMath;

public final class Commands {
  private Commands() {}

  public static final StringReader EMPTY_READER = new StringReader("");

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
    new CommandEndOpener();
    new CommandSpeed();
    new CommandSign();
    new CommandRoyalSword();
    new CommandGameMode();
    new CommandWorld();
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
    new CommandInvStore();
    new CommandCooldown();
    new CommandFtcVersion();

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
    new CommandNpcDialogue();

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
    new CommandSell();

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
    new CommandListWaypoints();

    //emote, other emotes are initialized by cosmetics in CosmeticEmotes.init()
    new EmotePog();
    new EmotePet();

    //help commands
    HelpCommand.createCommands();

    new CommandHelp();
    FtcHelpMap.getInstance().update();
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
    for (var parsedNode : context.getNodes()) {
      if (parsedNode.getNode().getName().equals(argument)) {
        var inputLength = context.getInput().length();
        var range = parsedNode.getRange();

        StringRange clamped = new StringRange(
            GenericMath.clamp(range.getStart(), 0, inputLength),
            GenericMath.clamp(range.getEnd(),   0, inputLength)
        );

        return clamped.get(context.getInput());
      }
    }

    return null;
  }

  public static String optionallyQuote(String quote, String s) {
    for (var c: s.toCharArray()) {
      if (!StringReader.isAllowedInUnquotedString(c)) {
        return quote + s + quote;
      }
    }

    return s;
  }

  /**
   * Skips the given string in the given reader, if the given reader's remaining input starts with
   * the given string.
   *
   * @param reader The reader to move the cursor of
   * @param s      The string to skip
   */
  public static void skip(StringReader reader, String s) {
    if (!Readers.startsWithIgnoreCase(reader, s)) {
      return;
    }

    reader.setCursor(reader.getCursor() + s.length());
  }

  /**
   * Ensures that the given string reader is at the end of its input
   *
   * @param reader The reader to test
   * @throws CommandSyntaxException If the reader is not at the end of it's input
   */
  public static void ensureCannotRead(StringReader reader) throws CommandSyntaxException {
    if (reader.canRead()) {
      throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
          .dispatcherUnknownArgument()
          .createWithContext(reader);
    }
  }
}