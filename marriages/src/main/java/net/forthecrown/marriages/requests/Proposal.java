package net.forthecrown.marriages.requests;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.time.Duration;
import java.util.UUID;
import net.forthecrown.command.request.PlayerRequest;
import net.forthecrown.marriages.MMessages;
import net.forthecrown.marriages.listeners.MarriageConfirmation;

public class Proposal extends PlayerRequest {

  public Proposal(UUID senderId, UUID targetId) {
    super(senderId, targetId);
  }

  @Override
  protected Duration getExpiryDuration() {
    return Duration.ofMinutes(5);
  }

  @Override
  public void onBegin() {
    var user = getSender();
    var target = getTarget();

    user.sendMessage(MMessages.proposeSender(target));
    target.sendMessage(MMessages.proposeTarget(user));
  }

  @Override
  public void cancel() {
    // Cancellation not supported
  }

  @Override
  public void accept() throws CommandSyntaxException {
    super.accept();

    var sender = getSender();
    var target = getTarget();

    sender.sendMessage(MMessages.proposeAcceptTarget(target));
    target.sendMessage(MMessages.proposeAcceptSender());

    MarriageConfirmation.setWaiting(sender, target);
  }

  @Override
  public void deny() {
    var sender = getSender();
    var target = getTarget();

    sender.sendMessage(MMessages.proposeDenySender(target));
    target.sendMessage(MMessages.proposeDenyTarget(sender));
  }
}
