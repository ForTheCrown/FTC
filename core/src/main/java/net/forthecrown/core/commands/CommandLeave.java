package net.forthecrown.core.commands;

import java.util.HashMap;
import java.util.Map;
import net.forthecrown.FtcServer.LeaveCommandListener;
import net.forthecrown.Permissions;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.grenadier.GrenadierCommand;

public class CommandLeave extends FtcCommand {

  public static final Map<String, LeaveCommandListener> listeners = new HashMap<>();

  public CommandLeave() {
    super("leave");

    setAliases("quit", "exit");
    setDescription("Use when you want to leave an event or server area");
    setPermission(Permissions.DEFAULT);

    register();
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command.executes(c -> {
      var user = getUserSender(c);

      for (LeaveCommandListener value : listeners.values()) {
        if (value.onUse(user)) {
          return 1;
        }
      }

      throw Exceptions.create("Not allowed to use here");
    });
  }
}
