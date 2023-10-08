package net.forthecrown.core.commands;

import net.forthecrown.command.Commands;
import net.forthecrown.command.CurrencyCommand;
import net.forthecrown.command.UserMapTopCommand;
import net.forthecrown.core.CorePlugin;
import net.forthecrown.core.commands.admin.CommandAlts;
import net.forthecrown.core.commands.admin.CommandBroadcast;
import net.forthecrown.core.commands.admin.CommandCooldown;
import net.forthecrown.core.commands.admin.CommandFtcCore;
import net.forthecrown.core.commands.admin.CommandGameMode;
import net.forthecrown.core.commands.admin.CommandGetOffset;
import net.forthecrown.core.commands.admin.CommandGetPos;
import net.forthecrown.core.commands.admin.CommandHologram;
import net.forthecrown.core.commands.admin.CommandInvStore;
import net.forthecrown.core.commands.admin.CommandLaunch;
import net.forthecrown.core.commands.admin.CommandMakeAward;
import net.forthecrown.core.commands.admin.CommandMemory;
import net.forthecrown.core.commands.admin.CommandPlayerTime;
import net.forthecrown.core.commands.admin.CommandSign;
import net.forthecrown.core.commands.admin.CommandSkull;
import net.forthecrown.core.commands.admin.CommandSpecificGameMode;
import net.forthecrown.core.commands.admin.CommandSpeed;
import net.forthecrown.core.commands.admin.CommandTab;
import net.forthecrown.core.commands.admin.CommandTeleport;
import net.forthecrown.core.commands.admin.CommandTeleportExact;
import net.forthecrown.core.commands.admin.CommandTellRawF;
import net.forthecrown.core.commands.admin.CommandTime;
import net.forthecrown.core.commands.admin.CommandTimeFields;
import net.forthecrown.core.commands.admin.CommandTop;
import net.forthecrown.core.commands.admin.CommandVanish;
import net.forthecrown.core.commands.admin.CommandWorld;
import net.forthecrown.core.commands.docs.CommandDocGen;
import net.forthecrown.core.commands.home.CommandDeleteHome;
import net.forthecrown.core.commands.home.CommandHome;
import net.forthecrown.core.commands.home.CommandHomeList;
import net.forthecrown.core.commands.home.CommandSetHome;
import net.forthecrown.core.commands.item.ItemModCommands;
import net.forthecrown.core.commands.tpa.CommandTpDeny;
import net.forthecrown.core.commands.tpa.CommandTpDenyAll;
import net.forthecrown.core.commands.tpa.CommandTpaAccept;
import net.forthecrown.core.commands.tpa.CommandTpaCancel;
import net.forthecrown.core.commands.tpa.CommandTpask;
import net.forthecrown.core.commands.tpa.CommandTpaskHere;
import net.forthecrown.core.user.UserServiceImpl;
import net.forthecrown.grenadier.annotations.AnnotatedCommandContext;
import net.forthecrown.text.UnitFormat;
import net.forthecrown.user.Users;
import net.forthecrown.user.currency.Currency;
import net.kyori.adventure.text.Component;

public final class CoreCommands {
  private CoreCommands() {}

  public static void createCommands(CorePlugin plugin) {
    new CommandHelp();

    new CommandProfile();

    // Tpa
    new CommandTpaAccept();
    new CommandTpaCancel();
    new CommandTpask();
    new CommandTpaskHere();
    new CommandTpaCancel();
    new CommandTpDeny();
    new CommandTpDenyAll();

    // Admin commands
    new CommandWorld();
    new CommandTop();
    new CommandTime();
    new CommandTeleportExact();
    new CommandSpeed();
    new CommandSkull();
    new CommandSign();
    new CommandPlayerTime();
    new CommandMemory();
    new CommandMakeAward();
    new CommandLaunch();
    new CommandInvStore();
    new CommandHologram();
    new CommandGetPos();
    new CommandGetOffset();
    new CommandGameMode();
    new CommandCooldown();
    new CommandBroadcast();
    new CommandDocGen();
    new CommandTellRawF();

    new CommandSay();
    new CommandNickname();
    new CommandNear();
    new CommandMe();
    new CommandList();
    new CommandHat();
    new CommandBack();
    new CommandAfk();
    new CommandSettings();
    new CommandTell();
    new CommandReply();
    new CommandSuicide();
    new CommandRoll();
    new CommandWild(plugin.getWild());

    new CommandWithdraw();
    new CommandDeposit();
    new CommandPay();

    new CommandHome();
    new CommandHomeList();
    new CommandDeleteHome();
    new CommandSetHome();

    CommandSelfOrUser.createCommands();
    CommandDumbThing.createCommands();
    CommandSpecificGameMode.createCommands();
    ToolBlockCommands.createCommands();
    ItemModCommands.createCommands();

    createMapTopCommands();
    createCurrencyCommands();

    AnnotatedCommandContext ctx = Commands.createAnnotationContext();
    ctx.registerCommand(new CommandTeleport());
    ctx.registerCommand(new CommandVanish());
    ctx.registerCommand(new CommandFtcCore());
    ctx.registerCommand(new CommandTab());
    ctx.registerCommand(new CommandAlts());
    ctx.registerCommand(new CommandTimeFields());
  }

  static void createCurrencyCommands() {
    UserServiceImpl users = (UserServiceImpl) Users.getService();
    var currencies = users.getCurrencies();

    currencies.get("rhines").ifPresent(currency -> {
      new CurrencyCommand("balance", currency, "bal", "bank", "cash", "money", "ebal");
    });

    currencies.get("gems").ifPresent(currency -> {
      new CurrencyCommand("gems", currency);
    });

    new CurrencyCommand("votes", Currency.wrap("Vote", users.getVotes()));
  }

  static void createMapTopCommands() {
    UserServiceImpl users = (UserServiceImpl) Users.getService();

    new UserMapTopCommand(
        "baltop",
        users.getBalances(),
        UnitFormat::rhines,
        Component.text("Top balances"),
        "balancetop", "banktop", "topbals", "topbalances"
    );

    new UserMapTopCommand(
        "gemtop",
        users.getGems(),
        UnitFormat::gems,
        Component.text("Gem Top"),
        "topgems"
    );

    new UserMapTopCommand(
        "topvoters",
        users.getVotes(),
        UnitFormat::votes,
        Component.text("Top voters"),
        "votetop"
    );

    new UserMapTopCommand(
        "playtimetop",
        users.getPlaytime(),
        UnitFormat::playTime,
        Component.text("Top by playtime"),
        "nolifetop", "topplayers"
    );
  }
}
