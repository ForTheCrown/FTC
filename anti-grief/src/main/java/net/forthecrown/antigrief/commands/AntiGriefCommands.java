package net.forthecrown.antigrief.commands;

import net.forthecrown.antigrief.JailCell;
import net.forthecrown.antigrief.Punishments;
import net.forthecrown.command.arguments.RegistryArguments;

public final class AntiGriefCommands {
  private AntiGriefCommands() {}

  public static final RegistryArguments<JailCell> JAIL_CELL_ARG = new RegistryArguments<>(
      Punishments.get().getCells(),
      "Jail Cell"
  );

  public static void createCommands() {
    PunishmentCommand.createCommands();
    new CommandSeparate();
    new CommandNotes();
    new CommandPunish();
    new CommandSmite();
    new CommandStaffChat();
  }
}