package net.forthecrown.user.data;

import com.google.gson.JsonElement;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.Crown;
import net.forthecrown.text.Messages;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextJoiner;
import net.forthecrown.text.format.UnitFormat;
import net.forthecrown.text.writer.TextWriter;
import net.forthecrown.text.writer.TextWriters;
import net.forthecrown.utils.JsonSerializable;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.user.User;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.UnaryOperator;

/**
 * A piece of data that can be attached to a messsage
 */
@Getter @Setter
public class MailAttachment implements JsonSerializable, HoverEventSource<Component> {
    /**
     * The item the attachment gives upon being
     * claimed
     */
    private ItemStack item;

    /**
     * The amount of rhines the attachment
     * gives upon being claimed
     */
    private int rhines;

    /**
     * The gems the attachment gives when
     * being claimed
     */
    private int gems;

    /**
     * A tag the attachment holds, allows for
     * objects to filter mail, find attachments
     * they've sent and modify/remove them.
     */
    private String tag;

    /**
     * True, if this attachment's rewards have been claimed
     */
    private boolean claimed;

    /**
     * Loads an attachment from the given json element
     * @param element The element to load from
     * @return The loaded attachment, or null, if the given element was null
     */
    public static MailAttachment load(JsonElement element) {
        if (element == null) {
            return null;
        }

        MailAttachment result = new MailAttachment();

        if (element.isJsonPrimitive()) {
            result.item = JsonUtils.readItem(element);
            return result;
        }

        if (element.isJsonArray()) {
            int[] arr = JsonUtils.readIntArray(element.getAsJsonArray());
            result.rhines = arr[0];
            result.gems = arr[1];

            return result;
        }

        JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());
        result.item = json.getItem("item");
        result.tag = json.getString("tag", null);

        // This is here for backwards compatibility
        // Rhines and Gems are no longer serialized like this
        if (json.has("currencies")) {
            int[] arr = JsonUtils.readIntArray(json.getArray("currencies"));
            result.rhines = arr[0];
            result.gems = arr[1];
        }

        if (json.has("rhines")) {
            result.rhines = json.getInt("rhines");
        }

        if (json.has("gems")) {
            result.gems = json.getInt("gems");
        }

        if (json.has("claimed")) {
            result.setClaimed(json.getBool("claimed"));
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

    /**
     * Checks if the given attachment is empty or null
     * @param attachment The attachment to check
     * @return True, if the attachment is null or empty
     */
    public static boolean isEmpty(MailAttachment attachment) {
        return attachment == null || attachment.isEmpty();
    }

    public void writeHover(TextWriter writer) {
        writer.write(claimed ? "Items claimed" : "Items not claimed");

        if (hasItem()) {
            writer.formattedLine("Item: &e{0, item}", item);
        }

        if (rhines > 0) {
            writer.formattedLine("Rhines: &e{0, rhines}", rhines);
        }

        if (gems > 0) {
            writer.formattedLine("Gems: &e{0, gems}", gems);
        }

        if (Crown.inDebugMode() && !Util.isNullOrBlank(tag)) {
            writer.formattedLine("Tag: '&e{0}&r'", tag);
        }
    }

    public Component claimText() {
        TextJoiner joiner = TextJoiner.onComma()
                .setColor(NamedTextColor.GRAY)
                .setPrefix(Messages.CLAIMED);

        if (hasItem()) {
            joiner.add(Text.itemAndAmount(item)
                    .color(NamedTextColor.YELLOW)
            );
        }

        if (rhines > 0) {
            joiner.add(UnitFormat.rhines(rhines)
                    .color(NamedTextColor.GOLD)
            );
        }

        if (gems > 0) {
            joiner.add(UnitFormat.gems(gems)
                    .color(NamedTextColor.YELLOW)
            );
        }

        return joiner.asComponent();
    }

    public void claim(User user) {
        if (rhines > 0) {
            user.addBalance(rhines);
        }

        if (gems > 0) {
            user.addGems(gems);
        }

        if (hasItem()) {
            user.getInventory().addItem(item.clone());
        }

        setClaimed(true);
    }

    public void testClaimable(User user) throws CommandSyntaxException {
        if (hasItem() && user.getInventory().firstEmpty() == -1) {
            throw Exceptions.INVENTORY_FULL;
        }
    }

    public boolean hasItem() {
        return !ItemStacks.isEmpty(item);
    }

    public boolean isEmpty() {
        return rhines <= 0 && gems <= 0 && !hasItem();
    }

    @Override
    public JsonElement serialize() {
        if (isEmpty()) {
            return null;
        }

        JsonWrapper json = JsonWrapper.create();

        if (hasItem()) {
            json.addItem("item", item);
        }

        if (gems > 0) {
            json.add("gems", gems);
        }

        if (rhines > 0) {
            json.add("rhines", rhines);
        }

        if (!Util.isNullOrBlank(tag)) {
            json.add("tag", tag);
        }

        json.add("claimed", claimed);
        return json.getSource();
    }

    @Override
    public @NotNull HoverEvent<Component> asHoverEvent(@NotNull UnaryOperator<Component> op) {
        var writer = TextWriters.newWriter();
        writeHover(writer);

        return writer.asComponent()
                .asHoverEvent(op);
    }
}