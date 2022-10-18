package net.forthecrown.inventory;

import lombok.Getter;
import net.forthecrown.dungeons.enchantments.FtcEnchant;
import net.forthecrown.text.writer.TextWriter;
import net.forthecrown.text.writer.TextWriters;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.inventory.ItemStacks;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

import static net.forthecrown.inventory.ExtendedItems.*;

public abstract class ExtendedItem {
    public static final String TAG_OWNER = "owner";

    @Getter
    private final ExtendedItemType type;

    @Getter
    private final UUID owner;

    public ExtendedItem(ExtendedItemType type, CompoundTag tag) {
        this.type = type;
        this.owner = tag.getUUID(TAG_OWNER);
    }

    public ExtendedItem(ExtendedItemType type, UUID owner) {
        this.type = type;
        this.owner = owner;
    }

    public void update(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        onUpdate(item, meta);

        var loreWriter = TextWriters.loreWriter();

        // Just in case there's any ftc enchants on the item
        // ensure they stay in the lore
        if (meta.hasEnchants()) {
            for (var e: meta.getEnchants().entrySet()) {
                var enchant = e.getKey();
                int level = e.getValue();

                if (enchant instanceof FtcEnchant ftcEnchant) {
                    loreWriter.line(ftcEnchant.displayName(level));
                }
            }
        }

        writeLore(loreWriter);

        //Save tags to item's NBT
        CompoundTag tag = new CompoundTag();

        if (owner != null) {
            tag.putUUID(TAG_OWNER, owner);
        }

        save(tag);

        CompoundTag topTag = new CompoundTag();
        topTag.putString(TAG_TYPE, type.getKey());
        topTag.put(TAG_DATA, tag);

        ItemStacks.setTagElement(meta, TAG_CONTAINER, topTag);

        meta.lore(loreWriter.getLore());
        item.setItemMeta(meta);
    }

    protected abstract void onUpdate(ItemStack item, ItemMeta meta);

    public abstract void save(CompoundTag tag);

    protected abstract void writeLore(TextWriter lore);

    public User getOwnerUser() {
        return !hasPlayerOwner() ? null : Users.get(getOwner());
    }

    public boolean hasPlayerOwner() {
        return owner != null && owner != Util.NIL_UUID;
    }
}