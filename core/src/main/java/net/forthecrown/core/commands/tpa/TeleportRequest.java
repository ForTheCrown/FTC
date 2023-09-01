package net.forthecrown.core.commands.tpa;

import static net.forthecrown.core.commands.tpa.TpMessages.TPA_FORMAT_HERE;
import static net.forthecrown.core.commands.tpa.TpMessages.TPA_FORMAT_NORMAL;
import static net.forthecrown.core.commands.tpa.TpMessages.tpaCancelButton;
import static net.forthecrown.core.commands.tpa.TpMessages.tpaTargetMessage;
import static net.forthecrown.text.Messages.REQUEST_ACCEPTED;
import static net.forthecrown.text.Messages.REQUEST_CANCELLED;
import static net.forthecrown.text.Messages.REQUEST_DENIED;
import static net.forthecrown.text.Messages.requestAccepted;
import static net.forthecrown.text.Messages.requestCancelled;
import static net.forthecrown.text.Messages.requestDenied;
import static net.forthecrown.text.Messages.requestSent;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.time.Duration;
import java.util.UUID;
import lombok.Getter;
import net.forthecrown.command.request.PlayerRequest;
import net.forthecrown.core.CoreConfig;
import net.forthecrown.core.CorePlugin;
import net.forthecrown.user.User;
import net.forthecrown.user.UserTeleport;
import org.bukkit.Sound;

public class TeleportRequest extends PlayerRequest {

  /**
   * Determines whether the sender or target will be teleporting.
   * <p>
   * If <code>tpaHere</code> == true, the target of this request will be teleporting, otherwise the
   * sender will be teleporting
   */
  @Getter
  private final boolean tpaHere;

  public TeleportRequest(UUID senderId, UUID targetId, boolean tpaHere) {
    super(senderId, targetId);
    this.tpaHere = tpaHere;
  }

  /**
   * Executes a teleport request.
   *
   * @param sender  The sender of the request
   * @param target  The recipient of the request
   * @param tpaHere True, if sender wants the target to teleport to them, false if it's the other
   *                way round
   */
  public static void run(User sender, User target, boolean tpaHere) throws CommandSyntaxException {
    var request = new TeleportRequest(sender.getUniqueId(), target.getUniqueId(), tpaHere);
    var table = TeleportRequests.getTable();
    table.sendRequest(request);
  }

  @Override
  public void onBegin() {
    var sender = getSender();
    var target = getTarget();

    String targetFormat = tpaHere ? TPA_FORMAT_HERE : TPA_FORMAT_NORMAL;

    sender.sendMessage(requestSent(target, tpaCancelButton(target)));
    target.sendMessage(tpaTargetMessage(targetFormat, sender));

    sender.playSound(Sound.UI_TOAST_OUT, 2, 1.5f);
    target.playSound(Sound.UI_TOAST_IN, 2, 1.3f);
  }

  @Override
  protected Duration getExpiryDuration() {
    CoreConfig config = CorePlugin.plugin().getFtcConfig();
    return config.tpaExpireTime();
  }

  /**
   * Accepts the TPA request, tells both uses the request was accepted and starts the
   * {@link UserTeleport} to teleport either the sender or target to the other user.
   */
  public void accept() throws CommandSyntaxException {
    super.accept();

    var sender = getSender();
    var target = getTarget();

    sender.sendMessage(requestAccepted(target));
    target.sendMessage(REQUEST_ACCEPTED);

    // If tpaHere, target is teleporting,
    // otherwise it's the opposite
    User teleporting = tpaHere ? target : sender;
    User notTeleporting = tpaHere ? sender : target;

    teleporting.createTeleport(notTeleporting::getLocation, UserTeleport.Type.TPA)
        .start();

    stop();
  }

  /**
   * Tells the users the request was denied and calls {@link #stop()} to stop this request
   */
  public void deny() {
    var sender = getSender();
    var target = getTarget();

    sender.sendMessage(requestDenied(target));
    target.sendMessage(REQUEST_DENIED);

    stop();
  }

  /**
   * Tells the users this request was cancelled and calls {@link #stop()} to stop this request
   */
  public void cancel() {
    var sender = getSender();
    var target = getTarget();

    sender.sendMessage(REQUEST_CANCELLED);
    target.sendMessage(requestCancelled(sender));

    stop();
  }
}
