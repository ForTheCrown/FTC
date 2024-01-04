package net.forthecrown.mail;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.sql.Date;
import java.time.Instant;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.mail.command.MailCommands;
import net.forthecrown.text.Messages;
import net.forthecrown.text.PlayerMessage;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextJoiner;
import net.forthecrown.text.TextWriters;
import net.forthecrown.text.ViewerAwareMessage;
import net.forthecrown.text.ViewerAwareMessage.WrappedComponent;
import net.forthecrown.text.format.TextFormatTypes;
import net.forthecrown.text.placeholder.PlaceholderRenderer;
import net.forthecrown.text.placeholder.Placeholders;
import net.forthecrown.utils.Result;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
class MailImpl implements Mail {

  private static final String KEY_SENDER = "sender";
  private static final String KEY_TARGET = "target";
  private static final String KEY_MESSAGE = "message";
  private static final String KEY_ATTACHMENT = "attachment";
  private static final String KEY_CLAIM_DATE = "claim_date";
  private static final String KEY_SENT_DATE = "sent_date";
  private static final String KEY_READ_DATE = "read_date";
  private static final String KEY_ID = "message_id";
  private static final String KEY_HIDE_SENDER = "hide_sender";
  private static final String KEY_ATTACHMENT_EXPIRY = "attachment_expire_date";

  @Include
  private final UUID sender;

  @Include
  private final UUID target;

  @Include
  private final ViewerAwareMessage message;

  @Include
  private final AttachmentImpl attachment;

  @Setter
  private Instant claimDate;
  @Setter
  private Instant attachmentExpiry;

  ServiceImpl service;

  Instant sentDate;
  private Instant readDate;

  private boolean deleted;
  private boolean hideSender;

  @Include
  long mailId = NULL_ID;

  public MailImpl(MailBuilder builder) {
    this.sender           = builder.sender;
    this.target           = builder.target;
    this.message          = builder.message;
    this.attachment       = builder.attachment;
    this.hideSender       = builder.hideSender;
    this.attachmentExpiry = builder.attachmentExpiry;
  }
  
  /* --------------------------- SERIALIZATION ---------------------------- */
  
  public static Result<MailImpl> load(JsonElement element) {
    if (!element.isJsonObject()) {
      return Result.error("Not an object");
    }

    JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());
    MailBuilder builder = new MailBuilder();
    
    if (!json.has(KEY_TARGET)) {
      return Result.error("No '%s' value", KEY_TARGET);
    } else {
      JsonElement targetElem = json.get(KEY_TARGET);
      var idResult = loadUuid(targetElem);
      
      if (idResult.isError()) {
        return idResult.map(s -> "Couldn't load 'target': " + s).cast();
      }
      
      builder.target = idResult.getValue();
    }
    
    if (json.has(KEY_SENDER)) {
      var idResult = loadUuid(json.get(KEY_SENDER));

      if (idResult.isError()) {
        return idResult.mapError(string -> "Couldn't load 'sender': " + string).cast();
      }

      builder.sender = idResult.getValue();
    }
    
    if (!json.has(KEY_MESSAGE)) {
      return Result.error("No '%s' value", KEY_MESSAGE);
    } else {
      var messageResult = loadMessage(json.get(KEY_MESSAGE));
      
      if (messageResult.isError()) {
        return messageResult.mapError(string -> "Couldn't load message: " + string).cast();
      }
      
      builder.message = messageResult.getValue();
    }

    if (json.has(KEY_ATTACHMENT)) {
      var attachmentResult = AttachmentImpl.load(json.get(KEY_ATTACHMENT));

      if (attachmentResult.isError()) {
        return attachmentResult.mapError(string -> "Couldn't load attachment: " + string).cast();
      }

      builder.attachment = attachmentResult.getValue();
    }

    MailImpl built = builder.build();

    built.mailId            = json.getLong(KEY_ID, NULL_ID);
    built.hideSender        = json.getBool(KEY_HIDE_SENDER, false);
    built.claimDate         = JsonUtils.readInstant(json.get(KEY_CLAIM_DATE));
    built.sentDate          = JsonUtils.readInstant(json.get(KEY_SENT_DATE));
    built.readDate          = JsonUtils.readInstant(json.get(KEY_READ_DATE));
    built.attachmentExpiry  = JsonUtils.readInstant(json.get(KEY_ATTACHMENT_EXPIRY));

    return Result.success(built);
  }
  
  private static Result<UUID> loadUuid(JsonElement element) {
    try {
      return Result.success(JsonUtils.readUUID(element));
    } catch (IllegalArgumentException exc) {
      return Result.error(exc.getMessage());
    }
  }
  
  private static Result<ViewerAwareMessage> loadMessage(JsonElement element) {
    if (!element.isJsonObject()) {
      return Result.error("Not an object");
    }
    
    JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

    if (json.has("message")) {
      DataResult<PlayerMessage> dataResult 
          = PlayerMessage.load(new Dynamic<>(JsonOps.INSTANCE, element));
      
      if (dataResult.error().isPresent()) {
        return Result.error(dataResult.error().get().message());
      }
      
      return Result.success(dataResult.result().get());
    }
    
    try {
      return Result.success(JsonUtils.readText(element)).map(ViewerAwareMessage::wrap);
    } catch (JsonSyntaxException exc) {
      return Result.error(exc.getMessage());
    }
  }
  
  public JsonElement save() {
    JsonWrapper json = JsonWrapper.create();
    json.addUUID(KEY_TARGET, target);
    json.add(KEY_MESSAGE, saveMessage());
    json.add(KEY_ID, mailId);

    if (sender != null) {
      json.addUUID(KEY_SENDER, sender);
    }

    if (hasAttachment()) {
      json.add(KEY_ATTACHMENT, attachment.save());

      if (claimDate != null) {
        json.add(KEY_CLAIM_DATE, JsonUtils.writeInstant(claimDate));
      }

      if (attachmentExpiry != null) {
        json.add(KEY_ATTACHMENT_EXPIRY, JsonUtils.writeInstant(attachmentExpiry));
      }
    }

    if (sentDate != null) {
      json.add(KEY_SENT_DATE, JsonUtils.writeInstant(sentDate));
    }

    if (readDate != null) {
      json.add(KEY_READ_DATE, JsonUtils.writeInstant(readDate));
    }

    if (hideSender) {
      json.add(KEY_HIDE_SENDER, true);
    }

    return json.getSource();
  }

  private JsonElement saveMessage() {
    if (message instanceof PlayerMessage player) {
      return player.save(JsonOps.INSTANCE).getOrThrow(false, string -> {});
    }

    WrappedComponent wrapped = (WrappedComponent) message;
    return JsonUtils.writeText(wrapped.text());
  }
  
  /* ---------------------------------------------------------------------- */

  @Override
  public AttachmentState getAttachmentState() {
    if (attachment == null || attachment.isEmpty()) {
      return AttachmentState.NO_ATTACHMENT;
    }

    if (claimDate != null) {
      return AttachmentState.CLAIMED;
    }

    if (attachmentExpiry == null) {
      return AttachmentState.UNCLAIMED;
    }

    Instant now = Instant.now();
    if (now.isAfter(attachmentExpiry)) {
      return AttachmentState.EXPIRED;
    }

    return AttachmentState.UNCLAIMED;
  }

  @Override
  public MessageType getMessageType() {
    return message instanceof PlayerMessage
        ? MessageType.PLAYER
        : MessageType.REGULAR;
  }

  @Override
  public Component displayText(Audience viewer, Page page) {
    TextJoiner firstLineJoiner = TextJoiner.onSpace();
    firstLineJoiner.add(readButton(viewer, page));

    if (hasAttachment()) {
      firstLineJoiner.add(claimButton(viewer, page));
    }

    firstLineJoiner.add(deleteButton(viewer, page));
    firstLineJoiner.add(infoButton(viewer));

    Component firstLine  = firstLineJoiner.asComponent();
    Component secondLine = formatMessage(viewer);

    return Component.textOfChildren(firstLine, newline(), secondLine);
  }

  @Override
  public Component deleteButton(Audience viewer, Page page) {
    if (deleted) {
      return text("[Deleted]", NamedTextColor.DARK_GRAY)
          .hoverEvent(text("This message has already been deleted"));
    }

    return text("[Delete]", NamedTextColor.RED)
        .hoverEvent(text("Click to delete this from your inbox"))
        .clickEvent(ClickEvent.runCommand(MailCommands.getDeleteCommand(this, page)));
  }

  @Override
  public Component infoButton(Audience viewer) {
    return text("[Info]").hoverEvent(metadataText(viewer));
  }

  @Override
  public Component claimButton(Audience viewer, Page page) {
    if (!hasAttachment()) {
      return null;
    }

    AttachmentState attachState = getAttachmentState();
    AttachmentImpl attach = getAttachment();

    TextColor color;
    String text;
    String hover;

    switch (attachState) {
      case CLAIMED -> {
        color = NamedTextColor.GRAY;
        text = "Already claimed";
        hover = "Already claimed rewards";
      }

      case UNCLAIMED -> {
        color = NamedTextColor.AQUA;
        text = "Claim Rewards";
        hover = "Click to claim rewards!";
      }

      case EXPIRED -> {
        color = TextColor.color(157, 107, 107);
        text = "Rewards Expired";
        hover = "Rewards expired on " + Text.DATE_FORMAT.format(Date.from(attachmentExpiry));
      }

      default -> {
        // Not possible
        throw new AssertionError();
      }
    }

    var builder = text()
        .content("[" + text + "]")
        .color(color);

    var writer = TextWriters.newWriter();
    writer.viewer(viewer);
    writer.line(hover);

    attach.write(writer);

    builder
        .hoverEvent(writer.asComponent())
        .clickEvent(ClickEvent.runCommand(MailCommands.getClaimCommand(this, page)));

    return builder.build();
  }

  @Override
  public Component readButton(Audience viewer, Page page) {
    boolean read = isRead();
    TextColor color = read ? NamedTextColor.GRAY : NamedTextColor.YELLOW;
    String text = read ? "unread" : "read";

    return text("[Mark as " + text + "]", color)
        .clickEvent(ClickEvent.runCommand(MailCommands.getReadToggleCommand(this, page)))
        .hoverEvent(text("Click to toggle between read/unread"));
  }

  @Override
  public Component formatMessage(Audience viewer) {
    Component formattedText = message.create(viewer);

    if (!isAdminMessage()) {
      return formattedText;
    }

    PlaceholderRenderer list = Placeholders.newRenderer();
    list.useDefaults();

    if (sentDate != null) {
      list.add("sent_date", () -> Text.DATE_TIME_FORMATTER.format(sentDate));
    }

    if (readDate != null) {
      list.add("read_date", () -> Text.DATE_TIME_FORMATTER.format(readDate));
    }

    if (sender != null) {
      Placeholders.createPlayerPlaceholders(list, "sender", sender);
    }

    Placeholders.createPlayerPlaceholders(list, "target", target);

    return list.render(formattedText, viewer);
  }

  @Override
  public Component metadataText(Audience viewer) {
    var writer = TextWriters.newWriter();
    writer.viewer(viewer);

    writer.line("Mail Message");

    if (sentDate != null) {
      writer.line("Sent ");
      writer.write(TextFormatTypes.DATE.resolve(sentDate, "", viewer));
    }

    if (sender != null && isSenderVisible()) {
      writer.line("Sent by ");
      writer.write(TextFormatTypes.USER.resolve(sender, "-flat", viewer));
    }

    if (hasAttachment()) {
      writer.line(Messages.PAGE_BORDER);

      if (claimDate != null) {
        writer.line("Claimed ");
        writer.write(TextFormatTypes.DATE.resolve(claimDate, "", viewer));
      }

      attachment.write(writer);
    }

    return writer.asComponent();
  }

  @Override
  public boolean canBeOmitted() {
    if (!isRead()) {
      return false;
    }

    if (!hasAttachment()) {
      return true;
    }

    return claimDate != null;
  }

  @Override
  public boolean isSenderVisible() {
    return !hideSender;
  }

  @Override
  public void delete() {
    deleted = true;

    if (service != null) {
      service.getMap().updateDeletedState(this);
    }
  }

  @Override
  public boolean toggleRead() {
    if (readDate != null) {
      readDate = null;
      return false;
    }

    readDate = Instant.now();
    return true;
  }

  @Override
  public void claimAttachment(Player player) throws CommandSyntaxException {
    var state = getAttachmentState();

    if (state == AttachmentState.NO_ATTACHMENT) {
      return;
    }

    if (state == AttachmentState.EXPIRED) {
      throw MailExceptions.attachmentExpired(attachmentExpiry);
    }

    if (state == AttachmentState.CLAIMED) {
      throw MailExceptions.ALREADY_CLAIMED;
    }

    attachment.claim(player);
    Component claimMessage = attachment.claimMessage(player);

    if (player.getUniqueId().equals(target)) {
      claimDate = Instant.now();
    }

    player.sendMessage(claimMessage);
  }
}

