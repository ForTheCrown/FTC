package net.forthecrown.commands.tpa;

import static net.forthecrown.commands.manager.Exceptions.CANNOT_TP;
import static net.forthecrown.commands.manager.Exceptions.CANNOT_TP_HERE;
import static net.forthecrown.commands.manager.Exceptions.CANNOT_TP_SELF;
import static net.forthecrown.commands.manager.Exceptions.END_CLOSED;
import static net.forthecrown.commands.manager.Exceptions.TPA_DISABLED_SENDER;
import static net.forthecrown.commands.manager.Exceptions.cannotTpaTo;
import static net.forthecrown.commands.manager.Exceptions.requestAlreadySent;
import static net.forthecrown.commands.manager.Exceptions.tpaDisabled;
import static net.forthecrown.core.Messages.TPA_FORMAT_NORMAL;
import static net.forthecrown.core.Messages.requestSent;
import static net.forthecrown.core.Messages.tpaCancelButton;
import static net.forthecrown.core.Messages.tpaTargetMessage;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.Worlds;
import net.forthecrown.core.config.EndConfig;
import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.user.TeleportRequest;
import net.forthecrown.user.User;
import net.forthecrown.user.data.UserInteractions;
import net.forthecrown.user.property.Properties;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

public class CommandTpask extends FtcCommand {

  public CommandTpask() {
    super("tpask");

    setAliases("tpa", "tprequest", "tpr", "etpa", "etpask");
    setDescription("Asks a to teleport to a player.");
    setPermission(Permissions.TPA);
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
    factory.usage("<player>")
        .addInfo("Asks to teleport to a <player>");
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
      throws CommandSyntaxException {
    if (sender.equals(target)) {
      throw CANNOT_TP_SELF;
    }

    if (!sender.get(Properties.TPA)) {
      throw TPA_DISABLED_SENDER;
    }

    if (!target.get(Properties.TPA)) {
      throw tpaDisabled(target);
    }

    UserInteractions i = sender.getInteractions();
    UserInteractions iTo = target.getInteractions();

    if (i.getOutgoing(target) != null
        || iTo.getIncoming(sender) != null
    ) {
      throw requestAlreadySent(target);
    }

    if (tpaHere) {
      testWorld(sender.getWorld(), sender.getPlayer(), CANNOT_TP_HERE);
    } else {
      testWorld(target.getWorld(), sender.getPlayer(), cannotTpaTo(target));
    }

    if (!tpaHere && !sender.canTeleport()) {
      throw CANNOT_TP;
    }
  }

  public static void testWorld(World world,
                               CommandSender sender,
                               CommandSyntaxException exc
  ) throws CommandSyntaxException {
    if (sender.hasPermission(Permissions.WORLD_BYPASS)) {
      return;
    }

    if (world.equals(Worlds.end()) && !EndConfig.open) {
      throw END_CLOSED;
    }

    if (isInvalidWorld(world)) {
      throw exc;
    }
  }

  public static boolean isInvalidWorld(World world) {
    return GeneralConfig.illegalWorlds.contains(world.getName());
  }
}