package net.forthecrown.mail;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;

import com.google.gson.JsonArray;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Member;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lombok.Getter;
import net.forthecrown.Loggers;
import net.forthecrown.discord.FtcDiscord;
import net.forthecrown.mail.Attachment.Builder;
import net.forthecrown.mail.event.MailReceiveEvent;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializationHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

class ServiceImpl implements MailService {

  private static final Logger LOGGER = Loggers.getLogger();

  private final Path path;

  @Getter
  private final MailMap map;

  private final MailPlugin plugin;

  public ServiceImpl(MailPlugin plugin) {
    this.plugin = plugin;
    this.path = PathUtil.pluginPath(plugin, "mail.json");
    this.map = new MailMap(this);
  }

  public void save() {
    SerializationHelper.writeFile(path, file -> {
      JsonArray arr = new JsonArray();
      map.save(arr);
      JsonUtils.writeFile(arr, file);
    });
  }

  public void load() {
    SerializationHelper.readFile(path,
        file -> JsonUtils.readFile(file).getAsJsonArray(),
        map::load
    );
  }

  @Override
  public Builder attachmentBuilder() {
    return new AttachmentImpl.BuilderImpl();
  }

  @Override
  public Mail.Builder mailBuilder() {
    return new MailBuilder();
  }

  @Override
  public void send(Mail apiType, MailSendFlag... flags) {
    sendQuietly(apiType);

    User target = Users.get(apiType.getTarget());

    if (target.isOnline() && !ArrayUtils.contains(flags, MailSendFlag.NO_MESSAGE)) {
      Component senderName;

      if (apiType.getSender() != null) {
        var user = Users.get(apiType.getSender());
        senderName = user.displayName(target);
      } else {
        senderName = null;
      }

      var builder = text()
          .color(NamedTextColor.YELLOW)
          .append(text("You've got mail!"));

      if (senderName != null && apiType.isSenderVisible()) {
        builder.append(text(" Sender: ")).append(senderName);
      }

      builder
          .append(newline())
          .append(apiType.displayText(target, Page.EMPTY));

      target.sendMessage(builder.build());
    }

    if (plugin.getMailConfig().isDiscordForwardingAllowed()
        && target.get(MailPrefs.MAIL_TO_DISCORD)
        && !ArrayUtils.contains(flags, MailSendFlag.NO_DISCORD)
        && !target.isOnline()
    ) {
      forwardMailToDiscord((MailImpl) apiType, target);
    }
  }

  @Override
  public void sendQuietly(Mail apiType) {
    MailImpl mail = (MailImpl) apiType;
    map.add(mail);
    mail.sentDate = Instant.now();

    User target = Users.get(mail.getTarget());

    var event = new MailReceiveEvent(target, mail);
    event.callEvent();
  }

  private void forwardMailToDiscord(MailImpl mail, User target) {
    Optional<Member> opt = FtcDiscord.getUserMember(target);

    if (opt.isEmpty()) {
      return;
    }

    Member member = opt.get();

    member.getUser().openPrivateChannel()
        .submit()
        .whenComplete((privateChannel, openingError) -> {
          if (openingError != null) {
            LOGGER.error("Couldn't open private channel for {} (member.tag={})",
                target.getName(),
                member.getUser().getName(),
                openingError
            );

            return;
          }

          StringBuilder builder = new StringBuilder();
          builder.append("You've got mail!");

          if (mail.isSenderVisible() && mail.getSender() != null) {
            User user = Users.get(mail.getSender());
            String userName = FtcDiscord.getUserMember(user)
                .map(m -> user.getName() + " (Discord ID: " + m.getUser().getName() + ")")
                .orElse(user.getName());

            builder.append(" From ");
            builder.append(userName);
            builder.append(".");
          }

          AttachmentState attachState = mail.getAttachmentState();
          if (attachState == AttachmentState.UNCLAIMED) {
            builder.append(" Message has items!");
          }

          //
          // Rendering the text in ANSI is the most amount of effort I can be bothered to give. I
          // tried rendering text to a PNG but there was a thousand issues with that. And the text
          // wouldn't show up in the discord notification anyway, so ANSI is the best I can do to
          // both support colors and let you see the message in the discord notification
          //
          // If we wanted to do PNG route, we'd need to reverse engineer the minecraft renderer
          // because jesus christ, minecraft has 3 fonts and each is rendered differently and a
          // single font, rendered by AWT, can't replicate how Minecraft does it
          //

          builder.append("\n```ansi\n");

          Component text = mail.getMessage().create(target);
          AnsiText ansi = new AnsiText(builder,
              Message.MAX_CONTENT_LENGTH
                  - "\n```".length()
                  - AnsiText.SUFFIX.length()
          );
          Text.FLATTENER.flatten(text, ansi);

          builder.append("\n```");
          builder.append(AnsiText.SUFFIX);

          String fullString = builder.toString();
          LOGGER.debug("Rendered discord message:\n{}", AnsiText.replaceControlCodes(fullString));

          privateChannel.sendMessage(fullString).submit().whenComplete((message, sendingError) -> {
            if (sendingError != null) {
              LOGGER.error("Error sending discord mail forward!", sendingError);
              return;
            }

            LOGGER.debug("Forwarded in-game mail to {}'s discord (member.tag={})",
                target.getName(),
                member.getUser().getName()
            );
          });
        });
  }

  @Override
  public Stream<? extends Mail> getMail(
      UUID targetId,
      @Nullable Instant cutoffDate,
      boolean keepDeleted
  ) {
    Objects.requireNonNull(targetId, "No playerId");

    var mailList = map.getByTarget(targetId);

    if (mailList == null || mailList.isEmpty()) {
      return Stream.empty();
    }

    Stream<MailImpl> stream = mailList.stream();

    if (cutoffDate != null) {
      stream = stream.filter(new CutOffFilter(cutoffDate));
    }

    if (!keepDeleted) {
      stream = stream.filter(DeletedFilter.INSTANCE);
    }

    return stream;
  }

  @Override
  public Stream<? extends Mail> query(boolean nonDeleted) {
    return map.getMailStream(nonDeleted);
  }

  @Override
  public boolean hasUnread(UUID playerId) {
    return map.hasUnread(playerId);
  }

  @Override
  public Mail getMessage(long messageId) {
    return map.getById(messageId);
  }

  enum DeletedFilter implements Predicate<Mail> {
    INSTANCE;

    @Override
    public boolean test(Mail mail) {
      return !mail.isDeleted();
    }
  }

  record CutOffFilter(Instant date) implements Predicate<Mail> {

    @Override
    public boolean test(Mail mail) {
      if (mail.getSentDate() == null || !mail.canBeOmitted()) {
        return true;
      }

      return !mail.getSentDate().isBefore(date);
    }
  }
}
