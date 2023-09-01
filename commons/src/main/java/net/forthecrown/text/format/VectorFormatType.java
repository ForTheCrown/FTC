package net.forthecrown.text.format;

import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongList;
import java.util.List;
import net.forthecrown.text.Text;
import net.forthecrown.utils.math.Vectors;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.math.vector.Vectord;
import org.spongepowered.math.vector.Vectorf;
import org.spongepowered.math.vector.Vectori;
import org.spongepowered.math.vector.Vectorl;

class VectorFormatType implements TextFormatType {

  @Override
  public @NotNull Component resolve(@NotNull Object value, @NotNull String style, Audience viewer) {
    if (value instanceof Location loc) {
      return of(DoubleList.of(loc.getX(), loc.getY(), loc.getZ()));
    }

    if (value instanceof Vector vec) {
      return of(DoubleList.of(vec.getX(), vec.getY(), vec.getZ()));
    }

    if (value instanceof Vectori veci) {
      return of(IntList.of(veci.toArray()));
    }

    if (value instanceof Vectord vecd) {
      return of(DoubleList.of(vecd.toArray()));
    }

    if (value instanceof Vectorf vecf) {
      return of(FloatList.of(vecf.toArray()));
    }

    if (value instanceof Vectorl vecl) {
      return of(LongList.of(vecl.toArray()));
    }

    return Text.valueOf(value);
  }

  private Component of(List<? extends Number> nList) {
    if (nList.size() > Vectors.NAMEABLE_AXES_LENGTH) {
      return Text.valueOf(nList.toString());
    }

    // Special case for 2D vectors, in our current context
    // 2D vectors are always represented as X and Z, never as
    // X and Y, but the math library uses them as X and Y
    if (nList.size() == 2) {
      return text("x" + nList.get(0) + " z" + nList.get(1));
    }

    TextComponent.Builder builder = text();
    var it = nList.listIterator();

    while (it.hasNext()) {
      int i = it.nextIndex();
      var n = it.next();

      String axis = Vectors.AXES[i];
      builder.append(text(n + axis));

      if (it.hasNext()) {
        builder.append(space());
      }
    }

    return builder.build();
  }
}
