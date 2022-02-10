package net.forthecrown.user;

import com.google.gson.JsonElement;
import net.forthecrown.economy.selling.SellAmountItem;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a player's sell amount in the {@link net.forthecrown.economy.selling.SellShops}
 *
 * @see net.forthecrown.economy.selling.SellShops
 */
public enum SellAmount implements JsonSerializable {
    PER_1   ( 1, "Sell per 1",      17),
    PER_16  (16, "Sell per 16",     26),
    PER_64  (64, "Sell per stack",  35),
    ALL     (-1, "Sell all",        44);

    private final byte value;
    private final String text;
    private final SellAmountItem invOption;

    SellAmount(int i, String text, int slot) {
        this.text = text;

        value = (byte) i;
        invOption = new SellAmountItem(this, slot);
    }

    /**
     * Gets the sell amount from a byte
     *
     * @param i The byte to get from
     * @return The sell amount of the corresponding byte, or null if one wasn't found
     */
    public static @Nullable SellAmount fromInt(byte i) {
        return switch (i) {
            case 64 -> PER_64;
            case 16 -> PER_16;
            case 1 -> PER_1;
            case -1 -> ALL;
            default -> null;
        };
    }

    public byte getItemAmount() {
        return (byte) Math.max(1, value);
    }

    public byte getValue() {
        return value;
    }

    public SellAmountItem getInvOption() {
        return invOption;
    }

    public String getText() {
        return text;
    }

    public Component loreThing() {
        return Component.text(name().toLowerCase().replaceAll("per_", ""));
    }

    @Override
    public JsonElement serialize() {
        return JsonUtils.writeEnum(this);
    }
}
