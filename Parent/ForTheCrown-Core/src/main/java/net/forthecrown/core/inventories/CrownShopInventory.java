package net.forthecrown.core.inventories;

import net.forthecrown.core.api.ShopInventory;
import net.forthecrown.core.api.SignShop;
import net.forthecrown.core.files.CrownSignShop;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftInventoryCustom;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CrownShopInventory extends CraftInventoryCustom implements ShopInventory {

    private final CrownSignShop owningShop;
    private ItemStack exampleItem;

    public CrownShopInventory(CrownSignShop signShop){
        super(signShop, InventoryType.CHEST, "Shop Contents");
        owningShop = signShop;
    }

    @Override
    public List<ItemStack> getShopContents(){
        List<ItemStack> tempList = new ArrayList<>();

        for (ItemStack i: getContents()){
            if(i == null) continue;
            tempList.add(i);
        }

        return tempList;
    }

    @Override
    public void setShopContents(List<ItemStack> stacks){
        clear();

        for (ItemStack i: stacks){
            if(i == null) continue;
            addItem(i);
        }
    }

    @Nonnull
    @Override
    public CrownSignShop getHolder() {
        return owningShop;
    }

    @Override
    public boolean isFull() {
        return firstEmpty() == -1;
    }

    @Override
    public ItemStack getExampleItem() {
        try {
            return exampleItem.clone();
        } catch (Exception e){
            return null;
        }
    }

    @Override
    public int getTotalItemAmount(){
        int amount = 0;

        for (ItemStack i: getContents()){
            if(i == null) continue;

            amount += i.getAmount();
        }

        return amount;
    }

    @Override
    public void checkStock(){
        if(getExampleItem() == null) return;
        getHolder().setOutOfStock(getTotalItemAmount() < getExampleItem().getAmount());
    }

    @Override
    public void setExampleItem(ItemStack exampleItem) {
        this.exampleItem = exampleItem;
    }

    @Nonnull
    @Override
    public HashMap<Integer, ItemStack> removeItemAnySlot(ItemStack... items) throws IllegalArgumentException {
        return removeItem(items);
    }

    @Nonnull
    @Override
    public SignShop getHolder(boolean useSnapshot) {
        return owningShop;
    }
}