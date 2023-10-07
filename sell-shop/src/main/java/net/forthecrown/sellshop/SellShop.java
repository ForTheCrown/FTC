package net.forthecrown.sellshop;

import java.nio.file.Path;
import lombok.Getter;
import net.forthecrown.menu.Menu;
import net.forthecrown.menu.Menus;
import net.forthecrown.menu.Slot;
import net.forthecrown.registry.Registries;
import net.forthecrown.registry.Registry;
import net.forthecrown.sellshop.event.SellShopCreateEvent;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.PluginJar;
import net.forthecrown.utils.io.SerializationHelper;

public class SellShop {

  /**
   * Registry of sell shop menus
   */
  @Getter
  private final Registry<SellShopMenu> menus = Registries.newRegistry();

  /**
   * Directory the sellshops are in, this directory must include the <code>shops.json</code> file
   * from which the shop data is read
   */
  private final Path directory;

  /**
   * Main sellshop menu
   */
  @Getter
  private Menu mainMenu;

  /**
   * Global item price map
   */
  @Getter
  private final ItemPriceMap priceMap = new ItemPriceMap();

  private final SellShopPlugin plugin;

  public SellShop(SellShopPlugin plugin, Path directory) {
    this.plugin = plugin;
    this.directory = directory;

    SellProperties.registerAll();
  }

  public void load() {
    createDefaults();
    SerializationHelper.readJsonFile(getPath(), this::load);
  }

  private void load(JsonWrapper json) {
    this.menus.clear();
    this.priceMap.clear();

    var builder = Menus.builder(Menus.sizeFromRows(4), "FTC Shop")
        .addBorder()
        .add(4, 1, SellShopNodes.WEBSTORE);

    for (var e : json.entrySet()) {
      var name = e.getKey();
      var element = e.getValue();
      var menuJson = JsonWrapper.wrap(element.getAsJsonObject());

      var reader = new MenuReader(directory, menuJson);
      var menu = reader.read(this, plugin.getShopConfig().defaultMaxEarnings());

      priceMap.addAll(menu.getPriceMap());
      builder.add(reader.getSlot(), Menus.createOpenNode(menu.getInventory(), menu.getButton()));

      menus.register(name, menu);
    }

    builder.add(Slot.of(4, 3), SellShopNodes.INFO);

    var event = new SellShopCreateEvent(builder, this);
    event.callEvent();

    this.mainMenu = builder.build();
  }

  public Path getPath() {
    return directory.resolve("shops.json");
  }

  public void createDefaults() {
    PluginJar.saveResources("shops", directory);
  }
}