package net.forthecrown.economy.shops;

import lombok.Setter;
import net.forthecrown.core.Crown;
import net.forthecrown.inventory.ItemStacks;
import net.forthecrown.utils.TagUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftInventoryCustom;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class FtcShopInventory extends CraftInventoryCustom implements ShopInventory, ShopComponent {

    private final FtcSignShop owningShop;

    @Setter
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
        if(ItemStacks.isEmpty(exampleItem)) {
            Crown.logger().warn("Found null example item in shop {}", getHolder().getName());
            return null;
        }

        return exampleItem.clone();
    }

    @Override
    public boolean inStock() {
        // Null example item shops are always out of stock
        if(getExampleItem() == null) return false;

        // If it's full and a sell shop, then it's considered
        // out of stock due to the fact it cannot operate
        if(isFull() && !owningShop.getType().isBuyType()) {
            return false;
        }

        // Admin shops are never out of stock
        if(getHolder().getType().isAdmin()) {
            return true;
        }

        return containsAtLeast(getExampleItem(), getExampleItem().getAmount());
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

    @Override
    public String getSerialKey() {
        return "inventory";
    }

    @Nullable
    @Override
    public Tag save() {
        CompoundTag invTag = new CompoundTag();
        invTag.put("exampleItem", TagUtil.writeItem(getExampleItem()));

        if (!isEmpty()) {
            ListTag items = new ListTag();

            for (ItemStack i: getShopContents()) {
                items.add(TagUtil.writeItem(i));
            }

            invTag.put("items", items);
        }

        return invTag;
    }

    @Override
    public void load(@Nullable Tag t) {
        CompoundTag tag = (CompoundTag) t;
        exampleItem = TagUtil.readItem(tag.get("exampleItem"));

        if(tag.contains("items", Tag.TAG_LIST)) {
            setShopContents(TagUtil.readList(tag.getList("items", Tag.TAG_COMPOUND), TagUtil::readItem));
        }
    }
}