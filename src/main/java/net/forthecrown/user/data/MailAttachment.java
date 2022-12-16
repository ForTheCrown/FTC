package net.forthecrown.user.data;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.FTC;
import net.forthecrown.core.Messages;
import net.forthecrown.core.script2.Script;
import net.forthecrown.economy.TransactionType;
import net.forthecrown.economy.Transactions;
import net.forthecrown.user.User;
import net.forthecrown.utils.JsonSerializable;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.TextJoiner;
import net.forthecrown.utils.text.format.UnitFormat;
import net.forthecrown.utils.text.writer.TextWriter;
import net.forthecrown.utils.text.writer.TextWriters;
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
    /* ----------------------------- CONSTANTS ------------------------------ */

    static final String
            KEY_ITEM = "item",
            KEY_RHINES = "rhines",
            KEY_GEMS = "gems",
            KEY_TAG = "tag",
            KEY_COMMANDS = "onClaim",
            KEY_CLAIMED = "claimed",
            KEY_SCRIPT = "script";

    /* -------------------------- INSTANCE FIELDS --------------------------- */

    /** The item the attachment gives upon being claimed */
    private ItemStack item;

    /** The amount of rhines the attachment gives upon being claimed */
    private int rhines;

    /** The gems the attachment gives when being claimed */
    private int gems;

    /**
     * A tag the attachment holds, allows for
     * objects to filter mail, find attachments
     * they've sent and modify/remove them.
     */
    private String tag;

    /** True, if this attachment's rewards have been claimed */
    private boolean claimed;

    /**
     * The Attachment's script, ran when claimed.
     * <p>
     * This script is executed by calling a 'onMailClaim' method with
     * 1 parameter. The user object that's claiming the attachment is
     * passed directly to this script
     */
    private String script;

    /* ------------------------ STATIC CONSTRUCTORS ------------------------- */

    public static MailAttachment item(ItemStack item) {
        MailAttachment attachment = new MailAttachment();
        attachment.item = item;

        return attachment;
    }

    /* ------------------------------ METHODS ------------------------------- */

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
            writer.field("Item", Text.itemDisplayName(item));
        }

        if (rhines > 0) {
            writer.field("Rhines", UnitFormat.rhines(rhines));
        }

        if (gems > 0) {
            writer.field("Gems", UnitFormat.gems(gems));
        }

        if (FTC.inDebugMode()) {
            if (!Strings.isNullOrEmpty(tag)) {
                writer.field("Tag", tag);
            }

            if (!Strings.isNullOrEmpty(script)) {
                writer.field("Script", script);
            }
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

            Transactions.builder()
                    .type(TransactionType.MAIL_ATTACHMENT)
                    .amount(rhines)
                    .target(user.getUniqueId())
                    .log();
        }

        if (gems > 0) {
            user.addGems(gems);
        }

        if (hasItem()) {
            Util.giveOrDropItem(
                    user.getInventory(),
                    user.getLocation(),
                    item.clone()
            );
        }

        setClaimed(true);

        if (!Strings.isNullOrEmpty(script)) {
            Script.run(script, "onMailClaim", user);
        }
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
        return rhines <= 0
                && gems <= 0
                && !hasItem()
                && Strings.isNullOrEmpty(script);
    }

    @Override
    public @NotNull HoverEvent<Component> asHoverEvent(@NotNull UnaryOperator<Component> op) {
        var writer = TextWriters.newWriter();
        writeHover(writer);

        return writer.asComponent()
                .asHoverEvent(op);
    }

    /* --------------------------- SERIALIZATION ---------------------------- */

    @Override
    public JsonElement serialize() {
        if (isEmpty()) {
            return null;
        }

        JsonWrapper json = JsonWrapper.create();

        if (hasItem()) {
            json.addItem(KEY_ITEM, item);
        }

        if (gems > 0) {
            json.add(KEY_GEMS, gems);
        }

        if (rhines > 0) {
            json.add(KEY_RHINES, rhines);
        }

        if (!Strings.isNullOrEmpty(tag)) {
            json.add(KEY_TAG, tag);
        }

        if (!Strings.isNullOrEmpty(script)) {
            json.add(KEY_SCRIPT, script);
        }

        json.add(KEY_CLAIMED, claimed);
        return json.getSource();
    }

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
        result.item = json.getItem(KEY_ITEM);
        result.tag = json.getString(KEY_TAG, null);

        // This is here for backwards compatibility
        // Rhines and Gems are no longer serialized like this
        if (json.has("currencies")) {
            int[] arr = JsonUtils.readIntArray(json.getArray("currencies"));
            result.rhines = arr[0];
            result.gems = arr[1];
        }

        if (json.has(KEY_RHINES)) {
            result.rhines = json.getInt(KEY_RHINES);
        }

        if (json.has(KEY_GEMS)) {
            result.gems = json.getInt(KEY_GEMS);
        }

        if (json.has(KEY_CLAIMED)) {
            result.setClaimed(json.getBool(KEY_CLAIMED));
        }

        if (json.has(KEY_SCRIPT)) {
            result.setScript(json.getString(KEY_SCRIPT));
        }

        return result;
    }
}