package net.forthecrown.core.inventories;

import net.forthecrown.core.api.ShopStock;
import net.forthecrown.core.api.SignShop;
import net.forthecrown.core.files.CrownSignShop;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CrownShopStock implements ShopStock {

    private List<ItemStack> contents = new ArrayList<>();
    private ItemStack exampleItem = null;
    private final CrownSignShop shop;

    public CrownShopStock(CrownSignShop shop){
        this.shop = shop;
    }

    @Override
    public boolean contains(Material material){
        for (ItemStack stack : getContents()){
            if(stack.getType() == material) return true;
        }
        return false;
    }

    @Override
    public boolean contains(Material material, @Nonnegative int amount){
        if(material == null || material == Material.AIR) return false;
        if(amount == 0) return true;
        for (ItemStack i : getContents()){
            if(i.getType() == material && (amount -= i.getAmount()) <= 0) return true;
        }

        return false;
    }

    @Override
    public boolean containsExampleItem(){
        return contains(getExampleItem().getType(), getExampleItem().getAmount());
    }

    @Override
    public void removeExampleItemAmount(){
        removeItem(getExampleItem().getType(), getExampleItem().getAmount());
    }

    @Override
    public void add(@Nonnull ItemStack stack){
        if(isFull()) throw new IllegalArgumentException("Contents is full!");
        if(isEmpty()){
            contents.add(stack);
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 27);

        ItemStack[] array = new ItemStack[27];
        inv.setContents(getContents().toArray(array));
        inv.addItem(stack);

        List<ItemStack> temp = new ArrayList<>();
        for (ItemStack i : inv.getContents()){
            if(i == null) continue;
            temp.add(i);
        }
        setContents(temp);
    }

    @Override
    public boolean isFull(){
        return contents.size() > 27;
    }

    @Override
    public boolean isEmpty(){
        return contents.isEmpty();
    }

    @Override
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

    @Override
    public List<ItemStack> getContents() {
        return contents;
    }
    @Override
    public void setContents(@Nonnull List<ItemStack> contents) {
        if(this.contents.size() < contents.size()) shop.setOutOfStock(false);
        this.contents = contents;
    }

    @Override
    public ItemStack getExampleItem() {
        if(exampleItem.getType() == Material.AIR) return null;
        return exampleItem.clone();
    }

    @Override
    public void setExampleItem(ItemStack exampleItem) {
        if(exampleItem == null || exampleItem.getType() == Material.AIR) throw new NullPointerException("A null item cannot be set as a shop's example item");
        this.exampleItem = exampleItem;
        shop.setOutOfStock(false);
    }

    @Override
    public void setExampleItemAndAdd(ItemStack exampleItem){
        setExampleItem(exampleItem);
        add(getExampleItem());
    }

    @Override
    public SignShop getOwningShop() {
        return shop;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CrownShopStock that = (CrownShopStock) o;
        return Objects.equals(getContents(), that.getContents()) &&
                Objects.equals(getExampleItem(), that.getExampleItem()) &&
                Objects.equals(shop, that.shop);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getContents(), shop);
    }

    @Override
    public String toString() {
        return "CrownShopStock{" +
                "contents=" + contents +
                ", exampleItem=" + exampleItem +
                ", shop=" + shop +
                '}';
    }

    @Override
    public void clear(){
        contents.clear();
    }
}
