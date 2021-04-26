package net.forthecrown.core.types.user;

import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.api.Grave;
import net.forthecrown.core.exceptions.UserNotOnlineException;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.Validate;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FtcUserGrave implements Grave {

    private final FtcUser user;
    private List<ItemStack> items = new ArrayList<>();

    FtcUserGrave(FtcUser user){
        this.user = user;
    }

    @Override
    public CrownUser getUser() {
        return user;
    }

    @Override
    public void giveItems(){
        if(!user.isOnline()) throw new UserNotOnlineException(user);

        int freeSpace = 0;
        for (ItemStack i: getUser().getPlayer().getInventory().getStorageContents()){
            if(i != null) continue;
            freeSpace++;
        }

        if(freeSpace < getItems().size()){
            getUser().sendMessage(ChatColor.RED + "[FTC] " + ChatColor.GRAY + "You don't have enough space in your inventory.");
            return;
        }

        for (ItemStack i: getItems()){
            getUser().getPlayer().getInventory().addItem(i);
        }

        getUser().sendMessage("&7You retrieved all the items from your grave.");
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
}
