package net.forthecrown.user;

import com.google.gson.JsonElement;
import lombok.Getter;
import net.forthecrown.economy.selling.SellAmountItem;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.text.Component;

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

    @Getter
    private final byte value;
    @Getter
    private final String text;
    @Getter
    private final SellAmountItem invOption;

    SellAmount(int i, String text, int slot) {
        this.text = text;

        value = (byte) i;
        invOption = new SellAmountItem(this, slot);
    }

    public byte getItemAmount() {
        return (byte) Math.max(1, value);
    }

    public Component loreThing() {
        return Component.text(name().toLowerCase().replaceAll("per_", ""));
    }

    @Override
    public JsonElement serialize() {
        return JsonUtils.writeEnum(this);
    }
}