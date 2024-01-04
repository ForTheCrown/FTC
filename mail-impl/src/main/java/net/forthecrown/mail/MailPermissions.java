package net.forthecrown.mail;

import net.forthecrown.Permissions;
import org.bukkit.permissions.Permission;

public interface MailPermissions {

  Permission MAIL = Permissions.register("ftc.mail");
  Permission MAIL_OTHERS = Permissions.register(MAIL, "others");
  Permission MAIL_ITEMS = Permissions.register(MAIL, "items");
  Permission MAIL_ADMIN = Permissions.register(MAIL, "admin");
  Permission MAIL_ALL = Permissions.register(MAIL, "send-all");
  Permission MAIL_FLAGS = Permissions.register(MAIL, "flags");
}
