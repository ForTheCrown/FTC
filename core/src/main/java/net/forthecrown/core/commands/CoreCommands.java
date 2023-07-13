package net.forthecrown.core.commands;

import net.forthecrown.Loggers;
import net.forthecrown.command.Commands;
import net.forthecrown.core.commands.tpa.CommandTpDeny;
import net.forthecrown.core.commands.tpa.CommandTpDenyAll;
import net.forthecrown.core.commands.tpa.CommandTpaAccept;
import net.forthecrown.core.commands.tpa.CommandTpaCancel;
import net.forthecrown.core.commands.tpa.CommandTpask;
import net.forthecrown.core.commands.tpa.CommandTpaskHere;
import net.forthecrown.grenadier.annotations.AnnotatedCommandContext;
import org.slf4j.Logger;

public final class CoreCommands {

  public static final Logger LOGGER = Loggers.getLogger();

  private CoreCommands() {}

  public static void createCommands() {
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

    AnnotatedCommandContext ctx = Commands.createAnnotationContext();
    ctx.registerCommand(new CommandTeleport());
  }
}
