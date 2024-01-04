package net.forthecrown.mail;

import static net.forthecrown.command.Exceptions.create;
import static net.forthecrown.command.Exceptions.format;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.time.Instant;

public interface MailExceptions {

  CommandSyntaxException CLAIM_NOT_ALLOWED = create("Not allowed to claim mail");

  CommandSyntaxException ALREADY_CLAIMED = create("Mail items already claimed");

  static CommandSyntaxException attachmentExpired(Instant expirationDate) {
    if (expirationDate == null) {
      return create("Rewards expired");
    }

    return format(
        "Rewards expired on {0, date} ({0, time, -biggest -timestamp} ago",
        expirationDate
    );
  }
}
