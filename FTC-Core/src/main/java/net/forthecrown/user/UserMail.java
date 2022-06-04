package net.forthecrown.user;

import com.google.gson.JsonElement;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.chat.ComponentWriter;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.inventory.ItemStacks;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.JsonUtils;
import net.forthecrown.utils.Struct;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.UUID;

public interface UserMail extends UserAttachment {
    /**
     * Max entries to display per /mail page
     */
    int PAGE_SIZE = 10;

    /**
     * Checks if the UUID is allowed to look at this user's
     * mail
     * @param id The UUID to check
     * @return True, if they can see, false otherwise
     */
    boolean canSee(UUID id);

    default boolean canSee(CommandSender sender) {
        if(sender instanceof Player) {
            return canSee(((Player) sender).getUniqueId());
        }

        if(sender instanceof CrownUser) {
            return canSee(((CrownUser) sender).getUniqueId());
        }

        return true;
    }

    default void add(Component component) {
        add(component, null);
    }

    default void add(Component message, @Nullable UUID sender) {
        add(new MailMessage(message, sender, System.currentTimeMillis()));
    }

    /**
     * Adds the given message to the front of the mail
     * list
     *
     * @param message The message to add
     */
    void add(MailMessage message);

    /**
     * Removes the mail at a specific index
     * @param index The index to remove the mail at
     */
    void remove(int index);

    /**
     * Reads all mail
     */
    void readAll();

    /**
     * Removes all mail that doesn't have an
     * attachment or has the attachment claimed
     */
    void clearPartial();

    /**
     * Clears mail, ignores item attachments having been claimed or not
     */
    void clearTotal();

    /**
     * Mail size
     * @return size
     */
    int size();

    /**
     * Gets all unread mail
     * @return unread mail
     */
    ObjectList<MailMessage> getUnread();

    /**
     * Gets all mail
     * @return The mail
     */
    ObjectList<MailMessage> getMail();

    /**
     * Gets the amount of unread messages
     * @return amount of unread messages
     */
    default int unreadSize() {
        return getUnread().size();
    }

    /**
     * Gets the message at the given index
     * @param index The index
     * @return The message at the index
     * @throws IndexOutOfBoundsException If the index is < 0 || > size
     */
    MailMessage get(int index) throws IndexOutOfBoundsException;

    /**
     * Gets the index of the given message
     * @param message The message to get the index of
     * @return The index of the message, or -1 if not in mail
     */
    int indexOf(MailMessage message);

    /**
     * Checks if the given index is within the mail list bounds
     * @param index The index to check
     * @return if the index > 0 && index < size
     */
    default boolean isValidIndex(int index) {
        return index >= 0 && index <= size();
    }

    /**
     * Tells the user if they have unread mail
     */
    default void informOfUnread() {
        if (!getUser().isOnline()) return;

        int unreadSize = unreadSize();
        if(unreadSize < 1) return;
        CrownUser user = getUser();

        user.sendMessage(
                Component.translatable("mail.new")
                        .color(NamedTextColor.YELLOW)

                        .hoverEvent(Component.text("Click to read mail"))
                        .clickEvent(ClickEvent.runCommand("/mail"))

                        .append(Component.space())
                        .append(Component.translatable("mail.new2", Component.text(unreadSize)).color(NamedTextColor.GRAY))
        );
    }

    /**
     * A small class that holds the data for
     * a mail message, when it was sent and by whom
     * and what it contains.
     */
    class MailMessage implements Struct {
        public final Component message;
        public final UUID sender;
        public final long sent;

        public boolean read, attachmentClaimed;
        public MailAttachment attachment;

        public MailMessage(Component message, UUID sender, long sent) {
            this(message, sender, sent, false);
        }

        public MailMessage(Component message, UUID sender, long sent, boolean read) {
            this.message = message;
            this.sender = sender;
            this.sent = sent;
            this.read = read;
        }
    }

    /**
     * Checks if the given message has a non-null, non-empty
     * attachment
     * @param message The message to check
     * @return True, if the message has a non-empty attachment
     */
    static boolean hasAttachment(MailMessage message) {
        return message.attachment != null && !message.attachment.isEmpty();
    }

    /**
     * Checks if the given attachment is empty or null
     * @param attachment The attachment to check
     * @return True, if the attachment is null or empty
     */
    static boolean isEmpty(MailAttachment attachment) {
        return attachment == null || attachment.isEmpty();
    }

    /**
     * A piece of data that can be attached to a messsage
     */
    class MailAttachment implements JsonSerializable {
        public ItemStack item;
        public int rhines, gems;
        public String tag;

        public static MailAttachment load(JsonElement element) {
            if (element == null) {
                return null;
            }

            MailAttachment result = new MailAttachment();

            if (element.isJsonPrimitive()) {
                result.item = JsonUtils.readItem(element);
                return result;
            }

            if(element.isJsonArray()) {
                int[] arr = JsonUtils.readIntArray(element.getAsJsonArray());
                result.rhines = arr[0];
                result.gems = arr[1];

                return result;
            }

            JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());
            result.item = json.getItem("item");
            result.tag = json.getString("tag", null);

            if (json.has("currencies")) {
                int[] arr = JsonUtils.readIntArray(json.getArray("currencies"));
                result.rhines = arr[0];
                result.gems = arr[1];
            }

            return result;
        }

        public static MailAttachment item(ItemStack item) {
            MailAttachment attachment = new MailAttachment();
            attachment.item = item;

            return attachment;
        }

        public static MailAttachment rhines(int rhines) {
            MailAttachment attachment = new MailAttachment();
            attachment.rhines = rhines;

            return attachment;
        }

        public static MailAttachment gems(int gems) {
            MailAttachment attachment = new MailAttachment();
            attachment.gems = gems;

            return attachment;
        }

        public static MailAttachment of(ItemStack item, int rhines, int gems) {
            MailAttachment attachment = new MailAttachment();
            attachment.gems = gems;
            attachment.rhines = rhines;
            attachment.item = item;

            return attachment;
        }

        public void writeHover(ComponentWriter writer) {
            if (hasItem()) {
                writer.newLine();
                writer.write(Component.text("Item: ").append(FtcFormatter.itemAndAmount(item).color(NamedTextColor.YELLOW)));
            }

            if (rhines > 0) {
                writer.newLine();
                writer.write(
                        Component.text("Rhines: ")
                                .append(FtcFormatter.rhines(rhines).color(NamedTextColor.YELLOW))
                );
            }

            if (gems > 0) {
                writer.newLine();
                writer.write(
                        Component.text("Gems: ")
                                .append(FtcFormatter.gems(gems).color(NamedTextColor.YELLOW))
                );
            }

            if (Crown.inDebugMode() && !FtcUtils.isNullOrBlank(tag)) {
                writer.newLine();
                writer.write(
                        Component.text("Tag: ")
                                .append(Component.text(tag, NamedTextColor.YELLOW))
                );
            }
        }

        public Component claimText() {
            TextComponent.Builder builder = Component.text()
                    .color(NamedTextColor.GRAY)
                    .append(Component.translatable("mail.claimed"))
                    .append(Component.space());

            boolean shouldAddComma = false;

            if (hasItem()) {
                shouldAddComma = true;

                builder.append(FtcFormatter.itemAndAmount(item).color(NamedTextColor.YELLOW));
            }

            if (rhines > 0) {
                if (shouldAddComma) builder.append(Component.text(", "));
                shouldAddComma = true;

                builder.append(FtcFormatter.rhines(rhines).color(NamedTextColor.GOLD));
            }

            if (gems > 0) {
                if (shouldAddComma) builder.append(Component.text(", "));
                shouldAddComma = true;

                builder.append(FtcFormatter.gems(gems).color(NamedTextColor.YELLOW));
            }

            return builder.build();
        }

        public void claim(CrownUser user) {
            if (rhines > 0) user.addBalance(rhines);
            if (gems > 0) user.addGems(gems);

            if (hasItem()) {
                user.getInventory().addItem(item.clone());
            }
        }

        public void testClaimable(CrownUser user) throws CommandSyntaxException {
            if(hasItem() && user.getInventory().firstEmpty() == -1) {
                throw FtcExceptionProvider.inventoryFull();
            }
        }

        public boolean hasItem() {
            return !ItemStacks.isEmpty(item);
        }

        public boolean isEmpty() {
            return rhines < 0 && gems < 0 && !hasItem();
        }

        @Override
        public JsonElement serialize() {
            if (isEmpty()) return null;

            JsonWrapper json = JsonWrapper.empty();

            if (hasItem()) {
                json.addItem("item", item);
            }

            if(rhines > 0 || gems > 0) {
                json.add("currencies", JsonUtils.writeIntArray(rhines, gems));
            }

            if(!FtcUtils.isNullOrBlank(tag)) {
                json.add("tag", tag);
            }

            return json.getSource();
        }
    }
}