package net.forthecrown.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Commands;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.FTC;
import net.forthecrown.core.Messages;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.admin.BannedWords;
import net.forthecrown.core.admin.Mute;
import net.forthecrown.core.admin.Punishments;
import net.forthecrown.events.Events;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.args.ArgsArgument;
import net.forthecrown.grenadier.types.args.Argument;
import net.forthecrown.grenadier.types.args.ParsedArgs;
import net.forthecrown.inventory.FtcInventory;
import net.forthecrown.useables.util.UsageUtil;
import net.forthecrown.user.User;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.UserOfflineException;
import net.forthecrown.user.Users;
import net.forthecrown.user.data.MailAttachment;
import net.forthecrown.user.data.MailMessage;
import net.forthecrown.user.data.UserMail;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.text.format.page.Footer;
import net.forthecrown.utils.text.format.page.PageEntry;
import net.forthecrown.utils.text.format.page.PageEntryIterator;
import net.forthecrown.utils.text.format.page.PageFormat;
import net.forthecrown.utils.text.writer.TextWriter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static net.forthecrown.commands.UserMapTopCommand.DEF_PAGE_SIZE;
import static net.forthecrown.economy.shops.SignShops.EXAMPLE_ITEM_SLOT;

public class CommandMail extends FtcCommand {
    public CommandMail() {
        super("mail");

        setPermission(Permissions.MAIL);
        setDescription("Does mail stuff, idk");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /Mail
     *
     * Permissions used:
     *
     * Main Author: Julie
     */

    private static final Argument<Component> MSG_ARG = Argument.builder("message", Arguments.CHAT)
            .build();

    private static final Argument<Integer> RHINE_ARG = Argument.builder("rhines", IntegerArgumentType.integer(0))
            .setDefaultValue(0)
            .build();

    private static final Argument<Integer> GEM_ARG = Argument.builder("gems", IntegerArgumentType.integer(0))
            .setDefaultValue(0)
            .build();

    private static final Argument<ItemStack> ITEM_ARG = Argument.builder("item", UsageUtil.ITEM_ARGUMENT)
            .build();

    private static final Argument<String> TAG_ARG = Argument.builder("tag", StringArgumentType.string())
            .build();

    private static final Argument<String> SCRIPT_ARG = Argument.builder("script", Arguments.SCRIPT)
            .build();

    private static final ArgsArgument ARGS = ArgsArgument.builder()
            .addRequired(MSG_ARG)
            .addOptional(ITEM_ARG)
            .addOptional(GEM_ARG)
            .addOptional(RHINE_ARG)
            .addOptional(TAG_ARG)
            .addOptional(SCRIPT_ARG)
            .build();

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> readMail(c, 0, DEF_PAGE_SIZE))

                // /mail <page>
                .then(argument("page", IntegerArgumentType.integer(1))
                        .executes(c -> {
                            int page = c.getArgument("page", Integer.class) - 1;
                            return readMail(c, page, DEF_PAGE_SIZE);
                        })

                        // /mail <page> <page size>
                        .then(argument("pageSize", IntegerArgumentType.integer(5, 20))
                                .executes(c -> {
                                    int page = c.getArgument("page", Integer.class) - 1;
                                    int pageSize = c.getArgument("pageSize", Integer.class);
                                    return readMail(c, page, pageSize);
                                })
                        )
                )

                // /mail read_other
                .then(literal("read_other")
                        .requires(source -> source.hasPermission(Permissions.MAIL_OTHERS))

                        // /mail read_other <user>
                        .then(argument("user", Arguments.USER)
                                .requires(source -> source.hasPermission(Permissions.MAIL_OTHERS))

                                .executes(c -> readMailOther(c, 0, DEF_PAGE_SIZE))

                                // /mail read_other <user> <page>
                                .then(argument("page", IntegerArgumentType.integer(1))
                                        .requires(source -> source.hasPermission(Permissions.MAIL_OTHERS))

                                        .executes(c -> {
                                            int page = c.getArgument("page", Integer.class) - 1;
                                            return readMailOther(c, page, DEF_PAGE_SIZE);
                                        })

                                        // /mail read_other <user> <page> <pageSize>
                                        .then(argument("pageSize", IntegerArgumentType.integer(5, 20))
                                                .requires(source -> source.hasPermission(Permissions.MAIL_OTHERS))

                                                .executes(c -> {
                                                    int page = c.getArgument("page", Integer.class) - 1;
                                                    int pageSize = c.getArgument("pageSize", Integer.class);
                                                    return readMailOther(c, page, pageSize);
                                                })
                                        )
                                )
                        )
                )

                // /mail clear
                .then(literal("clear")
                        .executes(c -> {
                            User user = getUserSender(c);
                            user.getMail().clearPartial();

                            user.sendMessage(
                                    Messages.MAIL_CLEARED
                            );
                            return 0;
                        })

                        // /mail clear <user>
                        .then(argument("user", Arguments.USER)
                                .requires(source -> source.hasPermission(Permissions.MAIL_OTHERS))

                                .executes(c -> {
                                    var user = Arguments.getUser(c, "user");
                                    var mail = user.getMail();
                                    var list = mail.getMail();

                                    list.clear();

                                    c.getSource().sendAdmin(Messages.clearedMail(user));
                                    return 0;
                                })
                        )
                )

                // Claiming items embedded in messages
                // /mail claim <index>
                .then(literal("claim")
                        .then(argument("index", IntegerArgumentType.integer(1))
                                .executes(c -> {
                                    User user = getUserSender(c);
                                    UserMail mail = user.getMail();
                                    var list = mail.getMail();

                                    int index = c.getArgument("index", Integer.class);

                                    Commands.ensureIndexValid(index, list.size());

                                    MailMessage message = list.get(index - 1);

                                    if (MailAttachment.isEmpty(message.getAttachment())) {
                                        throw Exceptions.MAIL_NOTHING_CLAIMABLE;
                                    }

                                    if (message.getAttachment().isClaimed()) {
                                        throw Exceptions.MAIL_ALREADY_CLAIMED;
                                    }

                                    var attachment = message.getAttachment();
                                    attachment.testClaimable(user);

                                    message.setRead(true);
                                    attachment.setClaimed(false);
                                    attachment.claim(user);

                                    user.sendMessage(attachment.claimText());
                                    return 0;
                                })
                        )
                )

                // /mail admin_send <user | -all> <arguments>
                .then(adminSend())

                // /mail mark_read <index>
                // /mail mark_read <index> <user>
                .then(markReadArgument(true))

                // /mail mark_unread <index>
                // /mail mark_unread <index> <user>
                .then(markReadArgument(false))

                // /mail send_item <user> <message>
                // /mail send_item -all <message>
                .then(sendArgs(true))

                // /mail send <user> <message>
                // /mail send -all <message>
                .then(sendArgs(false));
    }

    private LiteralArgumentBuilder<CommandSource> markReadArgument(boolean read) {
        return literal(String.format("mark_%sread", read ? "" : "un"))

                .then(argument("index", IntegerArgumentType.integer(1))
                        .executes(c -> {
                            User user = getUserSender(c);
                            UserMail mail = user.getMail();
                            var list = mail.getMail();
                            int index = c.getArgument("index", Integer.class);

                            Commands.ensureIndexValid(index, list.size());

                            MailMessage message = list.get(index - 1);
                            message.setRead(read);

                            user.sendMessage(read ?
                                    Messages.MARKED_READ
                                    : Messages.MARKED_UNREAD
                            );
                            return 0;
                        })

                        .then(argument("user", Arguments.USER)
                                .requires(source -> source.hasPermission(Permissions.MAIL_OTHERS))

                                .executes(c -> {
                                    User user = Arguments.getUser(c, "user");
                                    UserMail mail = user.getMail();
                                    var list = mail.getMail();
                                    int index = c.getArgument("index", Integer.class);

                                    Commands.ensureIndexValid(index, list.size());

                                    MailMessage message = list.get(index - 1);
                                    message.setRead(read);

                                    c.getSource().sendAdmin(Messages.markedReadOther(user, read));
                                    return 0;
                                })
                        )
                );
    }

    private LiteralArgumentBuilder<CommandSource> adminSend() {
        return literal("admin_send")
                .requires(source -> source.hasPermission(Permissions.MAIL_ALL))

                .then(literal("-all")
                        .then(adminSendArgs(true))
                )

                .then(argument("user", Arguments.USER)
                        .then(adminSendArgs(false))
                );
    }

    private RequiredArgumentBuilder<CommandSource, ?> adminSendArgs(boolean all) {
        return argument("args", ARGS)
                .executes(c -> {
                    var args = c.getArgument("args", ParsedArgs.class);
                    MailMessage message = MailMessage.of(args.get(MSG_ARG));
                    MailAttachment attachment = new MailAttachment();

                    if (args.has(TAG_ARG)) {
                        attachment.setTag(args.get(TAG_ARG));
                    }

                    if (args.has(ITEM_ARG)) {
                        attachment.setItem(args.get(ITEM_ARG));
                    }

                    if (args.has(SCRIPT_ARG)) {
                        attachment.setScript(args.get(SCRIPT_ARG));
                    }

                    attachment.setRhines(args.get(RHINE_ARG));
                    attachment.setGems(args.get(GEM_ARG));

                    if (!attachment.isEmpty()) {
                        message.setAttachment(attachment);
                    }

                    if (all) {
                        UserManager.get()
                                .getAllUsers()
                                .whenComplete((users, throwable) -> {
                                    if (throwable != null) {
                                        FTC.getLogger().error("Couldn't load all users", throwable);
                                        return;
                                    }

                                    users.forEach(user -> user.sendMail(message));
                                    c.getSource().sendAdmin("Sent all users mail");
                                });
                    } else {
                        var user = Arguments.getUser(c, "user");
                        user.sendMail(message);

                        c.getSource().sendAdmin(
                                Messages.mailSent(user, message.getMessage())
                        );
                    }

                    return 0;
                });
    }

    private LiteralArgumentBuilder<CommandSource> sendArgs(boolean item) {
        return literal("send" + (item ? "_item" : ""))
                .requires(item ? (source -> source.hasPermission(Permissions.MAIL_ITEMS)) : ArgumentBuilder.defaultRequirement())

                .then(literal("-all")
                        .requires(source -> source.hasPermission(Permissions.MAIL_ALL))

                        .then(argument("message", Arguments.CHAT)
                                .executes(c -> sendAll(c, item))
                        )
                )

                .then(argument("target", Arguments.USER)
                        .then(argument("message", Arguments.MESSAGE)
                                .executes(c -> send(c, item))
                        )
                );
    }

    private int send(CommandContext<CommandSource> c, boolean item) throws CommandSyntaxException {
        User user = getUserSender(c);
        User target = Arguments.getUser(c, "target");

        Component cMessage = Arguments.getMessage(c, "message");

        if (item) {
            ItemSender sender = (message, item1) -> {
                MailMessage message1 = MailMessage.of(message, user.getUniqueId());
                message1.setAttachment(MailAttachment.item(item1));

                // Failed to send mail because blocked or muted
                // or something, so give item back.
                if (!trySendMail(target, message1)) {
                    user.getInventory().addItem(
                            message1.getAttachment().getItem().clone()
                    );
                }
            };

            return handleItem(user, sender, cMessage);
        }

        MailMessage message1 = MailMessage.of(cMessage, user.getUniqueId());
        trySendMail(target, message1);
        return 0;
    }

    private int sendAll(CommandContext<CommandSource> c, boolean item) throws CommandSyntaxException {
        Component message = c.getArgument("message", Component.class);

        if (item) {
            User user = getUserSender(c);
            ItemSender sender = (message1, item1) -> sendAll_(c.getSource(), message1, item1);

            return handleItem(user, sender, message);
        }

        return sendAll_(c.getSource(), message, null);
    }

    private int handleItem(User user, ItemSender sender, Component message) {
        MailItemSender mailItemSender = new MailItemSender(user, sender, message);
        Events.register(mailItemSender);

        user.getPlayer().openInventory(mailItemSender.getInventory());
        return 0;
    }

    private int sendAll_(CommandSource source, Component text, ItemStack item) {
        UserManager.get().getAllUsers()
                .whenComplete((users, throwable) -> {
                    if (throwable != null) {
                        source.sendAdmin("Error sending mail to all users, check console");
                        FTC.getLogger().error(throwable);

                        return;
                    }

                    var message = MailMessage.of(text);

                    if (item != null) {
                        message.setAttachment(MailAttachment.item(item));
                    }

                    users.forEach(user -> user.sendMail(message));

                    source.sendAdmin("Finished sending everyone mail");
                    Users.unloadOffline();
                });

        return 0;
    }

    private int readMailOther(CommandContext<CommandSource> c, int page, int pageSize) throws CommandSyntaxException {
        User user = Arguments.getUser(c, "user");
        return readMail(c.getSource(), user, page, pageSize);
    }

    private static int readMail(CommandContext<CommandSource> c, int page, int pageSize) throws CommandSyntaxException {
        return readMail(c.getSource(), getUserSender(c), page, pageSize);
    }

    private static int readMail(CommandSource source, User user, int page, int pageSize) throws CommandSyntaxException {
        var mail = user.getMail();
        boolean self = source.textName().equals(user.getName());
        List<MailMessage> messages = mail.getMail();

        if (messages.isEmpty()) {
            throw Exceptions.NOTHING_TO_LIST;
        }

        Commands.ensurePageValid(page, pageSize, messages.size());

        final PageFormat<MailMessage> format = PageFormat.create();

        format.setHeader(
                self ? Messages.MAIL_HEADER_SELF : Messages.mailHeader(user)
        );

        format.setEntry(PageEntry.of(
                new PageEntry.IndexFormatter<MailMessage>() {
                    @Override
                    public Component createIndex(int viewerIndex, MailMessage entry) {
                        // Format index to show meta info about the
                        // mail message
                        return Component.text(viewerIndex + ")")
                                .color(NamedTextColor.GOLD)
                                .hoverEvent(Messages.messageMetaInfo(entry));
                    }
                },

                new PageEntry.EntryDisplay<MailMessage>() {
                    @Override
                    public void write(TextWriter writer, MailMessage entry, int viewerIndex) {
                        // create mark as read/unread click event
                        var event = ClickEvent.runCommand(
                                String.format(
                                        "/mail mark_%sread %s%s",
                                        entry.isRead() ? "un" : "",
                                        viewerIndex,
                                        self ? "" : " " + user.getName()
                                )
                        );

                        // Write the [read] or [unread] buttons
                        if (entry.isRead()) {
                            writer.write(Messages.MAIL_UNREAD.clickEvent(event));
                        } else {
                            writer.write(Messages.MAIL_READ.clickEvent(event));
                        }

                        // If there's an attachment to show
                        if (!MailAttachment.isEmpty(entry.getAttachment())) {
                            var attach = entry.getAttachment();

                            writer.space();

                            // If we're reading self, show the button which
                            // would let you claim the attachment
                            if (self) {
                                var cmd = "/mail claim " + viewerIndex;

                                writer.write(
                                        Messages.CLAIM
                                                .color(attach.isClaimed() ?
                                                        NamedTextColor.GRAY
                                                        : NamedTextColor.AQUA
                                                )
                                                .clickEvent(ClickEvent.runCommand(cmd))
                                );
                            } else {
                                // Else just create a display button to tell
                                // staff what's in the attachment
                                writer.write(
                                        Messages.MAIL_ATTACHMENT.hoverEvent(attach)
                                );
                            }
                        }

                        // Write a space and then the actual content
                        // of the mail message itself
                        writer.space();
                        writer.write(entry.getMessage());
                    }
                }
        ));

        String cmdFormat;

        if (self) {
            cmdFormat = "mail %s %s";
        } else {
            cmdFormat = "mail read_other " + user.getName() + " %s %s";
        }

        format.setFooter(Footer.ofButton(cmdFormat));

        source.sendMessage(
                format.format(
                        PageEntryIterator.of(
                                mail.getMail(),
                                page, pageSize
                        )
                )
        );

        return 0;
    }

    private interface ItemSender {
        void send(Component message, ItemStack item);
    }

    private static boolean trySendMail(User target, MailMessage message) throws UserOfflineException {
        if (message.getSender() != null) {
            var sender = Users.get(message.getSender());
            Mute mute = Punishments.checkMute(sender);

            if (BannedWords.checkAndWarn(sender.getPlayer(), message.getMessage())) {
                return false;
            }

            if (Users.testBlocked(sender, target,
                    Messages.MAIL_BLOCKED_SENDER,
                    Messages.MAIL_BLOCKED_TARGET
            )) {
                return false;
            }

            if (mute.isVisibleToSender()) {
                sender.sendMessage(
                        Messages.mailSent(target, message.getMessage())
                );
            }

            if (!mute.isVisibleToOthers()) {
                return false;
            }
        }

        target.sendMail(message);
        return true;
    }

    private static class MailItemSender implements Listener, InventoryHolder {
        @Getter
        private final FtcInventory inventory;
        private final User sender;
        private final Component msgInput;
        private final ItemSender itemSender;

        public MailItemSender(User sender, ItemSender itemSender, Component msgInput) {
            this.sender = sender;
            this.itemSender = itemSender;
            this.msgInput = msgInput;

            this.inventory = FtcInventory.of(this, InventoryType.HOPPER, Component.text("Mail an item"));

            ItemStack barrier = new ItemStack(Material.BARRIER, 1);

            inventory.setItem(0, barrier);
            inventory.setItem(1, barrier);
            inventory.setItem(3, barrier);
            inventory.setItem(4, barrier);
        }

        @EventHandler(ignoreCancelled = true)
        public void onInventoryClick(InventoryClickEvent event) {
            if (event.getClickedInventory() == null) {
                return;
            }

            if (!event.getClickedInventory().equals(inventory)) {
                return;
            }

            if (event.getSlot() == EXAMPLE_ITEM_SLOT) {
                return;
            }

            event.setCancelled(true);
        }

        @EventHandler(ignoreCancelled = true)
        public void onInventoryClose(InventoryCloseEvent event) {
            if (!event.getInventory().equals(inventory)) {
                return;
            }

            Events.runSafe(sender, event, e -> {
                ItemStack item = e.getInventory().getItem(EXAMPLE_ITEM_SLOT);

                if (ItemStacks.isEmpty(item)) {
                    throw Exceptions.MAIL_NO_ITEM_GIVEN;
                }

                Events.unregister(this);
                itemSender.send(msgInput, item.clone());
            });
        }
    }
}