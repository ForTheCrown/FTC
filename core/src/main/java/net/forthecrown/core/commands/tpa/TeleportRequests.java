package net.forthecrown.core.commands.tpa;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.request.RequestTable;
import net.forthecrown.command.request.RequestValidator;
import net.forthecrown.events.WorldAccessTestEvent;
import net.forthecrown.user.Properties;
import net.forthecrown.user.User;
import net.kyori.adventure.audience.Audience;
import org.bukkit.World;

public class TeleportRequests {

  @Getter
  private static final RequestTable<TeleportRequest> table;

  static {
    table = new RequestTable<>();
    table.setValidator(new TpaValidator());
  }

  public static void add(TeleportRequest request) {
    table.add(request);
  }

  public static void remove(TeleportRequest request) {
    table.remove(request);
  }

  public static TeleportRequest getOutgoing(User sender, User target) {
    return table.getOutgoing(sender, target);
  }

  public static TeleportRequest getIncoming(User target, User sender) {
    return table.getIncoming(sender, target);
  }

  public static boolean clearIncoming(User target) {
    return table.clearIncoming(target);
  }

  public static TeleportRequest latestIncoming(User target) {
    return table.latestIncoming(target);
  }

  public static TeleportRequest latestOutgoing(User sender) {
    return table.latestOutgoing(sender);
  }


  static class TpaValidator implements RequestValidator<TeleportRequest> {

    @Override
    public void validate(TeleportRequest request, Audience viewer) throws CommandSyntaxException {
      var sender = request.getSender();
      var target = request.getTarget();

      boolean tpaHere = request.isTpaHere();

      if (sender.equals(target)) {
        throw TpExceptions.CANNOT_TP_SELF;
      }

      if (!sender.get(Properties.TPA)) {
        throw TpExceptions.TPA_DISABLED_SENDER;
      }

      if (!target.get(Properties.TPA)) {
        throw TpExceptions.tpaDisabled(target);
      }

      TeleportRequest outgoing = table.getOutgoing(sender, target);
      TeleportRequest incoming = table.getIncoming(target, sender);

      if (outgoing != null || incoming != null) {
        throw Exceptions.requestAlreadySent(target);
      }

      World world = tpaHere ? sender.getWorld() : target.getWorld();

      CommandSyntaxException noMessage = tpaHere
          ? TpExceptions.CANNOT_TP_HERE
          : TpExceptions.cannotTpaTo(target);

      WorldAccessTestEvent.testOrThrow(sender.getPlayer(), world, noMessage);

      if (!tpaHere && !sender.canTeleport()) {
        throw TpExceptions.CANNOT_TP;
      }
    }
  }
}
