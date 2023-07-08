package net.forthecrown.core.commands.tpa;

import static net.forthecrown.core.commands.tpa.TpMessages.TPA_FORMAT_NORMAL;
import static net.forthecrown.core.commands.tpa.TpMessages.tpaCancelButton;
import static net.forthecrown.core.commands.tpa.TpMessages.tpaTargetMessage;
import static net.forthecrown.text.Messages.requestSent;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.events.WorldAccessTestEvent;
import net.forthecrown.events.WorldAccessTestEvent.AccessResult;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.Properties;
import net.forthecrown.user.User;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

public class CommandTpask extends FtcCommand {

  public CommandTpask() {
    super("tpask");

    setAliases("tpa", "tprequest", "tpr", "etpa", "etpask");
    setDescription("Asks a to teleport to a player.");
    setPermission(TpPermissions.TPA);
    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   * Allows players to teleport to another player by asking them.
   *
   * Valid usages of command:
   * - /tpask <player>
   *
   * Permissions used:
   * - ftc.commands.tpa
   *
   * Main Author: Julie
   * Edit by: Wout
   */

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("<player>").addInfo("Asks to teleport to a <player>");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command.then(argument("player", Arguments.ONLINE_USER)
        .executes(c -> {
          User player = getUserSender(c);
          User target = Arguments.getUser(c, "player");
          checkPreconditions(player, target, false);

          player.sendMessage(requestSent(target, tpaCancelButton(target)));
          target.sendMessage(tpaTargetMessage(TPA_FORMAT_NORMAL, player));

          player.playSound(Sound.UI_TOAST_OUT, 2, 1.5f);
          target.playSound(Sound.UI_TOAST_IN, 2, 1.3f);

          TeleportRequest.run(player, target, false);
          return 0;
        })
    );
  }

  public static void checkPreconditions(User sender, User target, boolean tpaHere)
      throws CommandSyntaxException
  {
    if (sender.equals(target)) {
      throw TpExceptions.CANNOT_TP_SELF;
    }

    if (!sender.get(Properties.TPA)) {
      throw TpExceptions.TPA_DISABLED_SENDER;
    }

    if (!target.get(Properties.TPA)) {
      throw TpExceptions.tpaDisabled(target);
    }

    TeleportRequest outgoing = TeleportRequests.getOutgoing(sender, target);
    TeleportRequest incoming = TeleportRequests.getIncoming(target, sender);

    if (outgoing != null || incoming != null) {
      throw Exceptions.requestAlreadySent(target);
    }

    if (tpaHere) {
      testWorld(sender.getWorld(), sender.getPlayer(), TpExceptions.CANNOT_TP_HERE);
    } else {
      testWorld(target.getWorld(), sender.getPlayer(), TpExceptions.cannotTpaTo(target));
    }

    if (!tpaHere && !sender.canTeleport()) {
      throw TpExceptions.CANNOT_TP;
    }
  }

  public static void testWorld(World world, CommandSender sender, CommandSyntaxException exc)
      throws CommandSyntaxException
  {
    AccessResult result = WorldAccessTestEvent.testWorldAccess(sender, world);

    if (result.accessible()) {
      return;
    }

    if (result.denyReason() == null) {
      throw exc;
    }

    throw Exceptions.create(result.denyReason());
  }
}