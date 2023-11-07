package net.forthecrown.sellshop;

import com.google.gson.JsonElement;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.menu.MenuNodeItem;
import net.forthecrown.menu.Menus;
import net.forthecrown.menu.Slot;
import net.forthecrown.text.Text;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang3.Validate;
import org.bukkit.Material;

/**
 * Parses a {@link SellShopMenu} from JSON.
 * <p>
 * Parsing is one by a few select JSON keys from a file (see the constants in this class). The most
 * important of those keys being {@link #KEY_CONTENT_FILE} which stores path of the '.shop' file
 * which contains the menu's item price data. That item data is parsed by {@link PriceMapReader},
 * which uses a custom .shop format
 *
 * @see PriceMapReader
 * @see SellShop
 */
@RequiredArgsConstructor
public class MenuReader {
  /* ----------------------------- CONSTANTS ------------------------------ */

  private static final String KEY_TITLE = "title";
  private static final String KEY_CONTENT_FILE = "shop_contents_file";
  private static final String KEY_SLOT = "main_page_slot";
  private static final String KEY_HEADER = "header_item";
  private static final String KEY_HEADER_MAT = "material";
  private static final String KEY_HEADER_LORE = "lore";
  private static final String KEY_HEADER_NAME = "name";

  /**
   * The size of a sell shop menu, 6 rows or just 54
   */
  private static final int MENU_SIZE = Menus.sizeFromRows(6);

  /* ----------------------------- INSTANCE FIELDS ------------------------------ */

  /**
   * Directory path of the shops.json
   */
  private final Path directory;

  /**
   * The JSON object of the menu being read
   */
  private final JsonWrapper json;

  /**
   * The slot of the current menu in the main menu
   */
  @Getter
  private Slot slot;

  public SellShopMenu read(SellShop shop, int defaultMax) {
    ensureKeyPresent(json, KEY_TITLE);
    ensureKeyPresent(json, KEY_CONTENT_FILE);
    ensureKeyPresent(json, KEY_SLOT);
    ensureKeyPresent(json, KEY_HEADER);

    var priceMap = readItemPrices(defaultMax);
    var item = readItem();

    Component title = json.getComponent(KEY_TITLE);
    slot = readSlot();

    return new SellShopMenu(item, priceMap, MENU_SIZE, title, shop);
  }

  private Slot readSlot() {
    var slotElement = json.get(KEY_SLOT);
    return readSlot(slotElement);
  }

  public static Slot readSlot(JsonElement slotElement) {
    return Slot.load(slotElement);
  }

  private ItemPriceMap readItemPrices(int defaultMaxEarnings) {
    // Get path
    var pathString = json.getString(KEY_CONTENT_FILE);
    var path = directory.resolve(pathString);

    // Ensure file at given path exists
    Validate.isTrue(Files.exists(path), "Given price file: '%s' does not exist", path);

    return PriceMapReader.readFile(path, defaultMaxEarnings);
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
    var name = Text.wrapForItems(itemJson.getComponent(KEY_HEADER_NAME))
        .color(NamedTextColor.AQUA);

    List<Component> lore = new ArrayList<>();

    // If lore is given
    if (itemJson.has(KEY_HEADER_LORE)) {
      var arr = itemJson.getArray(KEY_HEADER_LORE);

      for (var e : arr) {
        lore.add(
            Text.wrapForItems(JsonUtils.readText(e))
                .color(NamedTextColor.GRAY)
        );
      }
    }

    var builder = ItemStacks.builder(material)
        .setNameRaw(name)
        .setLore(lore);

    return MenuNodeItem.of(builder.build());
  }

  private static void ensureKeyPresent(JsonWrapper json, String key) {
    Validate.isTrue(json.has(key),
        "Invalid JSON! Missing key: '%s'", key
    );
  }
}