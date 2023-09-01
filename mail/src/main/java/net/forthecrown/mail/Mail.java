package net.forthecrown.mail;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Consumer;
import net.forthecrown.mail.command.Page;
import net.forthecrown.text.PlayerMessage;
import net.forthecrown.text.ViewerAwareMessage;
import net.forthecrown.user.User;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public interface Mail {

  long NULL_ID = 0;

  static Builder builder() {
    return new MailBuilder();
  }

  static void sendOrMail(User target, Component message) {
    if (target.isOnline()) {
      target.sendMessage(message);
      return;
    }

    builder().target(target).message(message).sendQuietly();
  }

  UUID getSender();

  UUID getTarget();

  ViewerAwareMessage getMessage();

  MessageType getMessageType();

  @Nullable
  Attachment getAttachment();

  @Nullable
  Instant getClaimDate();

  @Nullable
  Instant getSentDate();

  @Nullable
  Instant getReadDate();

  long getMailId();

  default boolean hasAttachment() {
    var attch = getAttachment();
    return attch != null && !attch.isEmpty();
  }

  default boolean wasSent() {
    return getSentDate() != null;
  }

  default boolean isRead() {
    return getReadDate() != null;
  }

  default boolean isAdminMessage() {
    return getMessageType() == MessageType.REGULAR || getSender() == null;
  }

  /**
   * Tests if this mail message can be omitted, if it is a very old message.
   * <p>
   * The method name is not accurate, as this method only tests if it is unread and has no unclaimed
   * attachment. If it does have both of those things, that means the message can be ignored if it
   * is past an arbitrary cutoff date
   *
   * @return {@code true}, if this message can be omitted, {@code false} otherwise
   */
  boolean canBeOmitted();

  boolean isDeleted();

  Component displayText(Audience viewer, Page page);

  Component formatMessage(Audience viewer);

  Component infoButton(Audience viewer);

  Component deleteButton(Audience viewer, Page page);

  Component claimButton(Audience viewer, Page page);

  Component readButton(Audience viewer, Page page);

  Component metadataText(Audience viewer);
  
  void delete();

  boolean toggleRead();

  void claimAttachment(Player player) throws CommandSyntaxException;

  boolean isSenderVisible();

  enum MessageType {
    PLAYER, REGULAR
  }

  interface Builder {

    Builder sender(UUID uuid);

    default Builder sender(Player player) {
      return sender(player.getUniqueId());
    }

    default Builder sender(User user) {
      return sender(user.getUniqueId());
    }

    Builder target(UUID uuid);

    default Builder target(Player player) {
      return target(player.getUniqueId());
    }

    default Builder target(User user) {
      return target(user.getUniqueId());
    }

    Builder hideSender(boolean hideSender);

    Builder message(Component message);

    Builder message(PlayerMessage message);

    default Builder attachment(Consumer<Attachment.Builder> consumer) {
      var builder = Attachment.builder();
      consumer.accept(builder);
      return attachment(builder.build());
    }

    Builder attachment(Attachment attachment);

    Mail build() throws IllegalStateException;

    default Mail send() throws IllegalStateException {
      Mail built = build();
      MailService.service().send(built);
      return built;
    }

    default Mail sendQuietly() throws IllegalStateException {
      Mail built = build();
      MailService.service().sendQuietly(built);
      return built;
    }
  }
}
