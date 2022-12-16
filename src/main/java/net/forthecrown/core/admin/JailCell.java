package net.forthecrown.core.admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.sk89q.worldedit.math.Vector3;
import lombok.Data;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.utils.text.writer.TextWriter;
import net.forthecrown.utils.JsonSerializable;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.math.Bounds3i;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.spongepowered.math.vector.Vector3d;

/**
 * Represents a single jail cell
 * <p>
 * Since this class uses lombok and I can't
 * add javadoc to that, I'll explain the variables
 * here.
 *
 */
@Data
public class JailCell implements JsonSerializable {
    public static final String
            KEY_WORLD = "world",
            KEY_POSITION = "pos",
            KEY_BOUNDS = "cell";

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
     * @param writer The writer to write to
     */
    public void writeDisplay(TextWriter writer) {
        writer.field("World", world.getName());
        writer.field("Pos", pos);
        writer.field("Cell", cell);
    }

    @Override
    public JsonElement serialize() {
        JsonWrapper json = JsonWrapper.create();

        json.add(KEY_BOUNDS, cell);
        json.add(KEY_POSITION, pos);
        json.add(KEY_WORLD, world.getName());

        return json.getSource();
    }

    private static JsonElement writePos(Vector3 vec) {
        JsonArray array = new JsonArray();
        array.add(vec.getX());
        array.add(vec.getY());
        array.add(vec.getZ());

        return array;
    }

    private static Vector3 readPos(JsonElement element) {
        JsonArray arr = element.getAsJsonArray();

        return Vector3.at(
                arr.get(0).getAsDouble(),
                arr.get(1).getAsDouble(),
                arr.get(2).getAsDouble()
        );
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