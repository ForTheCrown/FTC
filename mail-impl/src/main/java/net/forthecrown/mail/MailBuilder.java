package net.forthecrown.mail;

import java.time.Instant;
import java.util.UUID;
import net.forthecrown.mail.Mail.Builder;
import net.forthecrown.text.PlayerMessage;
import net.forthecrown.text.ViewerAwareMessage;
import net.kyori.adventure.text.Component;

class MailBuilder implements Builder {

  UUID sender;
  UUID target;

  ViewerAwareMessage message;

  AttachmentImpl attachment;

  Instant attachmentExpiry;

  boolean hideSender;

  @Override
  public Builder hideSender(boolean hideSender) {
    this.hideSender = hideSender;
    return this;
  }

  @Override
  public Builder sender(UUID uuid) {
    this.sender = uuid;
    return this;
  }

  @Override
  public Builder target(UUID uuid) {
    this.target = uuid;
    return this;
  }

  @Override
  public Builder message(Component message) {
    this.message = ViewerAwareMessage.wrap(message);
    return this;
  }

  @Override
  public Builder message(PlayerMessage message) {
    this.message = message;
    return this;
  }

  @Override
  public Builder attachment(Attachment attachment) {
    this.attachment = (AttachmentImpl) attachment;
    return this;
  }

  @Override
  public Builder attachmentExpiry(Instant instant) {
    this.attachmentExpiry = instant;
    return this;
  }

  @Override
  public MailImpl build() {
    if (target == null) {
      throw new IllegalStateException("No target set");
    }

    if (message == null) {
      throw new IllegalStateException("No message set");
    }

    return new MailImpl(this);
  }
}
