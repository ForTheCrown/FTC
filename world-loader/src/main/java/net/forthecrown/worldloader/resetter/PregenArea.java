package net.forthecrown.worldloader.resetter;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.Results;
import net.forthecrown.worldloader.LoadingArea;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

public interface PregenArea {

  PregenArea WORLD_BORDER = world -> {
    return LoadingArea.getArea(null, world);
  };

  PregenArea NONE = world -> {
    return null;
  };

  static DataResult<PregenArea> load(JsonElement element) {
    if (element.isJsonNull()) {
      return Results.success(NONE);
    }

    if (element.isJsonPrimitive()) {
      var str = element.getAsString();
      if (str.equalsIgnoreCase("world_border")) {
        return Results.success(WORLD_BORDER);
      }
      if (str.equalsIgnoreCase("none")) {
        return Results.success(NONE);
      }
    }

    if (!element.isJsonObject()) {
      return Results.error("Not an object");
    }

    var json = JsonWrapper.wrap(element.getAsJsonObject());

    if (!json.has("centerX") || !json.has("centerZ")) {
      return Results.error("No centerX, centerZ values set");
    }

    if (!json.has("radius") || (!json.has("radiusX") && !json.has("radiusZ"))) {
      return Results.error("No radius, radiusX or radiusZ values set");
    }

    int centerX = json.getInt("centerX");
    int centerZ = json.getInt("centerZ");

    LoadingArea area;

    if (json.has("radiusX")) {
      int radiusX = json.getInt("radiusX");
      int radiusZ = json.getInt("radiusZ");

      area = LoadingArea.ofRadius(centerX, centerZ, radiusX, radiusZ);
    } else {
      int radius = json.getInt("radius");
      area = LoadingArea.ofRadius(centerX, centerZ, radius, radius);
    }

    return Results.success(new Const(area));
  }

  @Nullable
  LoadingArea createArea(World world);

  record Const(LoadingArea area) implements PregenArea {

    @Override
    public @Nullable LoadingArea createArea(World world) {
      return area;
    }
  }
}
