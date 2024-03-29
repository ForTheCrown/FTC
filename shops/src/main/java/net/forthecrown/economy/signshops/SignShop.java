package net.forthecrown.economy.signshops;

import static net.forthecrown.economy.signshops.SignShops.DEFAULT_INV_SIZE;
import static net.forthecrown.economy.signshops.SignShops.LINE_PRICE;
import static net.forthecrown.economy.signshops.SignShops.LINE_TYPE;
import static net.forthecrown.menu.Menus.MAX_INV_SIZE;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.Loggers;
import net.forthecrown.menu.Menus;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.nbt.IntArrayTag;
import net.forthecrown.nbt.TagTypes;
import net.forthecrown.utils.LocationFileName;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.inventory.ItemList;
import net.forthecrown.utils.inventory.ItemLists;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.io.TagUtil;
import net.forthecrown.utils.math.WorldVec3i;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.apache.commons.lang3.Validate;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.HangingSign;
import org.bukkit.block.Sign;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.math.GenericMath;

public class SignShop implements InventoryHolder {

  private static final Logger LOGGER = Loggers.getLogger();

  /* ----------------------------- CONSTANTS ------------------------------ */

  private static final String TAG_EXAMPLE_ITEM = "exampleItem";
  private static final String TAG_INVENTORY_SIZE = "inventorySize";
  private static final String TAG_PRICE = "price";
  private static final String TAG_TYPE = "type";
  private static final String TAG_ITEM_COUNT = "itemCount";
  private static final String TAG_LAST_USE = "lastInteraction";
  private static final String TAG_OWNER = "owner";
  private static final String TAG_HISTORY = "history";
  private static final String TAG_RESELL_DISABLE = "resellDisabled";

  /* ----------------------------- INSTANCE FIELDS ------------------------------ */

  /**
   * The world and x, y, z position of this shop
   */
  @Getter
  private final WorldVec3i position;

  /**
   * The example item of the shop, which determines what the shop sells and how much of it to sell.
   */
  @Setter
  private ItemStack exampleItem;

  /**
   * The shop's inventory, not serialized
   * <p>
   * This inventory only exists when the shop is loaded and this inventory must <b>ONLY</b> contain
   * items similar to the {@link #getExampleItem()}. This is because the inventory is serialized in
   * a way that no items within the inventory are serialized, but rather that a count of the total
   * amount of items in the inventory is, this leaves no room for non-valid items.
   */
  @Getter
  private Inventory inventory;

  /**
   * This shop's transaction history
   */
  @Getter
  private final ShopHistory history;

  /**
   * The owner of this shop. If this shop is an admin shop, the owner will be null, as admin shops
   * do not have owners
   */
  @Getter
  @Setter
  private UUID owner;

  /**
   * The shop's type. This determines not only how the shop looks, but also how the shop is
   * interacted with by players.
   *
   * @see ShopInteraction
   */
  @Getter
  @Setter
  private ShopType type;

  /**
   * The price of the shop's items
   */
  @Getter
  private int price;

  @Getter @Setter
  private boolean resellDisabled = false;

  /**
   * The task that unloads this sign shop when it executes.
   * <p>
   * Created/pushed forward by
   */
  @Getter
  private BukkitTask unloadTask;

  ShopManager manager;

  @Getter @Setter
  private long lastInteraction = -1;

  /* ----------------------------- CONSTRUCTORS ------------------------------ */

  /**
   * Creates {@link SignShop} instance with the block data at the given position
   *
   * @param position The position of the shop
   * @throws IllegalArgumentException If the block does not have any sign shop data
   */
  public SignShop(WorldVec3i position) throws IllegalArgumentException {
    this(position, false);
  }

  /**
   * Creates a fresh shop with the given parameters.
   *
   * @param position  The position of the shop
   * @param shopType  The shop's type
   * @param price     The shop's inital price
   * @param shopOwner The owner of the shop
   */
  public SignShop(WorldVec3i position, ShopType shopType, int price, @Nullable UUID shopOwner) {
    this(position, true);

    this.price = price;
    this.type = shopType;

    // Admin shops don't have owners
    if (!type.isAdmin()) {
      this.owner = shopOwner;
    }
  }

  public SignShop(WorldVec3i pos, boolean newShop) {
    this.position = pos;

    this.history = new ShopHistory(this);
    this.inventory = SignShops.createInventory(this, DEFAULT_INV_SIZE);

    // If this is not a new shop, we must ensure that the block we've been
    // given is actually a sign shop
    if (!newShop) {
      Validate.isTrue(
          getSign().getPersistentDataContainer().has(SignShops.SHOP_KEY),
          getName() + " has no shop data"
      );
    }
  }

  /* ----------------------------- METHODS ------------------------------ */

  /**
   * Loads shop data from this sign shop's {@link Sign} block
   */
  public void load() {
    load(getSign());
  }

  /**
   * Destroys this sign shop
   *
   * @param removeBlock true, to set the shop's block to air, note: this doesn't determine if the
   *                    shop will drop its items or not, that will happen regardless
   */
  public void destroy(boolean removeBlock) {
    if (manager != null) {
      manager.removeShop(this);
    }

    // if inventory not empty -> drop contents
    if (!inventory.isEmpty()) {
      ItemStacks.forEachNonEmptyStack(inventory, stack -> {
        position.getWorld().dropItemNaturally(position.toLocation(), stack);
      });
    }

    // Some lil clouds :D
    position.getWorld()
        .spawnParticle(Particle.CLOUD, position.toLocation().add(0.5, 0.5, 0.5), 5, 0.1D, 0.1D,
            0.1D, 0.05D);

    // Break the block if we have to
    if (removeBlock) {
      getBlock().breakNaturally();
    }

    Tasks.cancel(unloadTask);
  }

  /**
   * Resizes the inventory to a new size.
   * <p>
   * <b>WARNING</b> If the stated inventory size is smaller
   * than the current size, there is a risk of items being lost due to the way they are transferred
   * over.
   * <p>
   * Cut me some slack, I'm rewriting 500 classes in a manic bid to remain relevant, and I've been
   * awake for 27 hours
   *
   * @param newSize THe new inventory size
   */
  public void resizeInventory(int newSize) {
    newSize = ensureCorrectInventorySize(newSize);

    if (newSize == getInventory().getSize()) {
      return;
    }

    var old = getInventory();
    this.inventory = SignShops.createInventory(this, newSize);

    // Attempt to move all items from
    // last inventory to new one
    ItemStacks.forEachNonEmptyStack(old, i -> inventory.addItem(i));
  }

  private static int ensureCorrectInventorySize(int size) {
    if (Menus.isValidSize(size)) {
      return size;
    }

    LOGGER.error(
        "Invalid shop inventory size: {}, must be in bounds [9..54] "
            + "and a multiple of 9",
        size
    );

    int rows = (size / 9) + 1;
    return Math.min(MAX_INV_SIZE, Menus.sizeFromRows(rows));
  }

  /**
   * Gets this shop's example item.
   *
   * @return A clone of this shop's example item, or null, if there is no example item
   * @see #exampleItem
   */
  public ItemStack getExampleItem() {
    return ItemStacks.isEmpty(exampleItem)
        ? null
        : exampleItem.clone();
  }

  /**
   * Tests whether this shop is in stock or not.
   * <p>
   * If, for some reason, the {@link #exampleItem} is null, then this method will return false.
   * <p>
   * If this shop is a sell shop, then this will return false if {@link #isFull()} returns true.
   * <p>
   * If this shop is an admin shop, this always returns true.
   * <p>
   * Lastly, it returns true if the shop's inventory contains enough of the example item to complete
   * a successful transaction
   *
   * @return True, if the shop is in stock, false otherwise
   */
  public boolean inStock() {
    if (ItemStacks.isEmpty(exampleItem)) {
      return false;
    }

    // Admin shops are never out of stock
    if (getType().isAdmin()) {
      return true;
    }

    // If it's full and a sell shop, then it's considered
    // out of stock due to the fact it cannot operate
    if (!getType().isBuyType()) {
      return !isFull();
    } else {
      // Check if inventory contains enough of the example item
      // to be considered 'in stock'
      return getInventory().containsAtLeast(
          getExampleItem(),
          getExampleItem().getAmount()
      );
    }
  }

  public int itemCount() {
    ItemList list = ItemLists.fromInventory(inventory, exampleItem::isSimilar);
    return list.totalItemCount();
  }

  /**
   * Checks if the inventory is full. Just checks if {@link Inventory#firstEmpty()} == -1
   *
   * @return True, if the shop's inventory is full, false otherwise
   */
  public boolean isFull() {
    return getInventory().firstEmpty() == -1;
  }

  /**
   * The name of this sign shop, in it's original file name format.
   *
   * @return This shop's file name
   */
  public LocationFileName getName() {
    return LocationFileName.of(getPosition());
  }

  /**
   * Gets this sign shop's block
   *
   * @return This shop's block
   */
  public Block getBlock() {
    return getPosition().getBlock();
  }

  /**
   * Sets the price of the shop and updates the sign tile entity.
   * <p>
   * Delegate for {@link #setPrice(int, boolean)}
   *
   * @param price The new price of the shop
   */
  public void setPrice(int price) {
    setPrice(price, true);
  }

  /**
   * Sets the price of the shop and optionally updates the sign tile entity.
   *
   * @param price      The shop's new price
   * @param updateSign True, to update the sign tile entity, false otherwise
   */
  public void setPrice(int price, boolean updateSign) {
    this.price = price;

    if (updateSign) {
      update();
    }
  }

  /**
   * Gets the sign tile entity of this sign shop
   *
   * @return The shop's tile entity
   */
  public Sign getSign() {
    var state = getBlock().getState();

    if (!(state instanceof Sign sign)) {
      Loggers.getLogger().warn("Shop at {} is not a sign block", getPosition());
      return null;
    }

    return sign;
  }

  /**
   * Resets/starts the shop's automatic unload timer
   * <p>
   * Once the timer executes it will call {@link #onUnload()} to unload this shop.
   */
  public void delayUnload() {
    cancelUnload();

    if (manager == null) {
      return;
    }

    var delay = manager.getPlugin().getShopConfig().getUnloadDelay();
    unloadTask = Tasks.runLater(this::onUnload, delay);
  }

  public void cancelUnload() {
    unloadTask = Tasks.cancel(unloadTask);
  }

  /**
   * Unloads this shop
   */
  private void onUnload() {
    // Close inventory for all viewers
    // just in case
    inventory.close();

    // Save block data and update block
    update();
    if (manager != null) {
      manager.removeShop(this);
    }
  }

  /**
   * Updates the tile entity of this shop
   */
  public void update() {
    // Get the sign
    Sign s = getSign();

    if (s == null) {
      return;
    }

    // Set the first and last lines,
    // 1st: label, 4th: the shop price
    s.line(
        LINE_TYPE,
        inStock()
            ? getType().getStockedLabel()
            : getType().getUnStockedLabel()
    );

    TextColor priceColor = derivePriceColor(s);

    if (s instanceof HangingSign) {
      s.line(
          LINE_PRICE,
          Component.text(getPrice() + "$", priceColor)
      );
    } else {
      s.line(
          LINE_PRICE,
          SignShops.priceLine(price, priceColor)
      );
    }

    // Save the shop into the sign's persistent data
    // container
    save(s);

    s.update();
  }

  private static TextColor derivePriceColor(Sign sign) {
    return NamedTextColor.WHITE;
  }

  /* ----------------------------- SERIALIZATION ------------------------------ */

  /**
   * Saves this shop into the given sign
   *
   * @param sign The sign entity snapshot to save into
   */
  private void save(Sign sign) {
    // Create tag and then call our own
    // save() methwod
    CompoundTag tag = BinaryTags.compoundTag();
    save(tag);

    // Then take that same tag and turn it
    // into bukkit's wrapper object and save
    // that in the data container
    sign.getPersistentDataContainer().set(
        SignShops.SHOP_KEY,
        PersistentDataType.TAG_CONTAINER,
        TagUtil.ofCompound(tag)
    );
  }

  /**
   * Loads this shop from the given sign
   *
   * @param sign The sign to load from
   */
  private void load(Sign sign) {
    // Get the data container from the sign's data container
    PersistentDataContainer container = sign.getPersistentDataContainer().getOrDefault(
        SignShops.SHOP_KEY,
        PersistentDataType.TAG_CONTAINER,
        TagUtil.newContainer()
    );

    // Turn the gotten container into a vanilla Tag
    // and then load shop data from that
    CompoundTag tag = TagUtil.ofContainer(container);
    load(tag);
  }

  /**
   * Saves the shop's data into the given tag
   *
   * @param tag The tag to save into
   */
  public void save(CompoundTag tag) {
    // Save the generic stuff
    tag.putInt(TAG_PRICE, price);
    tag.put(TAG_TYPE, TagUtil.writeEnum(type));
    tag.putBoolean(TAG_RESELL_DISABLE, resellDisabled);

    // If this shop has an owner, save them
    if (owner != null && !type.isAdmin()) {
      tag.putUUID(TAG_OWNER, getOwner());
    }

    if (lastInteraction != -1) {
      tag.putLong(TAG_LAST_USE, lastInteraction);
    }

    tag.put(TAG_EXAMPLE_ITEM, TagUtil.writeItem(exampleItem));
    tag.putInt(TAG_INVENTORY_SIZE, inventory.getSize());
    tag.putInt(TAG_ITEM_COUNT, countItems());

    if (!history.isEmpty()) {
      tag.put(TAG_HISTORY, history.save());
    }
  }

  private int countItems() {
    int result = 0;

    var it = ItemStacks.nonEmptyIterator(getInventory());
    while (it.hasNext()) {
      result += it.next().getAmount();
    }

    return result;
  }

  /**
   * Loads the shop's data from the given tag
   *
   * @param tag The data to load from
   */
  public void load(CompoundTag tag) {
    var config = manager.getPlugin().getShopConfig();
    price = tag.getInt(TAG_PRICE);
    price = GenericMath.clamp(price, 0, config.getMaxPrice());

    type = TagUtil.readEnum(ShopType.class, tag.get(TAG_TYPE));

    resellDisabled = tag.getBoolean(TAG_RESELL_DISABLE);

    // Read owner if there is one, if there isn't,
    // check there isn't a legacy tag to read
    if (tag.contains(TAG_OWNER)) {
      setOwner(tag.getUUID(TAG_OWNER));
    } else if (tag.contains("ownership")) {
      readLegacyOwner(tag.get("ownership"));
    }

    history.load(tag.get(TAG_HISTORY));

    if (tag.contains(TAG_LAST_USE, TagTypes.longType())) {
      lastInteraction = tag.getLong(TAG_LAST_USE);
    } else {
      lastInteraction = -1;
    }

    // Load legacy inventory
    if (tag.contains("inventory")) {
      var inventory = tag.getCompound("inventory");
      setExampleItem(TagUtil.readItem(inventory.get("exampleItem")));

      getInventory().clear();

      if (inventory.contains("items")) {
        var itemList = inventory.getList("items", TagTypes.compoundType());

        for (var t : itemList) {
          getInventory().addItem(TagUtil.readItem(t));
        }
      }
    } else {
      setExampleItem(TagUtil.readItem(tag.get(TAG_EXAMPLE_ITEM)));
      getInventory().clear();
      resizeInventory(tag.getInt(TAG_INVENTORY_SIZE));

      if (ItemStacks.isEmpty(exampleItem)) {
        LOGGER.warn("Shop {} loaded empty example item", getPosition());
      }

      int itemCount = tag.getInt(TAG_ITEM_COUNT);
      int stackSize = exampleItem.getMaxStackSize();

      int maxInvCapacity = stackSize * inventory.getSize();

      if (itemCount > maxInvCapacity) {
        LOGGER.warn(
            "itemCount above max inventory capacity! "
                + "itemCount={}, inventorySize={} invCapacity={}",
            itemCount,
            inventory.getSize(),
            maxInvCapacity
        );

        itemCount = maxInvCapacity;
      }

      for (int i = 0; i < inventory.getSize(); i++) {
        if (itemCount <= 0) {
          break;
        }

        var quantity = Math.min(itemCount, stackSize);
        var item = exampleItem.asQuantity(quantity);
        itemCount -= quantity;

        inventory.setItem(i, item);
      }
    }
  }

  /**
   * Since I yeeted the `ShopOwnership` class, I need to have this little method to make sure the
   * old shops get taken care of!
   *
   * @param tag The tag to read
   */
  private void readLegacyOwner(BinaryTag tag) {
    // Owner was only 1 person
    if (tag instanceof IntArrayTag) {
      setOwner(TagUtil.readUUID(tag));
    }

    // There may have been multiple owners
    if (tag instanceof CompoundTag compoundTag) {
      setOwner(compoundTag.getUUID("owner"));
    }
  }

  /* ----------------------------- OBJECT OVERRIDES ------------------------------ */

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    SignShop that = (SignShop) o;
    return getPosition().equals(that.getPosition());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getPosition());
  }
}