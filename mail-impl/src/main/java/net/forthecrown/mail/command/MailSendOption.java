package net.forthecrown.mail.command;

import java.util.Collection;
import lombok.Getter;
import net.forthecrown.mail.Mail;
import net.forthecrown.mail.Mail.Builder;

@Getter
public enum MailSendOption {
  ANONYMOUS ('a') {
    @Override
    void apply(Builder builder) {
      builder.hideSender(true);
    }
  };

  private final char character;

  MailSendOption(char character) {
    this.character = character;
  }

  abstract void apply(Mail.Builder builder);

  static void apply(Collection<MailSendOption> options, Mail.Builder builder) {
    if (options == null || options.isEmpty()) {
      return;
    }

    for (MailSendOption option : options) {
      option.apply(builder);
    }
  }
}
