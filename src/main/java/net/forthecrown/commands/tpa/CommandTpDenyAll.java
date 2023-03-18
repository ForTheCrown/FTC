package net.forthecrown.commands.tpa;

import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Messages;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;

public class CommandTpDenyAll extends FtcCommand {

  public CommandTpDenyAll() {
    super("tpdenyall");

    setPermission(Permissions.TPA);
    setDescription("Denies all incoming tpa requests");
    simpleUsages();

    register();
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command.executes(c -> {
      User user = getUserSender(c);

      if (user.getInteractions().getIncoming().isEmpty()) {
        throw Exceptions.NO_TP_REQUESTS;
      }

      user.getInteractions().clearIncoming();
      user.sendMessage(Messages.TPA_DENIED_ALL);
      return 0;
    });
  }
}