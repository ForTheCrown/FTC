package net.forthecrown.webmap;

import static net.forthecrown.webmap.WebMap.map;

import java.io.InputStream;
import java.util.Optional;
import java.util.function.Supplier;
import net.forthecrown.Loggers;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;
import org.bukkit.World;
import org.slf4j.Logger;

public final class WebMaps {
  private WebMaps() {}

  private static final Logger LOGGER = Loggers.getLogger();

  public static Optional<MapLayer> findOrDefineLayer(World world, String id, String name) {
    return map().getLayer(world, id)
        .or(() -> {
          var result =  map().createLayer(world, id, name)
              .mapError(string -> "Error creating marker set: " + string);

          result.applyError(LOGGER::error);

          return Optional.ofNullable(result.getValue());
        });
  }

  public static Optional<MapIcon> findOrDefineIcon(
      String id,
      String name,
      Supplier<InputStream> dataSupplier
  ) {
    return map().getIcon(id)
        .or(() -> {
          var result = map().createIcon(id, name, dataSupplier.get())
              .mapError(string -> "Failed to create map icon: " + string);

          result.applyError(LOGGER::error);

          return Optional.ofNullable(result.getValue());
        });
  }

  public static Color fromTextColor(TextColor color) {
    return Color.fromRGB(color.value());
  }

  public static Color setAlpha(Color base, int alpha) {
    int r = base.getRed();
    int g = base.getGreen();
    int b = base.getBlue();
    return Color.fromARGB(alpha, r, g, b);
  }

  public static Color setAlpha(Color base, double alpha) {
    return setAlpha(base, (int) alpha * 255);
  }
}
