package net.forthecrown.user.data;

import com.google.gson.JsonElement;
import lombok.Getter;
import net.forthecrown.utils.JsonSerializable;
import net.forthecrown.utils.io.JsonUtils;
import net.kyori.adventure.text.Component;

import static net.kyori.adventure.text.Component.text;

@Getter
public enum SellAmount implements JsonSerializable {
    PER_1   ( 1, "Sell per 1"),
    PER_16  (16, "Sell per 16"),
    PER_64  (64, "Sell per stack"),
    ALL     (-1, "Sell all");

    private final byte value;
    private final String sellPerText;

    SellAmount(int i, String text) {
        value = (byte) i;
        this.sellPerText = text;
    }

    public int getItemAmount() {
        return Math.max(1, getValue());
    }

    public Component amountText() {
        return text(value == -1 ? "All" : (value + ""));
    }

    @Override
    public JsonElement serialize() {
        return JsonUtils.writeEnum(this);
    }
}