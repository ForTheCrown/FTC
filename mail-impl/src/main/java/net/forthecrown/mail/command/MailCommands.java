package net.forthecrown.mail.command;

import static net.kyori.adventure.text.Component.text;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import net.forthecrown.Loggers;
import net.forthecrown.command.Commands;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.arguments.UserParseResult;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.options.ArgumentOption;
import net.forthecrown.grenadier.types.options.Options;
import net.forthecrown.grenadier.types.options.OptionsArgument;
import net.forthecrown.mail.Mail;
import net.forthecrown.mail.MailPermissions;
import net.forthecrown.mail.MailService;
import net.forthecrown.mail.Page;
import net.forthecrown.mail.SentDateComparator;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

public class MailCommands {

  private static final Logger LOGGER = Loggers.getLogger();

  public static final ArgumentOption<Integer> PAGE
      = Options.argument(IntegerArgumentType.integer(1))
      .setLabel("page")
      .setDefaultValue(1)
      .build();

  public static final ArgumentOption<Integer> PAGE_SIZE
      = Options.argument(IntegerArgumentType.integer(5, 20))
      .setLabel("page_size")
      .setDefaultValue(10)
      .build();

  public static final ArgumentOption<Long> MESSAGE_ID
      = Options.argument(LongArgumentType.longArg())
      .setLabel("message_id")
      .build();

  public static final ArgumentOption<UserParseResult> PLAYER
      = Options.argument(Arguments.USER, "player");

  public static final OptionsArgument OPTIONS = OptionsArgument.builder()
      .addOptional(PAGE)
      .addOptional(PAGE_SIZE)
      .addRequired(MESSAGE_ID)
      .addOptional(PLAYER)
      .build();

  public static final UnaryOperator<String> NEW_LINES
      = string -> string.replaceAll("(?:\\\\n)+", "\n");

  public static String getReadToggleCommand(Mail mail, Page page) {
    return String.format("/mail mark_read message_id=%s %s", mail.getMailId(), page);
  }

  public static String getClaimCommand(Mail mail, Page page) {
    return String.format("/mail claim message_id=%s %s", mail.getMailId(), page);
  }

  public static String getDeleteCommand(Mail mail, Page page) {
    return String.format("/mail delete message_id=%s %s", mail.getMailId(), page);
  }

  public static void createCommands(MailService service) {
    var ctx = Commands.createAnnotationContext();
    ctx.registerCommand(new MailCommand(service));
  }

  static void sendMail(CommandSource source, MailTargets targets, Consumer<User> sender)
      throws CommandSyntaxException
  {
    if (targets == MailTargets.ALL) {
      source.sendSuccess(text("Sending mail to all users"));
    }

    targets.getTargets(source).whenComplete((users, throwable) -> {
      if (throwable != null) {
        LOGGER.error("Error getting mail targets", throwable);
        return;
      }

      for (User user : users) {
        LOGGER.debug("Sending mail to {}", user);

        try {
          sender.accept(user);
        } catch (Throwable t) {
          LOGGER.error("Error sending mail to {}", user, t);
        }
      }

      if (users.size() == 1) {
        var user = users.iterator().next();
        source.sendSuccess(
            Text.format("Sent mail to &7{0, user}&r.", NamedTextColor.GRAY, user)
        );
      } else {
        source.sendSuccess(
            Text.format("Sent mail to &e{0, number} players&r.", NamedTextColor.GRAY, users.size())
        );
      }
    });
  }

  static Mail.Builder createBuilder(CommandSource source) {
    var builder = Mail.builder();
    if (source.isPlayer()) {
      builder.sender(source.asPlayerOrNull());
    }
    return builder;
  }

  public static List<Mail> getMailList(MailService service, User user, boolean includeDeleted) {
    return service.getMail(user.getUniqueId(), null, includeDeleted)
        .sorted(SentDateComparator.INSTANCE)
        .collect(ObjectArrayList.toList());
  }

  public static void validateAccess(CommandSource source, Mail mail) throws CommandSyntaxException {
    if (!source.isPlayer() || source.hasPermission(MailPermissions.MAIL_ADMIN)) {
      return;
    }

    var player = source.asPlayerOrNull();
    if (mail.getTarget().equals(player.getUniqueId())) {
      return;
    }

    throw Exceptions.create("Not allowed to access that message");
  }
}
