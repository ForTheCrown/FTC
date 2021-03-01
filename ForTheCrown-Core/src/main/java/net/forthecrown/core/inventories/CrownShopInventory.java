package net.forthecrown.core.inventories;

import net.forthecrown.core.api.ShopInventory;
import net.forthecrown.core.api.SignShop;
import net.forthecrown.core.files.CrownSignShop;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftInventoryCustom;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

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
    public HashMap<Integer, ItemStack> addItem(ItemStack... items) {
        owningShop.setOutOfStock(false);
        return super.addItem(items);
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

    @Override
    public SignShop getHolder() {
        return owningShop;
    }

    @Override
    public boolean isFull() {
        return firstEmpty() == -1;
    }

    @Override
    public ItemStack getExampleItem() {
        if(exampleItem == null) throw new NullPointerException(owningShop.getName() + " has null example item");
        return exampleItem.clone();
    }

    @Override
    public void setExampleItem(ItemStack exampleItem) {
        this.exampleItem = exampleItem;
    }

    @Override
    public void setExampleItemAndAdd(ItemStack exampleItem) {
        setExampleItem(exampleItem);

        addItem(exampleItem);
    }

    @Override
    public HashMap<Integer, ItemStack> removeItemAnySlot(ItemStack... items) throws IllegalArgumentException {
        return removeItem(items);
    }

    @Override
    public SignShop getHolder(boolean useSnapshot) {
        return owningShop;
    }
}
