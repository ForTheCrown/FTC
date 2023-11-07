package net.forthecrown.cosmetics.menu;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.forthecrown.menu.Slot;
import net.forthecrown.utils.Result;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonUtils.EnumTypeAdapter;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

class TypeDisplay {

  static Gson GSON = createGson();

  Slot slot;
  Component[] description;
  Material material;

  boolean noEffectButton = false;

  static Result<TypeDisplay> load(JsonElement element) {
    var menu = GSON.fromJson(element, TypeDisplay.class);
    if (menu.slot == null) {
      return Result.error("No slot");
    }

    if (menu.description == null) {
      menu.description = new Component[0];
    }

    if (menu.material == null) {
      return Result.error("No material");
    }

    return Result.success(menu);
  }

  static Gson createGson() {
    GsonBuilder builder = new GsonBuilder();
    builder.registerTypeAdapter(Slot.class, JsonUtils.createAdapter(null, Slot::load));
    builder.registerTypeAdapterFactory(new EnumTypeAdapter());

    builder.registerTypeHierarchyAdapter(
        Component.class,
        JsonUtils.createAdapter(JsonUtils::writeText, JsonUtils::readText)
    );

    return builder.create();
  }
}
