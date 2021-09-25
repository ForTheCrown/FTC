package net.forthecrown.user;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang.Validate;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FtcUserGrave extends AbstractUserAttachment implements Grave {

    public List<ItemStack> items = new ArrayList<>();

    FtcUserGrave(FtcUser user){
        super(user);
    }

    @Override
    public void giveItems() throws RoyalCommandException, UserNotOnlineException {
        user.checkOnline();

        int freeSpace = 0;
        for (ItemStack i: getUser().getPlayer().getInventory().getStorageContents()){
            if(i != null) continue;
            freeSpace++;
        }

        if(freeSpace < getItems().size()) throw FtcExceptionProvider.inventoryFull();

        for (ItemStack i: getItems()){
            getUser().getPlayer().getInventory().addItem(i);
        }

        getUser().sendMessage(Component.translatable("user.grave.gotItems").color(NamedTextColor.GRAY));
        items.clear();
    }

    @Override
    public boolean isEmpty(){
        return items.isEmpty();
    }

    @Override
    public void addItem(@NotNull ItemStack item){
        Validate.notNull(items, "The Item may not be null");
        items.add(item);
    }

    @Override
    public void addItem(@NotNull ItemStack... items){
        Validate.notNull(items, "The Item list may not be null");
        this.items.addAll(Arrays.asList(items));
    }

    @Override
    public void setItems(@NotNull List<ItemStack> items){
        Validate.notNull(items, "The Item list may not be null");
        this.items = items;
    }

    @Override
    public List<ItemStack> getItems(){
        return items;
    }

    @Override
    public JsonArray serialize() {
        if(items.isEmpty()) return null;

        JsonArray array = new JsonArray();

        for (ItemStack i: items){
            array.add(JsonUtils.writeItem(i));
        }

        return array;
    }

    @Override
    public void deserialize(JsonElement element) {
        items.clear();

        if(element == null) return;
        JsonArray array = element.getAsJsonArray();
        array.forEach(e -> {
            items.add(JsonUtils.readItem(e));
        });
    }
}
