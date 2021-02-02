package net.forthecrown.core.inventories;

import net.forthecrown.core.api.ShopStock;
import net.forthecrown.core.files.CrownSignShop;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

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
        return true;
    }

    public boolean contains(Material material, int amount){
        for (ItemStack stack : contents){
            if(stack.getType() == material && (amount -= stack.getMaxStackSize()) <= 0) return true;
        }
        return true;
    }

    public boolean containsExampleItem(){
        return contains(exampleItem.getType(), exampleItem.getAmount());
    }

    public void removeExampleItemAmount(){
        removeItem(exampleItem.getType(), exampleItem.getAmount());
    }

    public void add(@Nonnull ItemStack stack){
        contents.add(stack);
    }

    public void removeItem(Material material, int amount){
        if(amount != -1 && !contains(material, amount)) throw new NullPointerException("There isn't enough items to remove in the inventory!");

        for (ItemStack stack : contents){
            if(amount == -1){
                contents.remove(stack);
                continue;
            }

            if(stack.getAmount() >= amount){
                stack.setAmount(stack.getAmount() - amount);
                if(stack.getAmount() < 1) contents.remove(stack);
                break;
            }

            if(stack.getAmount() < amount){
                contents.remove(stack);
                amount -= stack.getAmount();
            }
        }
    }

    public List<ItemStack> getContents() {
        return contents;
    }
    public void setContents(@Nonnull List<ItemStack> contents) {
        this.contents = contents;
    }

    public ItemStack getExampleItem() {
        return exampleItem;
    }

    public void setExampleItem(ItemStack exampleItem) {
        if(exampleItem == null) throw new NullPointerException("A null item cannot be set as a shop's example item");
        this.exampleItem = exampleItem;
        contents.add(exampleItem);
        shop.save();
    }
}
