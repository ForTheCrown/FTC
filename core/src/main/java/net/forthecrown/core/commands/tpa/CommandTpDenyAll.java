package net.forthecrown.core.commands.tpa;

import net.forthecrown.command.FtcCommand;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.User;

public class CommandTpDenyAll extends FtcCommand {

  public CommandTpDenyAll() {
    super("tpdenyall");

    setPermission(TpPermissions.TPA);
    setDescription("Denies all incoming tpa requests");
    simpleUsages();

    register();
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command.executes(c -> {
      User user = getUserSender(c);

      if (!TeleportRequests.clearIncoming(user)) {
        throw TpExceptions.NO_TP_REQUESTS;
      }

      user.sendMessage(TpMessages.TPA_DENIED_ALL);
      return 0;
    });
  }
}