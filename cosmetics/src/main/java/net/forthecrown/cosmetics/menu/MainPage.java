package net.forthecrown.cosmetics.menu;

import static net.forthecrown.menu.Menus.MAX_INV_SIZE;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.nio.file.Path;
import net.forthecrown.cosmetics.CosmeticType;
import net.forthecrown.cosmetics.Cosmetics;
import net.forthecrown.menu.MenuBuilder;
import net.forthecrown.menu.MenuNode;
import net.forthecrown.menu.Menus;
import net.forthecrown.menu.page.MenuPage;
import net.forthecrown.registry.Holder;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
import net.forthecrown.utils.Result;
import net.forthecrown.utils.context.Context;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.PluginJar;
import net.forthecrown.utils.io.SerializationHelper;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

class MainPage extends MenuPage {

  public static final Logger LOGGER = CosmeticMenus.LOGGER;

  public MainPage() {
    initMenu(Menus.builder(MAX_INV_SIZE, "Cosmetics"), false);
  }

  @Override
  protected MenuNode createHeader() {
    return MenuNode.builder()
        .setItem((user, context) -> {
          return ItemStacks.builder(Material.NETHER_STAR)
              .setName("Menu")
              .addLore("")
              .addLore(Text.format("You have &6{0, gems}&r", NamedTextColor.GRAY, user.getGems()))
              .build();
        })
        .build();
  }

  @Override
  public @Nullable ItemStack createItem(@NotNull User user, @NotNull Context context) {
    return ItemStacks.builder(Material.PAPER)
        .setName("< Go back!")
        .addLore(Text.format("You have &6{0, gems}&r.", NamedTextColor.GRAY, user.getGems()))
        .build();
  }

  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  protected void createMenu(MenuBuilder builder) {
    var registry = Cosmetics.TYPES;
    var displayData = loadDisplayData();

    for (Holder<CosmeticType> entry : registry.entries()) {
      getDisplayData(displayData, entry.getKey())
          .mapError(s -> "Couldn't load display data for '" + entry.getKey() + "': " + s)
          .apply(LOGGER::error, typeDisplay -> {
            TypePage page = new TypePage(this, entry.getValue(), typeDisplay);
            builder.add(typeDisplay.slot, page);
          });
    }
  }

  private Result<TypeDisplay> getDisplayData(JsonWrapper json, String key) {
    JsonElement displayElement = json.get(key);

    if (displayElement == null || !displayElement.isJsonObject()) {
      return Result.error("Missing entry in 'effect-types.toml'");
    }

    return TypeDisplay.load(displayElement);
  }

  private JsonWrapper loadDisplayData() {
    PluginJar.saveResources("effect-types.toml");
    Path path = PathUtil.pluginPath("effect-types.toml");
    JsonObject obj = SerializationHelper.readTomlAsJson(path).getOrThrow(false, s -> {});
    return JsonWrapper.wrap(obj);
  }
}
