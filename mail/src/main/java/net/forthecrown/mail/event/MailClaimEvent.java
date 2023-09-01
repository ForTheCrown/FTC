package net.forthecrown.mail.event;

import lombok.Getter;
import net.forthecrown.mail.Attachment;
import net.forthecrown.user.User;
import net.forthecrown.user.event.UserEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class MailClaimEvent extends UserEvent implements Cancellable {

  @Getter
  private static final HandlerList handlerList = new HandlerList();

  private final Attachment attachment;

  private Component denyReason;
  private boolean denied;

  public MailClaimEvent(User user, Attachment attachment) {
    super(user);
    this.attachment = attachment;
  }

  public Attachment getAttachment() {
    return attachment;
  }

  @Override
  public void setCancelled(boolean cancel) {
    this.denied = cancel;
  }

  @Override
  public boolean isCancelled() {
    return denied;
  }

  public void setDenyReason(Component denyReason) {
    this.denyReason = denyReason;
  }

  public Component getDenyReason() {
    return denyReason;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return handlerList;
  }
}
