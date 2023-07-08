package net.forthecrown.core.commands.tpa;

import static net.forthecrown.text.Messages.REQUEST_ACCEPTED;
import static net.forthecrown.text.Messages.REQUEST_CANCELLED;
import static net.forthecrown.text.Messages.REQUEST_DENIED;
import static net.forthecrown.text.Messages.requestAccepted;
import static net.forthecrown.text.Messages.requestCancelled;
import static net.forthecrown.text.Messages.requestDenied;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.CoreConfig;
import net.forthecrown.core.CorePlugin;
import net.forthecrown.user.User;
import net.forthecrown.user.UserTeleport;
import net.forthecrown.utils.Tasks;
import org.bukkit.scheduler.BukkitTask;

@RequiredArgsConstructor
public class TeleportRequest {

  /**
   * The sender of the request
   */
  @Getter
  private final User sender;

  /**
   * The target of the request
   */
  @Getter
  private final User target;

  /**
   * Determines whether the sender or target will be teleporting.
   * <p>
   * If <code>tpaHere</code> == true, the target of this request will be teleporting, otherwise the
   * sender will be teleporting
   */
  @Getter
  private final boolean tpaHere;

  private BukkitTask expiryTask;

  /**
   * Executes a teleport request.
   * <p>
   * Creates a teleport request instance with the given parameters and calls its {@link #run()}
   * function.
   *
   * @param sender  The sender of the request
   * @param target  The recipient of the request
   * @param tpaHere True, if sender wants the target to teleport to them, false if it's the other
   *                way round
   */
  public static void run(User sender, User target, boolean tpaHere) {
    new TeleportRequest(sender, target, tpaHere).run();
  }

  /**
   * Adds this request to both users' teleport request lists and starts the expiry task to cancel
   * this request after {@link CoreConfig#getTpaExpireTime()} ticks.
   */
  public void run() {
    //Add the request
    TeleportRequests.add(this);

    //Start the countdown
    CoreConfig config = CorePlugin.plugin().getFtcConfig();
    expiryTask = Tasks.runLaterAsync(this::stop, config.getTpaExpireTime());
  }

  /**
   * Accepts the TPA request, tells both uses the request was accepted and starts the
   * {@link UserTeleport} to teleport either the sender or target to the other user.
   */
  public void accept() {
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
    sender.sendMessage(requestDenied(target));
    target.sendMessage(REQUEST_DENIED);

    stop();
  }

  /**
   * Tells the users this request was cancelled and calls {@link #stop()} to stop this request
   */
  public void cancel() {
    sender.sendMessage(REQUEST_CANCELLED);
    target.sendMessage(requestCancelled(sender));

    stop();
  }

  /**
   * Stops this request, removes this request from both users' request lists and cancels the expiry
   * task.
   * <p>
   * This method does not tell either user the request was stopped, use {@link #cancel()} for that
   */
  public void stop() {
    // Remove from request lists
    TeleportRequests.remove(this);

    // Cancel task
    expiryTask = Tasks.cancel(expiryTask);
  }

  @Override
  public String toString() {
    return "TeleportRequest{" +
        "sender=" + sender.getName() +
        ", receiver=" + target.getName() +
        ", tpaHere=" + tpaHere +
        '}';
  }
}
