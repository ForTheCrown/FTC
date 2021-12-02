package net.forthecrown.inventory;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.manager.UserManager;
import net.forthecrown.utils.LoreBuilder;
import net.kyori.adventure.text.Component;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.UUID;

public abstract class RoyalItem {
    private final String tagKey;
    private UUID owner;
    protected final ItemStack item;
    private int loreStart;
    private int loreEnd;

    public RoyalItem(ItemStack item, String tagKey) {
        this.item = item;
        this.tagKey = tagKey;
    }

    public RoyalItem(UUID owner, ItemStack item, String tagKey) {
        this.owner = owner;
        this.item = item;
        this.tagKey = tagKey;
    }

    protected void load() {
        CompoundTag tag = FtcItems.getTagElement(item.getItemMeta(), tagKey);
        this.owner = tag.getUUID("owner");
        this.loreStart = tag.getInt("lore_start");
        this.loreEnd = tag.getInt("lore_end");

        readNBT(tag);
    }

    public UUID getOwner() {
        return owner;
    }

    public void update() {
        ItemMeta meta = item.getItemMeta();

        List<Component> lore = meta.hasLore() ? meta.lore() : new ObjectArrayList<>();

        LoreBuilder loreBuilder = new LoreBuilder();
        createLore(loreBuilder);

        List<Component> extraLore = loreBuilder.getLore();

        //Remove preexisting lore created by the item
        int maxIndex = lore.isEmpty() ? 0 : lore.size();
        lore.subList(loreStart, Math.min(loreEnd++, maxIndex)).clear();

        loreStart = lore.isEmpty() ? 0 : loreStart;
        loreEnd = loreStart + extraLore.size();

        //Add newly created lore
        lore.addAll(loreStart, extraLore);

        //Save tags to item's NBT
        CompoundTag tag = new CompoundTag();
        tag.putInt("lore_start", loreStart);
        tag.putInt("lore_end", loreEnd);
        tag.putUUID("owner", owner);

        onUpdate(item, meta, tag);

        meta.lore(lore);
        FtcItems.setTagElement(meta, tagKey, tag);
        item.setItemMeta(meta);
    }

    protected abstract void onUpdate(ItemStack item, ItemMeta meta, CompoundTag nbt);

    protected abstract void readNBT(CompoundTag tag);
    protected abstract void createLore(LoreBuilder lore);

    public CrownUser getOwnerUser() {
        return !hasPlayerOwner() ? null : UserManager.getUser(getOwner());
    }

    public boolean hasPlayerOwner() {
        return owner != null && owner != Util.NIL_UUID;
    }

    public ItemStack getItem() {
        return item;
    }
}
