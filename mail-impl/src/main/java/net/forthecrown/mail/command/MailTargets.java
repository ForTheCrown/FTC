package net.forthecrown.mail.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.Loggers;
import net.forthecrown.command.Exceptions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.mail.MailPermissions;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;

interface MailTargets {

  MailTargets ALL = source -> {
    if (!source.hasPermission(MailPermissions.MAIL_ALL)) {
      throw Exceptions.NO_PERMISSION;
    }

    var service = Users.getService();

    return service.loadAllUsers().whenComplete((users, throwable) -> {
      if (throwable == null) {
        return;
      }

      var logger = Loggers.getLogger();
      logger.error("Couldn't get all users", throwable);
    });
  };

  CompletableFuture<Collection<User>> getTargets(CommandSource source)
      throws CommandSyntaxException;
}
