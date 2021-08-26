package net.forthecrown.user.enums;

import com.google.gson.JsonElement;
import net.forthecrown.economy.selling.SellAmountItem;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a player's sell amount in the {@link net.forthecrown.economy.selling.SellShops}
 * @see net.forthecrown.economy.selling.SellShops
 */
public enum SellAmount implements JsonSerializable {
    PER_1 ((byte) 1, "Sell per 1", 17),
    PER_16 ((byte) 16, "Sell per 16", 26),
    PER_64 ((byte) 64, "Sell per stack", 35),
    ALL ((byte) 1, "Sell all", 44);

    public final byte value;
    public final String text;
    public final SellAmountItem invOption;

    SellAmount(byte i, String text, int slot) {
        this.text = text;

        value = i;
        invOption = new SellAmountItem(this, slot);
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

    public Component loreThing(){
        return Component.text(name().toLowerCase().replaceAll("per_", ""));
    }

    /**
     * Gets the sell amount from a byte
     * @param i The byte to get from
     * @return The sell amount of the corresponding byte, or null if one wasn't found
     */
    public static @Nullable SellAmount fromInt(byte i){
        return switch (i) {
            case 64 -> PER_64;
            case 16 -> PER_16;
            case 1 -> PER_1;
            case -1 -> ALL;
            default -> null;
        };
    }

    @Override
    public JsonElement serialize() {
        return JsonUtils.writeEnum(this);
    }
}
