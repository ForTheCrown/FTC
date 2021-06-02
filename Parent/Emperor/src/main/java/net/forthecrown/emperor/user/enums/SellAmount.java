package net.forthecrown.emperor.user.enums;

import com.google.gson.JsonElement;
import net.forthecrown.emperor.serialization.JsonSerializable;
import net.forthecrown.emperor.utils.JsonUtils;
import net.forthecrown.emperor.economy.SellShop;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a player's sell amount in the
 * @see SellShop
 */
public enum SellAmount implements JsonSerializable {
    PER_64 ((byte) 64, "Sell per stack"),
    PER_16 ((byte) 16, "Sell per 16"),
    PER_1 ((byte) 1, "Sell per 1"),
    ALL ((byte) 1, "Sell all");

    public final byte value;
    public final String text;

    SellAmount(byte i, String text) {
        this.value = i;
        this.text = text;
    }

    /**
     * Gets the sell amount as an integer
     * <p>
     * @see SellAmount#PER_1 and {@link SellAmount#ALL} return the same value, 1
     * </p>
     * @return A numeric representation of the sell amount
     */
    public Byte getValue(){
        return value;
    }

    /**
     * Gets the sell amount from a byte
     * @param i The byte to get from
     * @return The sell amount of the corresponding byte, or null if one wasn't found
     */
    public static @Nullable SellAmount fromInt(byte i){
        switch (i){
            case 64: return PER_64;
            case 16: return PER_16;
            case 1: return PER_1;
            case -1: return ALL;
            default: return null;
        }
    }

    @Override
    public JsonElement serialize() {
        return JsonUtils.serializeEnum(this);
    }
}
