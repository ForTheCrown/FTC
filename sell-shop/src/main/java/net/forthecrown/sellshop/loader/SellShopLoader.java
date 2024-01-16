package net.forthecrown.sellshop.loader;

import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import net.forthecrown.menu.MenuNode;
import net.forthecrown.sellshop.ItemPriceMap;
import net.forthecrown.sellshop.SellShopNodes;
import net.forthecrown.sellshop.SellShopPlugin;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.Results;
import net.forthecrown.utils.io.SerializationHelper;

public class SellShopLoader {

  private final Map<String, ItemPriceMap> priceMaps = new HashMap<>();
  private final Map<String, LoadingPage> pages = new HashMap<>();
  private final Map<String, MenuNode> hardcoded;

  private final Path pluginDir;
  private final SellShopPlugin plugin;

  private final Path pricesDir;
  private final Path menusDir;

  public SellShopLoader(SellShopPlugin plugin) {
    this.plugin = plugin;
    this.pluginDir = plugin.getDataFolder().toPath();

    this.pricesDir = pluginDir.resolve("prices");
    this.menusDir = pluginDir.resolve("menus");

    hardcoded = new HashMap<>();
    hardcoded.put("filter_named",    SellShopNodes.SELLING_NAMED);
    hardcoded.put("filter_lore",     SellShopNodes.SELLING_LORE);
    hardcoded.put("info",            SellShopNodes.INFO);
    hardcoded.put("webstore",        SellShopNodes.WEBSTORE);
    hardcoded.put("toggle_compact",  SellShopNodes.COMPACT_TOGGLE);
    hardcoded.put("sell_amount_1",   SellShopNodes.SELL_PER_1);
    hardcoded.put("sell_amount_16",  SellShopNodes.SELL_PER_16);
    hardcoded.put("sell_amount_64",  SellShopNodes.SELL_PER_64);
    hardcoded.put("sell_amount_all", SellShopNodes.SELL_PER_ALL);
  }

  DataResult<LoadingPage> getPage(String fileName) {
    LoadingPage page = pages.get(fileName);
    if (page != null) {
      return Results.success(page);
    }

    DataResult<LoadingPage> result = loadPage(fileName);
    result.result().ifPresent(sellShopPage -> pages.put(fileName, sellShopPage));

    return result;
  }

  DataResult<LoadingPage> loadPage(String fileName) {
    Path path = getPath(menusDir, fileName);

    if (!Files.exists(path)) {
      return Results.error("File doesn't exist");
    }

    return SerializationHelper.readJson(path)
        .flatMap(object -> {
          DataResult<LoadingPage> loadingPage
              = SellShopCodecs.PAGE_CODEC.parse(JsonOps.INSTANCE, object);

          return loadingPage.flatMap(page -> loadFromLoadingPage(page, object));
        });
  }

  private DataResult<LoadingPage> loadFromLoadingPage(LoadingPage page, JsonObject object) {
    if (object.has("items")) {

    } else if (object.has("sellable-items")) {

    } else {
      return Results.success(page);
    }
  }

  private DataResult<ItemPriceMap> getPriceMap(String fileName) {
    ItemPriceMap found = priceMaps.get(fileName);
    if (found != null) {
      return Results.success(found);
    }

    DataResult<ItemPriceMap> result = loadPriceMap(fileName);
    result.result().ifPresent(itemSellData -> {
      priceMaps.put(fileName, itemSellData);
    });

    return result;
  }

  private DataResult<ItemPriceMap> loadPriceMap(String fileName) {
    Path path = getPath(pricesDir, fileName);

    if (!Files.exists(path)) {
      return Results.error("File doesn't exist");
    }

    return SerializationHelper.readFileObject(path, JsonUtils::readFile)
        .flatMap(element -> {
          if (!element.isJsonArray()) {
            return Results.error("Not a JSON array");
          }
          return SellShopCodecs.PRICE_MAP.parse(JsonOps.INSTANCE, element);
        });
  }

  private static Path getPath(Path dir, String fileName) {

    String filteredFileName;

    if (fileName.endsWith(".json")) {
      filteredFileName = fileName;
    } else {
      filteredFileName = fileName + ".json";
    }

    return dir.resolve(filteredFileName);
  }
}
