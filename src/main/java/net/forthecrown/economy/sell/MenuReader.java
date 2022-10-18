package net.forthecrown.economy.sell;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.text.Text;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.MenuNodeItem;
import net.forthecrown.utils.inventory.menu.Menus;
import net.forthecrown.utils.inventory.menu.Slot;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.Validate;
import org.bukkit.Material;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses a {@link SellShopMenu} from JSON.
 * <p>
 * Parsing is one by a few select JSON keys from a file
 * (see the constants in this class). The most important
 * of those keys being {@link #KEY_CONTENT_FILE} which
 * stores path of the '.shop' file which contains the
 * menu's item price data. That item data is parsed by
 * {@link PriceMapReader}, which uses a custom .shop
 * format
 * @see PriceMapReader
 * @see SellShop
 */
@RequiredArgsConstructor
public class MenuReader {
    /* ----------------------------- CONSTANTS ------------------------------ */

    private static final String
        KEY_TITLE = "title",

        /** The JSON key of the .shop file path for the shop's items */
        KEY_CONTENT_FILE = "shop_contents_file",

        /** The JSON key of the slot the menu should have in the main menu */
        KEY_SLOT = "main_page_slot",

        /** The JSON key of the header item */
        KEY_HEADER = "header_item",
        KEY_HEADER_MAT = "material",
        KEY_HEADER_LORE = "lore",
        KEY_HEADER_NAME = "name";

    /** The size of a sell shop menu, 6 rows or just 54 */
    private static final int MENU_SIZE = Menus.sizeFromRows(6);

    /* ----------------------------- INSTANCE FIELDS ------------------------------ */

    /** Directory path of the shops.json */
    private final Path directory;

    /** The JSON object of the menu being read */
    private final JsonWrapper json;

    /** The slot of the current menu in the main menu */
    @Getter
    private Slot slot;

    public SellShopMenu read(SellShop shop) {
        ensureKeyPresent(json, KEY_TITLE);
        ensureKeyPresent(json, KEY_CONTENT_FILE);
        ensureKeyPresent(json, KEY_SLOT);
        ensureKeyPresent(json, KEY_HEADER);

        var priceMap = readItemPrices();
        var item = readItem();

        Component title = json.getComponent(KEY_TITLE);
        slot = readSlot();

        return new SellShopMenu(item, priceMap, MENU_SIZE, title, shop);
    }

    private Slot readSlot() {
        var slotElement = json.get(KEY_SLOT);

        // If we were given a raw integer as an index
        if (slotElement.isJsonPrimitive()) {
            return Slot.of(slotElement.getAsInt());
        }

        // Read slot as a column, row inventory position
        JsonObject json = (JsonObject) slotElement;

        return Slot.of(
                json.get("column").getAsInt(),
                json.get("row").getAsInt()
        );
    }

    private ItemPriceMap readItemPrices() {
        // Get path
        var pathString = json.getString(KEY_CONTENT_FILE);
        var path = directory.resolve(pathString);

        // Ensure file at given path exists
        Validate.isTrue(Files.exists(path), "Given price file: '%s' does not exist", path);

        return PriceMapReader.readFile(path);
    }

    private MenuNodeItem readItem() {
        var itemJson = json.getWrapped(KEY_HEADER);

        // Ensure the header has both the name
        // and material keys
        ensureKeyPresent(itemJson, KEY_HEADER_MAT);
        ensureKeyPresent(itemJson, KEY_HEADER_NAME);

        var material = itemJson.getEnum(KEY_HEADER_MAT, Material.class);

        // Using getComponent, and by extension JsonUtil#readChat
        // for deserialization works because the GSON deserializer
        // accepts strings as valid input for text deserialization
        // That being said we do have to wrap them, so they don't
        // become italic and pink by default lol
        var name = Text.wrapForItems(itemJson.getComponent(KEY_HEADER_NAME));

        List<Component> lore = new ArrayList<>();

        // If lore is given
        if (itemJson.has(KEY_HEADER_LORE)) {
            var arr = itemJson.getArray(KEY_HEADER_LORE);

            for (var e: arr) {
                lore.add(Text.wrapForItems(JsonUtils.readText(e)));
            }
        }

        var builder = ItemStacks.builder(material)
                .setName(name)
                .setLore(lore);

        return MenuNodeItem.of(builder.build());
    }

    private static void ensureKeyPresent(JsonWrapper json, String key) {
        Validate.isTrue(json.has(key),
                "Invalid JSON! Missing key: '%s'", key
        );
    }
}