package net.forthecrown.mail.command;

import static net.forthecrown.command.UserMapTopCommand.DEF_PAGE_SIZE;
import static net.forthecrown.mail.command.MailCommands.MESSAGE_ID;
import static net.forthecrown.mail.command.MailCommands.PAGE;
import static net.forthecrown.mail.command.MailCommands.PAGE_SIZE;
import static net.forthecrown.mail.command.MailCommands.PLAYER;
import static net.kyori.adventure.text.Component.text;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.forthecrown.Loggers;
import net.forthecrown.command.Commands;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.arguments.ItemListResult;
import net.forthecrown.events.Events;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.annotations.Argument;
import net.forthecrown.grenadier.annotations.CommandFile;
import net.forthecrown.grenadier.annotations.VariableInitializer;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.options.ArgumentOption;
import net.forthecrown.grenadier.types.options.FlagOption;
import net.forthecrown.grenadier.types.options.Options;
import net.forthecrown.grenadier.types.options.OptionsArgument;
import net.forthecrown.grenadier.types.options.OptionsArgumentBuilder;
import net.forthecrown.grenadier.types.options.ParsedOptions;
import net.forthecrown.mail.Attachment;
import net.forthecrown.mail.Mail;
import net.forthecrown.mail.MailPermissions;
import net.forthecrown.mail.MailService;
import net.forthecrown.mail.Page;
import net.forthecrown.registry.Holder;
import net.forthecrown.scripts.commands.ScriptArgument;
import net.forthecrown.text.PlayerMessage;
import net.forthecrown.text.ViewerAwareMessage;
import net.forthecrown.text.ViewerAwareMessage.WrappedComponent;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.user.currency.Currency;
import net.forthecrown.user.currency.CurrencyMap;
import net.forthecrown.user.currency.CurrencyMaps;
import net.forthecrown.utils.io.source.Source;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.slf4j.Logger;

@CommandFile("mail_command.gcn")
class MailCommand {

  private static final Logger LOGGER = Loggers.getLogger();

  private static final ArgumentOption<ViewerAwareMessage> MESSAGE
      = Options.argument(Arguments.CHAT, "message");

  private static final ArgumentOption<ItemListResult> ITEMS
      = Options.argument(Arguments.ITEM_LIST, "items");

  private static final ArgumentOption<List<String>> TAGS
      = Options.argument(ArgumentTypes.array(StringArgumentType.string()), "tags");

  private static final ArgumentOption<Source> CLAIM_SCRIPT
      = Options.argument(ScriptArgument.SCRIPT, "claim-script");

  private static final ArgumentOption<LocalDate> REWARD_EXPIRY
      = Options.argument(ArgumentTypes.localDate(), "rewards-expire");

  private static final FlagOption USE_GAIN_MULTIPLIER
      = Options.flag("multiply-guild-exp");

  private final MailService service;

  private final CurrencyMap<ArgumentOption<Integer>> currencyOptions;
  private final OptionsArgument adminSendOptions;

  public MailCommand(MailService service) {
    this.service = service;

    OptionsArgumentBuilder optionsBuilder = OptionsArgument.builder();
    optionsBuilder.addRequired(MESSAGE);
    optionsBuilder.addOptional(ITEMS);
    optionsBuilder.addOptional(TAGS);
    optionsBuilder.addOptional(CLAIM_SCRIPT);
    optionsBuilder.addOptional(REWARD_EXPIRY);
    optionsBuilder.addFlag(USE_GAIN_MULTIPLIER);

    var userService = Users.getService();
    var currencies = userService.getCurrencies();

    currencyOptions = CurrencyMaps.newMap();
    IntegerArgumentType intArg = IntegerArgumentType.integer(1);

    for (Holder<Currency> entry : currencies.entries()) {
      ArgumentOption<Integer> option = Options.argument(intArg, entry.getKey());
      optionsBuilder.addOptional(option);
      currencyOptions.putCurrency(entry.getKey(), option);
    }

    adminSendOptions = optionsBuilder.build();
  }

  @VariableInitializer
  void createVariables(Map<String, Object> map) {
    map.put("mail_targets", new MailTargetsArgument(true));
    map.put("mail_target", new MailTargetsArgument(false));
    map.put("admin_options", adminSendOptions);
    map.put("options", MailCommands.OPTIONS);
    map.put("mail_flags", new MailOptionsArgument());

    map.put("flags_permission", MailPermissions.MAIL_FLAGS);
    map.put( "mail_permission", MailPermissions.MAIL);
    map.put("other_permission", MailPermissions.MAIL_OTHERS);
    map.put("admin_permission", MailPermissions.MAIL_ADMIN);
    map.put( "item_permission", MailPermissions.MAIL_ITEMS);
  }

  void showMail(
      CommandSource source,
      @Argument(value = "page", optional = true) Integer pageArg,
      @Argument(value = "pageSize", optional = true) Integer pageSizeArg
  ) throws CommandSyntaxException {
    User user = Commands.getUserSender(source);

    int page = Objects.requireNonNullElse(pageArg, 1);
    int pageSize = Objects.requireNonNullElse(pageSizeArg, DEF_PAGE_SIZE);

    Page p = new Page(user, page, pageSize);
    displayPage(source, p, false);
  }

  void readOther(
      CommandSource source,
      @Argument(value = "user") User target,
      @Argument(value = "page", optional = true) Integer pageArg,
      @Argument(value = "pageSize", optional = true) Integer pageSizeArg
  ) throws CommandSyntaxException {
    int page = Objects.requireNonNullElse(pageArg, 1);
    int pageSize = Objects.requireNonNullElse(pageSizeArg, DEF_PAGE_SIZE);

    Page p = new Page(target, page, pageSize);
    displayPage(source, p, true);
  }

  void sendMailAnonymous(
      CommandSource source,
      @Argument("targets") MailTargets targets,
      @Argument("message") PlayerMessage message
  ) throws CommandSyntaxException {
    sendMail(source, targets, message, EnumSet.of(MailSendOption.ANONYMOUS));
  }

  void sendMail(
      CommandSource source,
      @Argument("targets") MailTargets targets,
      @Argument("message") PlayerMessage message,
      @Argument(value = "flags", optional = true) EnumSet<MailSendOption> options
  ) throws CommandSyntaxException {
    MailCommands.sendMail(source, targets, user -> {
      var builder = MailCommands.createBuilder(source)
          .target(user)
          .message(message.edit(MailCommands.NEW_LINES));

      MailSendOption.apply(options, builder);
      builder.send();
    });
  }

  void sendItemAnonymous(
      CommandSource source,
      @Argument("target")  MailTargets targets,
      @Argument("message") PlayerMessage message
  ) throws CommandSyntaxException {
    sendItem(source, targets, message, EnumSet.of(MailSendOption.ANONYMOUS));
  }

  void sendItem(
      CommandSource source,
      @Argument("target")  MailTargets targets,
      @Argument("message") PlayerMessage message,
      @Argument(value = "flags", optional = true) EnumSet<MailSendOption> options
  ) throws CommandSyntaxException {
    Player player = source.asPlayer();

    message = message.edit(MailCommands.NEW_LINES);

    ItemSendListener listener = new ItemSendListener(source, message, targets, options);
    Events.register(listener);

    player.openInventory(listener.getInventory());
  }

  void sendAdmin(
      CommandSource source,
      @Argument("targets") MailTargets targets,
      @Argument("options") ParsedOptions options
  ) throws CommandSyntaxException {
    var mailBuilder = Mail.builder();
    var attachment = Attachment.builder();

    var message = options.getValue(MESSAGE);
    if (message instanceof PlayerMessage playerMessage) {
      mailBuilder.message(playerMessage.edit(MailCommands.NEW_LINES));
    } else if (message instanceof WrappedComponent wrapped) {
      mailBuilder.message(wrapped.text());
    } else {
      LOGGER.error("Mail message from command was neither PlayerMessage nor normal component");
      throw Exceptions.create("Internal error");
    }

    currencyOptions.forEach((currency, option) -> {
      if (!options.has(option)) {
        return;
      }

      int value = options.getValue(option);
      attachment.currency(currency, value);
    });

    if (options.has(ITEMS)) {
      var result = options.getValue(ITEMS);
      var list = result.get(source);
      attachment.items(list);
    }

    options.getValueOptional(REWARD_EXPIRY).ifPresent(date -> {
      var instant = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
      mailBuilder.attachmentExpiry(instant);
    });

    options.getValueOptional(TAGS).ifPresent(attachment::addTags);
    options.getValueOptional(CLAIM_SCRIPT).ifPresent(attachment::claimScript);
    attachment.useGainMultiplier(options.has(USE_GAIN_MULTIPLIER));
    mailBuilder.attachment(attachment.build());

    MailCommands.sendMail(source, targets, user -> mailBuilder.target(user).send());
  }

  void markRead(
      CommandSource source,
      @Argument("options") ParsedOptions options
  ) throws CommandSyntaxException {
    Optional<Mail> mailOpt = options.getValueOptional(MESSAGE_ID).map(service::getMessage);

    if (mailOpt.isPresent()) {
      var mail = mailOpt.get();
      MailCommands.validateAccess(source, mail);

      boolean nowRead = mail.toggleRead();

      if (nowRead) {
        source.sendMessage(text("Marked as read", NamedTextColor.YELLOW));
      } else {
        source.sendMessage(text("Marked as unread", NamedTextColor.GRAY));
      }
    }

    Page page = fromOptions(source, options);
    displayPage(source, page);
  }

  void claimRewards(
      CommandSource source,
      @Argument("options") ParsedOptions options
  ) throws CommandSyntaxException {
    Optional<Mail> mailOpt = options.getValueOptional(MESSAGE_ID).map(service::getMessage);

    if (mailOpt.isPresent()) {
      var mail = mailOpt.get();
      MailCommands.validateAccess(source, mail);
      mail.claimAttachment(source.asPlayer());
    }

    Page page = fromOptions(source, options);
    displayPage(source, page);
  }

  void deleteMail(
      CommandSource source,
      @Argument("options") ParsedOptions options
  ) throws CommandSyntaxException {
    Optional<Mail> mailOpt = options.getValueOptional(MESSAGE_ID).map(service::getMessage);

    if (mailOpt.isPresent()) {
      var mail = mailOpt.get();

      MailCommands.validateAccess(source, mail);
      mail.delete();

      source.sendMessage(text("Message deleted", NamedTextColor.YELLOW));
    }

    Page page = fromOptions(source, options);
    displayPage(source, page);
  }

  void displayPage(CommandSource source, Page page) throws CommandSyntaxException {
    boolean includeDeleted = !page.player().getName().equals(source.textName());
    displayPage(source, page, includeDeleted);
  }

  void displayPage(CommandSource source, Page page, boolean includeDeleted)
      throws CommandSyntaxException
  {
    User user = page.player();

    // Ensure they actually have permission to view the other player's mail
    if (source.isPlayer() && !source.hasPermission(MailPermissions.MAIL_OTHERS)) {
      throw Exceptions.NO_PERMISSION;
    }

    var mailList = MailCommands.getMailList(service, user, includeDeleted);

    if (mailList.isEmpty()) {
      throw Exceptions.NOTHING_TO_LIST;
    }

    Component pageText = MailList.formatMail(source, page, mailList);
    source.sendMessage(pageText);
  }

  Page fromOptions(CommandSource source, ParsedOptions options) throws CommandSyntaxException {
    int page = options.getValueOptional(PAGE).orElse(1);
    int pageSize = options.getValueOptional(PAGE_SIZE).orElse(DEF_PAGE_SIZE);

    User user;

    if (options.has(PLAYER)) {
      user = options.getValue(PLAYER).get(source, true);

      if (source.isPlayer() && !source.hasPermission(MailPermissions.MAIL_OTHERS)) {
        var senderId = source.asPlayer().getUniqueId();

        if (!user.getUniqueId().equals(senderId)) {
          throw Exceptions.create("Cannot view mail");
        }
      }

    } else {
      user = null;
    }

    return new Page(user, page, pageSize);
  }
}
