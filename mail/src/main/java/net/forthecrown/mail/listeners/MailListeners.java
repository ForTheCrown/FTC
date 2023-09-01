package net.forthecrown.mail.listeners;

import net.forthecrown.events.Events;
import net.forthecrown.mail.MailService;

public class MailListeners {

  public static void registerAll(MailService service) {
    Events.register(new UserJoinListener(service));
  }
}
