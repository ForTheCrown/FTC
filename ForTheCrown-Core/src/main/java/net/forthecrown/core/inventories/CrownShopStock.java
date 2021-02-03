package net.forthecrown.core.inventories;

import net.forthecrown.core.api.ShopStock;
import net.forthecrown.core.files.CrownSignShop;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class CrownShopStock implements ShopStock {

    private List<ItemStack> contents = new ArrayList<>();
    private ItemStack exampleItem = null;
    private final CrownSignShop shop;

    public CrownShopStock(CrownSignShop shop){
        this.shop = shop;
    }

    public boolean contains(Material material){
        for (ItemStack stack : contents){
            if(stack.getType() == material) return true;
        }
        return false;
    }

    public boolean contains(Material material, @Nonnegative int amount){
        if(material == null || material == Material.AIR) return false;
        if(amount == 0) return true;
        for (ItemStack i : contents){
            if(i.getType() == material && (amount -= i.getAmount()) <= 0) return true;
        }

        return false;
    }

    public boolean containsExampleItem(){
        return contains(exampleItem.getType(), exampleItem.getAmount());
    }

    public void removeExampleItemAmount(){
        removeItem(exampleItem.getType(), exampleItem.getAmount());
    }

    public void add(@Nonnull ItemStack stack){
        if(contents.isEmpty()){
            contents.add(stack.clone());
            return;
        }
        if(isFull()) throw new IllegalArgumentException("stock is full!");

        ItemStack fuckFuck = stack.clone();
        int lastItemAmount = getContents().get(getContents().size()-1).getAmount();

        if(lastItemAmount == getExampleItem().getMaxStackSize()) contents.add(stack.clone());
        else if(lastItemAmount < getExampleItem().getMaxStackSize()){
            int a = lastItemAmount - fuckFuck.getAmount();
        }
    }

    public boolean isFull(){
        return contents.size() > 27;
    }

    public boolean isEmpty(){
        return contents.isEmpty();
    }

    public void removeItem(Material material, @Nonnegative int amount){
        if(amount == 0 && !contains(material, amount)) throw new NullPointerException("There isn't enough items to remove in the inventory!");

        List<ItemStack> toRemove = new ArrayList<>();
        for (ItemStack stack : contents){
            if(stack.getAmount() >= amount){
                stack.setAmount(stack.getAmount() - amount);
                if(stack.getAmount() <= 0) toRemove.add(stack);
                break;
            } else {
                amount -= stack.getAmount();
                toRemove.add(stack);
            }
        }

        contents.removeAll(toRemove);
    }

    public List<ItemStack> getContents() {
        return contents;
    }
    public void setContents(@Nonnull List<ItemStack> contents) {
        if(this.contents.size() < contents.size()) shop.setOutOfStock(false);
        this.contents = contents;
    }

    public ItemStack getExampleItem() {
        if(exampleItem.getType() == Material.AIR) return null;
        return exampleItem.clone();
    }

    public void setExampleItem(ItemStack exampleItem) {
        if(exampleItem == null || exampleItem.getType() == Material.AIR) throw new NullPointerException("A null item cannot be set as a shop's example item");
        this.exampleItem = exampleItem;
        shop.setOutOfStock(false);
    }

    public void setExampleItemAndAdd(ItemStack exampleItem){
        if(exampleItem == null || exampleItem.getType() == Material.AIR) throw new NullPointerException("A null item cannot be set as a shop's example item");
        this.exampleItem = exampleItem;
        shop.setOutOfStock(false);
        contents.add(exampleItem);
    }

    public CrownSignShop getOwningShop() {
        return shop;
    }
}
