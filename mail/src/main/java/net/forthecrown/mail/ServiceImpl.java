package net.forthecrown.mail;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;

import com.google.gson.JsonArray;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lombok.Getter;
import net.forthecrown.Loggers;
import net.forthecrown.mail.command.Page;
import net.forthecrown.mail.event.MailReceiveEvent;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializationHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

class ServiceImpl implements MailService {

  private static final Logger LOGGER = Loggers.getLogger();

  private final Path path;

  @Getter
  private final MailMap map;

  public ServiceImpl() {
    this.path = PathUtil.pluginPath("mail.json");
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
  public void send(Mail apiType) {
    sendQuietly(apiType);

    User target = Users.get(apiType.getTarget());

    if (!target.isOnline()) {
      LOGGER.debug("User {} is offline, cannot inform of mail", target);
      return;
    }

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

    LOGGER.debug("Informing {} of sent mail", target);
    target.sendMessage(builder.build());
  }

  @Override
  public void sendQuietly(Mail apiType) {
    MailImpl mail = (MailImpl) apiType;
    map.add(mail);
    mail.sentDate = Instant.now();

    LOGGER.debug("Added mail to map");

    User target = Users.get(mail.getTarget());

    var event = new MailReceiveEvent(target, mail);
    event.callEvent();

    LOGGER.debug("Post event call");
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
