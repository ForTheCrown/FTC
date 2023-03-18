package net.forthecrown.commands.tpa;

import static net.forthecrown.commands.tpa.CommandTpask.checkPreconditions;
import static net.forthecrown.core.Messages.TPA_FORMAT_HERE;
import static net.forthecrown.core.Messages.requestSent;
import static net.forthecrown.core.Messages.tpaCancelButton;
import static net.forthecrown.core.Messages.tpaTargetMessage;

import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.TeleportRequest;
import net.forthecrown.user.User;
import org.bukkit.Sound;

public class CommandTpaskHere extends FtcCommand {

  public CommandTpaskHere() {
    super("tpaskhere");

    setAliases("tpahere", "eptahere", "etpaskhere");
    setDescription("Asks a player to teleport to them.");
    setPermission(Permissions.TPA_HERE);
    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   * Allows players to ask another player to teleport to them.
   *
   * Valid usages of command:
   * - /tpaskhere <player>
   *
   * Permissions used:
   * - ftc.tpahere
   *
   * Main Author: Julie
   * Edit by: Wout
   */

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("<player>", "Asks a <player> to teleport to you");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command.then(argument("player", Arguments.ONLINE_USER)
        .executes(c -> {
          User player = getUserSender(c);
          User target = Arguments.getUser(c, "player");
          checkPreconditions(player, target, true);

          player.sendMessage(requestSent(target, tpaCancelButton(target)));
          target.sendMessage(tpaTargetMessage(TPA_FORMAT_HERE, player));

          player.playSound(Sound.UI_TOAST_OUT, 2, 1.5f);
          target.playSound(Sound.UI_TOAST_IN, 2, 1.3f);

          TeleportRequest.run(player, target, true);
          return 0;
        })
    );
  }
}