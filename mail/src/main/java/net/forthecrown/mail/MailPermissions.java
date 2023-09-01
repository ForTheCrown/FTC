package net.forthecrown.mail;

import static net.forthecrown.Permissions.register;

import org.bukkit.permissions.Permission;

public interface MailPermissions {

  Permission MAIL = register("ftc.mail");
  Permission MAIL_OTHERS = register(MAIL, "others");
  Permission MAIL_ITEMS = register(MAIL, "items");
  Permission MAIL_ADMIN = register(MAIL, "admin");
  Permission MAIL_ALL = register(MAIL, "send-all");
  Permission MAIL_FLAGS = register(MAIL, "flags");
}
