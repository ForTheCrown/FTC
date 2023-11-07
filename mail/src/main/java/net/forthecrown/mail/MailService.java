package net.forthecrown.mail;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;
import net.forthecrown.BukkitServices;
import org.jetbrains.annotations.Nullable;

public interface MailService {

  static MailService service() {
    return BukkitServices.loadOrThrow(MailService.class);
  }

  void send(Mail mail);

  void sendQuietly(Mail mail);

  default Stream<? extends Mail> getMail(UUID targetId) {
    return getMail(targetId, null);
  }

  default Stream<? extends Mail> getMail(UUID targetId, @Nullable Instant cutoffDate) {
    return getMail(targetId, cutoffDate, false);
  }

  Stream<? extends Mail> getMail(UUID targetId, @Nullable Instant cutoffDate, boolean keepDeleted);

  Stream<? extends Mail> query(boolean nonDeleted);

  boolean hasUnread(UUID playerId);

  Mail getMessage(long messageId);
}
