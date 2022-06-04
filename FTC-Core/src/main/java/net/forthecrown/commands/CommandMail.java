package net.forthecrown.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import net.forthecrown.commands.arguments.ChatArgument;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.commands.manager.FtcSuggestionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.economy.shops.ShopConstants;
import net.forthecrown.events.Events;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.inventory.FtcInventory;
import net.forthecrown.inventory.ItemStacks;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserMail;
import net.forthecrown.user.actions.ActionFactory;
import net.forthecrown.user.actions.MailAddAction;
import net.forthecrown.user.actions.MailQuery;
import net.forthecrown.user.actions.UserActionHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

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

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> readMail(c, 0))

                .then(argument("page", IntegerArgumentType.integer(1))
                        .executes(c -> {
                            int page = c.getArgument("page", Integer.class) - 1;
                            return readMail(c, page);
                        })
                )

                .then(literal("read_other")
                        .requires(source -> source.hasPermission(Permissions.MAIL_OTHERS))

                        .then(argument("user", UserArgument.user())
                                .requires(source -> source.hasPermission(Permissions.MAIL_OTHERS))

                                .executes(c -> readMailOther(c, 0))

                                .then(argument("page", IntegerArgumentType.integer(1))
                                        .requires(source -> source.hasPermission(Permissions.MAIL_OTHERS))

                                        .executes(c -> {
                                            int page = c.getArgument("page", Integer.class) - 1;
                                            return readMailOther(c, page);
                                        })
                                )
                        )
                )

                .then(literal("mark_unread")
                        .then(argument("index", IntegerArgumentType.integer(1))
                                .executes(c -> {
                                    CrownUser user = getUserSender(c);
                                    UserMail mail = user.getMail();
                                    int index = c.getArgument("index", Integer.class) - 1;

                                    if(!mail.isValidIndex(index)) {
                                        throw FtcExceptionProvider.translatable("mail.invalidIndex", Component.text(index + 1));
                                    }

                                    UserMail.MailMessage message = mail.get(index);
                                    message.read = false;

                                    user.sendMessage(
                                            Component.translatable("mail.marked",
                                                    NamedTextColor.YELLOW,
                                                    Component.translatable("mail.unread")
                                            )
                                    );
                                    return 0;
                                })
                        )
                )

                .then(literal("mark_read")
                        .then(argument("index", IntegerArgumentType.integer(1))
                                .executes(c -> {
                                    CrownUser user = getUserSender(c);
                                    UserMail mail = user.getMail();
                                    int index = c.getArgument("index", Integer.class) - 1;

                                    if(!mail.isValidIndex(index)) {
                                        throw FtcExceptionProvider.translatable("mail.invalidIndex", Component.text(index + 1));
                                    }

                                    UserMail.MailMessage message = mail.get(index);
                                    message.read = true;

                                    user.sendMessage(
                                            Component.translatable("mail.marked",
                                                    NamedTextColor.YELLOW,
                                                    Component.translatable("mail.read")
                                            )
                                    );
                                    return 0;
                                })
                        )
                )

                .then(literal("clear")
                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            user.getMail().clearPartial();

                            user.sendMessage(
                                    Component.translatable("mail.cleared", NamedTextColor.YELLOW)
                            );
                            return 0;
                        })
                )

                // Claiming items embedded in messages
                .then(literal("claim")
                        .then(argument("index", IntegerArgumentType.integer(1))
                                .executes(c -> {
                                    CrownUser user = getUserSender(c);
                                    UserMail mail = user.getMail();

                                    int index = c.getArgument("index", Integer.class);

                                    if (!mail.isValidIndex(index)) {
                                        throw FtcExceptionProvider.translatable("mail.invalidIndex", Component.text(index));
                                    }

                                    UserMail.MailMessage message = mail.get(index - 1);

                                    if (!UserMail.hasAttachment(message)) {
                                        throw FtcExceptionProvider.translatable("mail.noItem");
                                    }

                                    if (message.attachmentClaimed) {
                                        throw FtcExceptionProvider.translatable("mail.alreadyClaimed");
                                    }

                                    message.attachment.testClaimable(user);

                                    message.read = true;
                                    message.attachmentClaimed = true;
                                    message.attachment.claim(user);

                                    user.sendMessage(message.attachment.claimText());
                                    return 0;
                                })
                        )
                )

                .then(sendArgs(true))
                .then(sendArgs(false));
    }

    private LiteralArgumentBuilder<CommandSource> sendArgs(boolean item) {
        return literal("send" + (item ? "_item" : ""))
                .requires(item ? (source -> source.hasPermission(Permissions.MAIL_ITEMS)) : ArgumentBuilder.defaultRequirement())

                .then(literal("-all")
                        .requires(source -> source.hasPermission(Permissions.MAIL_ALL))

                        .then(argument("message", ChatArgument.chat())
                                .suggests((context, builder) -> FtcSuggestionProvider.suggestPlayerNamesAndEmotes(context, builder, false))

                                .executes(c -> sendAll(c, item))
                        )
                )

                .then(argument("target", UserArgument.user())
                        .then(argument("message", StringArgumentType.greedyString())
                                .suggests((context, builder) -> FtcSuggestionProvider.suggestPlayerNamesAndEmotes(context, builder, false))

                                .executes(c -> send(c, item))
                        )
                );
    }

    private int send(CommandContext<CommandSource> c, boolean item) throws CommandSyntaxException {
        CrownUser user = getUserSender(c);
        CrownUser target = UserArgument.getUser(c, "target");

        String rawMessage = c.getArgument("message", String.class);
        Component cMessage = FtcFormatter.formatIfAllowed(rawMessage, user);

        if (item) {
            ItemSender sender = (message, item1) -> {
                MailAddAction action = new MailAddAction(message, target, user.getUniqueId());
                action.setAttachment(UserMail.MailAttachment.item(item1));

                UserActionHandler.handleAction(action);
            };

            return handleItem(user, sender, cMessage);
        }

        ActionFactory.addMail(target, cMessage, user.getUniqueId());
        return 0;
    }

    private int sendAll(CommandContext<CommandSource> c, boolean item) throws CommandSyntaxException {
        Component message = c.getArgument("message", Component.class);

        if (item) {
            CrownUser user = getUserSender(c);
            ItemSender sender = (message1, item1) -> sendAll_(c.getSource(), message1, item1);

            return handleItem(user, sender, message);
        }

        return sendAll_(c.getSource(), message, null);
    }

    private int handleItem(CrownUser user, ItemSender sender, Component message) {
        MailItemSender mailItemSender = new MailItemSender(user, sender, message);
        Events.register(mailItemSender);

        user.getPlayer().openInventory(mailItemSender.getInventory());
        return 0;
    }

    private int sendAll_(CommandSource source, Component text, ItemStack item) {
        UUID uuid = null;

        Crown.getUserManager().getAllUsers()
                .whenComplete((users, throwable) -> {
                    if(throwable != null) {
                        source.sendAdmin("Error sending mail to all users, check console");
                        Crown.logger().error(throwable);

                        return;
                    }

                    users.forEach(user -> {
                        MailAddAction action = new MailAddAction(text, user, uuid);

                        if(item != null) {
                            action.setAttachment(UserMail.MailAttachment.item(item.clone()));
                        }

                        action.setValidateSender(false);
                        action.setInformSender(false);

                        UserActionHandler.handleAction(action);
                    });

                    source.sendAdmin("Finished sending everyone mail");
                    Crown.getUserManager().unloadOffline();
                });

        return 0;
    }

    private int readMailOther(CommandContext<CommandSource> c, int page) throws CommandSyntaxException {
        CrownUser user = UserArgument.getUser(c, "user");

        MailQuery query = new MailQuery(c.getSource(), user, page);
        UserActionHandler.handleAction(query);

        return 0;
    }

    private static int readMail(CommandContext<CommandSource> c, int page) throws CommandSyntaxException {
        MailQuery query = new MailQuery(c.getSource(), getUserSender(c), page);
        UserActionHandler.handleAction(query);
        return 1;
    }

    private interface ItemSender {
        void send(Component message, ItemStack item);
    }

    private static class MailItemSender implements Listener, InventoryHolder {
        @Getter
        private final FtcInventory inventory;
        private final CrownUser sender;
        private final Component msgInput;
        private final ItemSender itemSender;

        public MailItemSender(CrownUser sender, ItemSender itemSender, Component msgInput) {
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
            if (event.getClickedInventory() == null) return;
            if (!event.getClickedInventory().equals(inventory)) return;
            if (event.getSlot() == ShopConstants.EXAMPLE_ITEM_SLOT) return;

            event.setCancelled(true);
        }

        @EventHandler(ignoreCancelled = true)
        public void onInventoryClose(InventoryCloseEvent event) {
            if (!event.getInventory().equals(inventory)) return;

            Events.handle(sender, event, e -> {
                ItemStack item = e.getInventory().getItem(ShopConstants.EXAMPLE_ITEM_SLOT);

                if (ItemStacks.isEmpty(item)) {
                    throw FtcExceptionProvider.translatable("mail.noItemGiven");
                }

                Events.unregister(this);
                itemSender.send(msgInput, item.clone());
            });
        }
    }
}