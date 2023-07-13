package net.forthecrown.text.format;

import net.forthecrown.text.Text;
import net.forthecrown.utils.math.WorldVec3i;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class LocationFormatType implements TextFormatType {

  @Override
  public @NotNull Component resolve(Object value, String style, Audience viewer) {
    boolean includeWorld = style.contains("-w");
    boolean clickable = style.contains("-c");

    if (value instanceof Location l) {
      return location(includeWorld, clickable, l);
    }

    if (value instanceof WorldVec3i vec3i) {
      return location(includeWorld, clickable, vec3i.toLocation());
    }

    return Text.valueOf(value);
  }

  private Component location(boolean world, boolean clickable, Location l) {
    if (clickable) {
      return Text.clickableLocation(l, world);
    }

    return Text.prettyLocation(l, world);
  }
}
