package net.forthecrown.economy.shops;

import com.google.gson.JsonElement;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.util.UUID;

public record HistoryEntry(long date, UUID customer, int amount, int earned, boolean wasBuy) implements JsonSerializable {
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

    public static HistoryEntry of(JsonElement element) {
        JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());

        return new HistoryEntry(
                json.getLong("date"),
                json.getUUID("customer"),
                json.getInt("amount"),
                json.getInt("earned"),
                json.getEnum("type", Type.class, Type.BUY)
        );
    }

    @Override
    public JsonElement serialize() {
        JsonWrapper json = JsonWrapper.empty();

        json.add("date", date);
        json.addUUID("customer", customer);
        json.add("amount", amount);
        json.add("earned", earned);

        if(!wasBuy) {
            json.addEnum("type", type());
        }

        return json.getSource();
    }

    public enum Type {
        SELL,
        BUY;
    }
}
