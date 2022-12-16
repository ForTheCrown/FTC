package net.forthecrown.economy.shops;

import lombok.Getter;
import lombok.Setter;
import net.forthecrown.core.FTC;
import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.economy.Economy;
import net.forthecrown.utils.LocationFileName;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.Time;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.io.TagUtil;
import net.forthecrown.utils.math.WorldVec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.Tag;
import org.apache.commons.lang3.Validate;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

import static net.forthecrown.economy.shops.SignShops.*;

public class SignShop implements InventoryHolder {
    /* ----------------------------- CONSTANTS ------------------------------ */

    /** NBT tag of the example item */
    private static final String
            TAG_EXAMPLE_ITEM = "exampleItem",

            /** NBT tag of the inventory size */
            TAG_INVENTORY_SIZE = "inventorySize",

            /** NBT tag of the item's price */
            TAG_PRICE = "price",

            /** NBT tag of the shop's type */
            TAG_TYPE = "type",

            /** NBT tag of the amount of items in the shop */
            TAG_ITEM_COUNT = "itemCount",

            /** NBT tag of the shop history */
            TAG_HISTORY = "history",

            /** NBT tag of the owner's UUID */
            TAG_OWNER = "owner";

    /* ----------------------------- INSTANCE FIELDS ------------------------------ */

    /**
     * The world and x, y, z position of this shop
     */
    @Getter private final WorldVec3i position;

    /**
     * This shop's transaction history
     */
    @Getter private final ShopHistory history;

    /**
     * The example item of the shop, which
     * determines what the shop sells and
     * how much of it to sell.
     */
    @Setter
    private ItemStack exampleItem;

    /**
     * The shop's inventory, not serialized
     * <p>
     * This inventory only exists when the shop is loaded
     * and this inventory must <b>ONLY</b> contain items
     * similar to the {@link #getExampleItem()}. This is
     * because the inventory is serialized in a way that
     * no items within the inventory are serialized, but
     * rather that a count of the total amount of items
     * in the inventory is, this leaves no room for
     * non-valid items.
     */
    @Getter
    private Inventory inventory;

    /**
     * The owner of this shop. If this shop is an admin shop, the owner
     * will be null, as admin shops do not have owners
     */
    @Getter @Setter
    private UUID owner;

    /**
     * The shop's type.
     * This determines not only how the shop looks,
     * but also how the shop is interacted with by players.
     * @see ShopInteraction
     */
    @Getter @Setter
    private ShopType type;

    /**
     * The price of the shop's items
     */
    @Getter
    private int price;

    /**
     * The task that unloads this sign shop when it
     * executes.
     * <p>
     * Created/pushed forward by
     */
    @Getter
    private BukkitTask unloadTask;

    /* ----------------------------- CONSTRUCTORS ------------------------------ */

    /**
     * Creates {@link SignShop} instance with the block data
     * at the given position
     * @param position The position of the shop
     * @throws IllegalArgumentException If the block does not have any sign shop data
     */
    public SignShop(WorldVec3i position) throws IllegalArgumentException {
        this(position, false);
    }

    /**
     * Creates a fresh shop with the given parameters.
     * @param position The position of the shop
     * @param shopType The shop's type
     * @param price The shop's inital price
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
        if(!newShop) {
            Validate.isTrue(
                    getSign().getPersistentDataContainer().has(SignShops.SHOP_KEY),
                    getName() + " has no shop data"
            );

            load();
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
     * @param removeBlock true, to set the shop's block to air, note:
     *                    this doesn't determine if the shop will drop
     *                    its items or not, that will happen regardless
     */
    public void destroy(boolean removeBlock) {
        Economy.get()
                .getShops()
                .removeShop(this);

        // if inventory not empty -> drop contents
        if (!inventory.isEmpty()) {
            ItemStacks.forEachNonEmptyStack(inventory, stack -> {
                position.getWorld().dropItemNaturally(position.toLocation(), stack);
            });
        }

        // Some lil clouds :D
        position.getWorld().spawnParticle(Particle.CLOUD, position.toLocation().add(0.5, 0.5, 0.5), 5, 0.1D, 0.1D, 0.1D, 0.05D);

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
     * than the current size, there is a risk of items being
     * lost due to the way they are transferred over.
     * <p>
     * Cut me some slack, I'm rewriting 500 classes in a manic
     * bid to remain relevant, and I've been awake for 27 hours
     * @param newSize THe new inventory size
     */
    public void resizeInventory(int newSize) {
        if (newSize == getInventory().getSize()) {
            return;
        }

        var old = getInventory();
        this.inventory = SignShops.createInventory(this, newSize);

        // Attempt to move all items from
        // last inventory to new one
        ItemStacks.forEachNonEmptyStack(old, i -> inventory.addItem(i));
    }

    /**
     * Gets this shop's example item.
     * @see #exampleItem
     * @return A clone of this shop's example item, or null, if there is no example item
     */
    public ItemStack getExampleItem() {
        return exampleItem == null ? null : exampleItem.clone();
    }

    /**
     * Tests whether this shop is in stock or not.
     * <p>
     * If, for some reason, the {@link #exampleItem} is null,
     * then this method will return false.
     * <p>
     * If this shop is a sell shop, then this will return false
     * if {@link #isFull()} returns true.
     * <p>
     * If this shop is an admin shop, this always returns true.
     * <p>
     * Lastly, it returns true if the shop's inventory contains
     * enough of the example item to complete a successful
     * transaction
     * @return
     */
    public boolean inStock() {
        if (getExampleItem() == null) {
            return false;
        }

        // Admin shops are never out of stock
        if (getType().isAdmin()) {
            return true;
        }

        // If it's full and a sell shop, then it's considered
        // out of stock due to the fact it cannot operate
        if (isFull() && !getType().isBuyType()) {
            return false;
        }

        // Check if inventory contains enough of the example item
        // to be considered 'in stock'
        return getInventory().containsAtLeast(getExampleItem(), getExampleItem().getAmount());
    }

    /**
     * Checks if the inventory is full.
     * Just checks if {@link Inventory#firstEmpty()} == -1
     * @return True, if the shop's inventory is full, false otherwise
     */
    public boolean isFull() {
        return getInventory().firstEmpty() == -1;
    }

    /**
     * The name of this sign shop, in it's original
     * file name format.
     * @return This shop's file name
     */
    public LocationFileName getName() {
        return LocationFileName.of(getPosition());
    }

    /**
     * Gets this sign shop's block
     * @return This shop's block
     */
    public Block getBlock() {
        return getPosition().getBlock();
    }

    /**
     * Sets the price of the shop and updates the
     * sign tile entity.
     * <p>
     * Delegate for {@link #setPrice(int, boolean)}
     * @param price The new price of the shop
     */
    public void setPrice(int price) {
        setPrice(price, true);
    }

    /**
     * Sets the price of the shop and optionally
     * updates the sign tile entity.
     * @param price The shop's new price
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
     * @return The shop's tile entity
     */
    public Sign getSign() {
        var state = getBlock().getState();

        if (!(state instanceof Sign sign)) {
            FTC.getLogger().warn("Shop at {} is not a sign block", getPosition());
            return null;
        }

        return sign;
    }

    /**
     * Resets/starts the shop's automatic unload timer
     * <p>
     * Once the timer executes it will call {@link #onUnload()}
     * to unload this shop.
     */
    public void delayUnload() {
        Tasks.cancel(unloadTask);

        unloadTask = Tasks.runLater(
                this::onUnload,
                Time.millisToTicks(GeneralConfig.shopUnloadDelay)
        );
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

        Economy.get()
                .getShops()
                .removeShop(this);
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
                inStock() ? getType().getStockedLabel() : getType().getUnStockedLabel()
        );
        s.line(
                LINE_PRICE,
                SignShops.priceLine(price)
        );

        // Save the shop into the sign's persistent data
        // container
        save(s);

        // Update the ingame tile entity by
        // calling bukkit's update method here.
        // what did I just say???
        s.update(true);
    }

    /* ----------------------------- SERIALIZATION ------------------------------ */

    /**
     * Saves this shop into the given sign
     * @param sign The sign entity snapshot to save into
     */
    private void save(Sign sign) {
        // Create tag and then call our own
        // save() methwod
        CompoundTag tag = new CompoundTag();
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
     * @param tag The tag to save into
     */
    public void save(CompoundTag tag) {
        // Save the generic stuff
        tag.putInt(TAG_PRICE, price);
        tag.put(TAG_TYPE, TagUtil.writeEnum(type));

        // If this shop has an owner, save them
        if (owner != null && !type.isAdmin()) {
            tag.putUUID(TAG_OWNER, getOwner());
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
     * @param tag The data to load from
     */
    public void load(CompoundTag tag) {
        price = tag.getInt(TAG_PRICE);
        type = TagUtil.readEnum(ShopType.class, tag.get(TAG_TYPE));

        // Read owner if there is one, if there isn't,
        // check there isn't a legacy tag to read
        if (tag.contains(TAG_OWNER)) {
            setOwner(tag.getUUID(TAG_OWNER));
        } else if (tag.contains("ownership")) {
            readLegacyOwner(tag.get("ownership"));
        }

        // Load legacy inventory
        if (tag.contains("inventory")) {
            var inventory = tag.getCompound("inventory");
            setExampleItem(TagUtil.readItem(inventory.get("exampleItem")));

            getInventory().clear();

            if (inventory.contains("items")) {
                var itemList = inventory.getList("items", Tag.TAG_COMPOUND);

                for (var t: itemList) {
                    getInventory().addItem(TagUtil.readItem(t));
                }
            }
        } else {
            setExampleItem(TagUtil.readItem(tag.get(TAG_EXAMPLE_ITEM)));
            this.inventory = SignShops.createInventory(this, tag.getInt(TAG_INVENTORY_SIZE));

            int itemCount = tag.getInt(TAG_ITEM_COUNT);
            int stackSize = exampleItem.getMaxStackSize();

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
     * Since I yeeted the `ShopOwnership` class, I need
     * to have this little method to make sure the old
     * shops get taken care of!
     *
     * @param tag The tag to read
     */
    private void readLegacyOwner(Tag tag) {
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