package net.forthecrown.economy.shops;

import com.google.gson.JsonElement;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.Tag;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.util.UUID;

public record HistoryEntry(long date, UUID customer, int amount, int earned, boolean wasBuy) {
    public HistoryEntry(long date, UUID customer, int amount, int earned, Type type) {
        this(date, customer, amount, earned, type == Type.BUY);
    }

    public Component display(SignShop shop) {
        return Component.translatable("shops.history." + (wasBuy ? "buy" : "sell"),
                NamedTextColor.GRAY,
                customerDisplay().color(NamedTextColor.GOLD),
                FtcFormatter.itemAndAmount(shop.getInventory().getExampleItem(), amount).color(NamedTextColor.YELLOW),
                FtcFormatter.rhines(earned).color(NamedTextColor.GOLD),
                FtcFormatter.formatDate(date).color(NamedTextColor.YELLOW)
        );
    }

    public Type type() {
        return wasBuy ? Type.BUY : Type.SELL;
    }

    public Component customerDisplay() {
        if(UserManager.isPlayerID(customer)) {
            CrownUser user = UserManager.getUser(customer);
            return user.nickDisplayName();
        }

        Entity entity = Bukkit.getEntity(customer);
        if(entity == null) return Component.translatable("shops.history.unknown");

        return FtcFormatter.displayName(entity);
    }

    public static HistoryEntry ofLegacyJson(JsonElement element) {
        JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());

        return new HistoryEntry(
                json.getLong("date"),
                json.getUUID("customer"),
                json.getInt("amount"),
                json.getInt("earned"),
                json.getEnum("type", Type.class, Type.BUY)
        );
    }


    public static HistoryEntry of(Tag t) {
        return of(((LongArrayTag) t).getAsLongArray());
    }

    public static HistoryEntry of(long[] data) {
        Validate.isTrue(data.length >= 6, "Invalid data size");

        return new HistoryEntry(
                data[0],
                new UUID(data[1], data[2]),
                (int) data[3],
                (int) data[4],
                Type.values()[(int) data[5]]
        );
    }

    public long[] toArray() {
        return new long[] {
                date,
                customer.getMostSignificantBits(),
                customer.getLeastSignificantBits(),
                amount,
                earned,
                type().ordinal()
        };
    }

    public LongArrayTag save() {
        return new LongArrayTag(toArray());
    }

    public enum Type {
        SELL,
        BUY;
    }
}