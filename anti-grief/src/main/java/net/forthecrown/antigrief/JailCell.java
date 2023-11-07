package net.forthecrown.antigrief;

import com.google.gson.JsonElement;
import lombok.Data;
import net.forthecrown.text.TextWriter;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.utils.math.Vectors;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.spongepowered.math.vector.Vector3d;

/**
 * Represents a single jail cell
 */
@Data
public class JailCell {

  public static final String KEY_WORLD = "world";
  public static final String KEY_POSITION = "pos";
  public static final String KEY_BOUNDS = "cell";

  /**
   * The World the cell is in
   */
  private final World world;

  /**
   * The position of the cell
   */
  private final Vector3d pos;

  /**
   * The bounds of the cell
   */
  private final Bounds3i cell;

  /**
   * Writes the cell's info to the given writer.
   *
   * @param writer The writer to write to
   */
  public void writeDisplay(TextWriter writer) {
    writer.field("World", world.getName());
    writer.field("Pos", pos);
    writer.field("Cell", cell);
  }

  public JsonElement serialize() {
    JsonWrapper json = JsonWrapper.create();

    json.add(KEY_BOUNDS, cell.serialize());
    json.add(KEY_POSITION, pos);
    json.add(KEY_WORLD, world.getName());

    return json.getSource();
  }

  public static JailCell deserialize(JsonElement element) {
    JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

    return new JailCell(
        Bukkit.getWorld(json.getString(KEY_WORLD)),
        Vectors.read3d(json.get(KEY_POSITION)),
        Bounds3i.of(json.get(KEY_BOUNDS))
    );
  }
}