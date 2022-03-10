package net.forthecrown.economy.shops;

import net.forthecrown.inventory.ItemStacks;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftInventoryCustom;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class FtcShopInventory extends CraftInventoryCustom implements ShopInventory {

    private final FtcSignShop owningShop;
    private ItemStack exampleItem;

    public FtcShopInventory(FtcSignShop signShop){
        super(signShop, InventoryType.CHEST, "Shop Contents");
        owningShop = signShop;
    }

    @Override
    public List<ItemStack> getShopContents(){
        List<ItemStack> tempList = new ArrayList<>();

        for (ItemStack i: getContents()){
            if(ItemStacks.isEmpty(i)) continue;
            tempList.add(i);
        }

        return tempList;
    }

    @Override
    public void setShopContents(Collection<ItemStack> stacks){
        clear();

        for (ItemStack i: stacks){
            if(ItemStacks.isEmpty(i)) continue;
            addItem(i);
        }
    }

    @Nonnull
    @Override
    public FtcSignShop getHolder() {
        return owningShop;
    }

    @Override
    public boolean isFull() {
        return firstEmpty() == -1;
    }

    @Override
    public ItemStack getExampleItem() {
        return exampleItem == null ? null : exampleItem.clone();
    }

    @Override
    public void setExampleItem(ItemStack exampleItem) {
        this.exampleItem = exampleItem;
    }

    @Override
    public void checkStock(){
        if(getExampleItem() == null) return;
        if(isFull() && !owningShop.getType().isBuyType()) getHolder().setOutOfStock(true);
        getHolder().setOutOfStock(containsAtLeast(getExampleItem(), getExampleItem().getAmount()));
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