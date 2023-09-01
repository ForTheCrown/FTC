package net.forthecrown.inventory;

import static net.forthecrown.inventory.ExtendedItems.TAG_CONTAINER;
import static net.forthecrown.inventory.ExtendedItems.TAG_DATA;
import static net.forthecrown.inventory.ExtendedItems.TAG_TYPE;

import com.google.common.base.Preconditions;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.enchantment.FtcEnchant;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.text.TextWriter;
import net.forthecrown.text.TextWriters;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class ExtendedItem {

  public static final String TAG_OWNER = "owner";

  @Getter
  private final ExtendedItemType<?> type;

  @Getter
  private final UUID owner;

  /**
   * Instances of this class act as a snapshot of the item, once update is
   * called, you are invalidating this snapshot instance of the item and are
   * required to create a new instance to edit further
   */
  @Getter
  private boolean finished = false;

  @Getter @Setter
  protected boolean displayItem;

  public ExtendedItem(ExtendedItemType<?> type, CompoundTag tag) {
    this.type = type;
    this.owner = tag.getUUID(TAG_OWNER);
    this.displayItem = tag.getBoolean("display_item");
  }

  public ExtendedItem(ExtendedItemType<?> type, UUID owner) {
    this.type = type;
    this.owner = owner;
  }

  public void update(ItemStack item) {
    Preconditions.checkState(
        !finished,
        "Item already updated, create a new instance of "
            + "this class to edit further"
    );

    ItemMeta meta = item.getItemMeta();
    onUpdate(item, meta);

    var loreWriter = TextWriters.buffered();

    // Just in case there's any ftc enchants on the item
    // ensure they stay in the lore
    if (meta.hasEnchants() && Bukkit.getPluginManager().isPluginEnabled("FTC-Dungeons")) {
      for (var e : meta.getEnchants().entrySet()) {
        var enchant = e.getKey();
        int level = e.getValue();

        if (enchant instanceof FtcEnchant ftcEnchant) {
          loreWriter.line(
              ftcEnchant.displayName(level)
                  .colorIfAbsent(NamedTextColor.GRAY)
          );
        }
      }
    }

    writeLore(loreWriter);

    //Save tags to item's NBT
    CompoundTag tag = BinaryTags.compoundTag();

    if (owner != null) {
      tag.putUUID(TAG_OWNER, owner);
    }

    if (displayItem) {
      tag.putBoolean("display_item", true);
    }

    save(tag);

    CompoundTag topTag = BinaryTags.compoundTag();
    topTag.putString(TAG_TYPE, type.getKey());
    topTag.put(TAG_DATA, tag);

    ItemStacks.setTagElement(meta, TAG_CONTAINER, topTag);

    meta.lore(loreWriter.getBuffer());
    item.setItemMeta(meta);
    finished = true;
  }

  protected abstract void onUpdate(ItemStack item, ItemMeta meta);

  public abstract void save(CompoundTag tag);

  protected abstract void writeLore(TextWriter lore);

  public User getOwnerUser() {
    return !hasPlayerOwner() ? null : Users.get(getOwner());
  }

  public boolean hasPlayerOwner() {
    return owner != null && !owner.equals(Identity.nil().uuid());
  }
}