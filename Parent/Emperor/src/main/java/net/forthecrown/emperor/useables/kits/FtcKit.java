package net.forthecrown.emperor.useables.kits;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.nbt.NbtHandler;
import net.forthecrown.emperor.useables.CheckableBase;
import net.forthecrown.emperor.useables.UsageCheck;
import net.forthecrown.emperor.utils.ChatFormatter;
import net.forthecrown.emperor.utils.JsonUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class FtcKit extends CheckableBase implements Kit{
    private List<ItemStack> items = new ArrayList<>();
    private final Key key;

    public FtcKit(Key key, List<ItemStack> items){
        this.key = key;
        this.items = items;
    }

    public FtcKit(Key key, JsonElement element) throws CommandSyntaxException {
        JsonObject json = element.getAsJsonObject();
        this.key = key;

        JsonArray itemArray = json.getAsJsonArray("items");
        for (JsonElement e: itemArray){
            items.add(JsonUtils.deserializeItem(e));
        }

        reloadChecksFrom(json);
    }

    @Override
    public boolean attemptItemGiving(Player player) {
        if(!test(player)) return false;
        giveItems(player);
        return true;
    }

    @Override
    public void giveItems(Player player) {
        PlayerInventory inv = player.getInventory();

        for (ItemStack i: items){
            inv.addItem(i);
        }
    }

    @Override
    public List<ItemStack> getItems() {
        return items;
    }

    @Override
    public boolean test(Player player) {
        List<Consumer<Player>> onSuccess = new ArrayList<>();
        for (UsageCheck c: checks.values()){
            if(!c.test(player)){
                Component m = c.getPersonalizedFailMessage(player);
                if(m != null) player.sendMessage(m);
                return false;
            }

            Consumer<Player> s = c.onSuccess();
            if(s != null) onSuccess.add(s);
        }

        if(testSpace(player)){
            onSuccess.forEach(c -> c.accept(player));
            return true;
        }
        return false;
    }

    public boolean testSpace(Player player){
        if(!hasSpace(player.getInventory())){
            player.sendMessage(Component.text("No room in inventory"));
            return false;
        }
        return true;
    }

    @Override
    public boolean hasSpace(PlayerInventory inventory){
        int freeSlots = 0;

       for (ItemStack i: inventory){
           if(i == null || i.getType() == Material.AIR) freeSlots++;
       }

       return freeSlots >= items.size();
    }

    @Override
    public String getName() {
        return key.value();
    }

    @Override
    public boolean testSilent(Player player) {
        for (UsageCheck c: checks.values()){
            if(!c.test(player)) return false;
        }

        return hasSpace(player.getInventory());
    }

    @Override
    public JsonElement serialize() {
        JsonObject json = new JsonObject();

        json.add("key", new JsonPrimitive(key.asString()));

        JsonArray itemArray = new JsonArray();
        for (ItemStack i: items){
            itemArray.add(new JsonPrimitive(NbtHandler.ofItem(i).serialize()));
        }

        json.add("items", itemArray);
        saveChecksInto(json);
        return json;
    }

    @Override
    public @NonNull Key key() {
        return key;
    }

    @Override
    public @NonNull HoverEvent<Component> asHoverEvent(@NonNull UnaryOperator<Component> op) {
        TextComponent.Builder builder = Component.text().append(Component.text("Items: "));

        for (ItemStack i: items){
            ItemMeta m = i.getItemMeta();
            Component name = m.hasDisplayName() ? m.displayName() : Component.text(ChatFormatter.normalEnum(i.getType()));

            builder
                    .append(Component.newline())
                    .append(name);
        }

        return builder.build().asHoverEvent(op);
    }

    @Override
    public void delete() {
        CrownCore.getKitRegistry().remove(key);
    }
}
