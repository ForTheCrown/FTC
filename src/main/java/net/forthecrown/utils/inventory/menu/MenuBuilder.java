package net.forthecrown.utils.inventory.menu;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.text.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

import static net.forthecrown.utils.inventory.menu.Menus.DEFAULT_INV_SIZE;

/**
 * A builder of {@link Menu}s.
 * @see MenuNode
 * @see MenuNodeItem
 * @see Menu
 */
@Getter
@Accessors(chain = true)
public class MenuBuilder {
    /* ----------------------------- INSTANCE FIELDS ------------------------------ */

    /** The title of the inventory */
    Component title;

    /** The inventory's size, initially set to {@link Menus#DEFAULT_INV_SIZE} */
    int size = DEFAULT_INV_SIZE;

    /**
     * Determines if inside the given inventory can be moved around
     * and if items can be moved in and out of the menu
     */
    @Setter
    boolean itemMovingAllowed;

    /** Slot 2 menu node map for all current nodes in the builder */
    final Int2ObjectMap<MenuNode> nodes = new Int2ObjectOpenHashMap<>();

    /** Callback for when the inventory is opened */
    @Setter
    MenuOpenConsumer openCallback;

    /** Callback for when the inventory is closed */
    @Setter
    MenuCloseConsumer closeCallback;

    /* ----------------------------- METHODS ------------------------------ */

    /**
     * Adds the given node to the menu
     * @param index The inventory slot of the node
     * @param node The node
     * @return This
     */
    public MenuBuilder add(int index, MenuNode node) {
        nodes.put(index, node);
        return this;
    }

    /**
     * Adds the given node to the given
     * slot
     * @param slot The slot to insert the node at
     * @param node The node to insert
     * @return This
     */
    public MenuBuilder add(Slot slot, MenuNode node) {
        return add(slot.getIndex(), node);
    }

    public MenuBuilder add(int column, int row, MenuNode node) {
        return add(Slot.toIndex(column, row), node);
    }

    public MenuBuilder add(int slot, ItemStack itemStack) {
        return add(slot,
                MenuNode.builder()
                        .setItem(itemStack)
                        .build()
        );
    }

    public MenuBuilder add(Slot slot, ItemStack itemStack) {
        return add(slot.getIndex(), itemStack);
    }

    public MenuBuilder add(int column, int row, ItemStack itemStack) {
        return add(Slot.toIndex(column, row), itemStack);
    }

    public MenuBuilder addBorder() {
        return addBorder(Menus.defaultBorderItem());
    }

    public MenuBuilder addBorder(ItemStack item) {
        var option = MenuNode.builder()
                .setItem(item)
                .build();

        Menus.placeBorder(this, option);
        return this;
    }

    public MenuBuilder setTitle(Component title) {
        this.title = title;
        return this;
    }

    public MenuBuilder setTitle(String title) {
        return setTitle(Text.renderString(title));
    }

    public MenuBuilder setSize(int size) throws IllegalArgumentException {
        this.size = Menus.validateSize(size);
        return this;
    }

    public Menu build() {
        return new Menu(this);
    }
}