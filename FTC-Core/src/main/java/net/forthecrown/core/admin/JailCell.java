package net.forthecrown.core.admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.sk89q.worldedit.math.Vector3;
import lombok.Data;
import net.forthecrown.core.chat.ComponentWriter;
import net.forthecrown.serializer.JsonDeserializable;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.utils.math.Bounds3i;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a single jail cell
 * <p></p>
 * Since this class uses lombok and I can't
 * add javadoc to that, I'll explain the variables
 * here.
 *
 * <p></p>
 * {@code world} - The world the cell is in
 * <p>
 * {@code pos} - The position anyone placed in this cell will be placed at
 * <p>
 * {@code cell} - The bounds of the cell, if a prisoner leaves it, they will
 *                teleported back in
 * </p></p>
 */
@Data
public class JailCell implements Keyed, JsonSerializable, JsonDeserializable {
    private final Key key;

    private World world;
    private Vector3 pos;
    private Bounds3i cell;

    @Override
    public @NotNull Key key() {
        return key;
    }

    /**
     * Writes the cell's info to the given writer.
     * @param writer The writer to write to
     */
    public void writeDisplay(ComponentWriter writer) {
        writer.write(Component.text("Pos: " + pos));
        writer.newLine();
        writer.write(Component.text("Bounds: " + getCell()));
        writer.newLine();
        writer.write(Component.text("World: " + world.getName()));
        writer.newLine();
        writer.write(Component.text("Name: " + key.value()));
    }

    @Override
    public JsonElement serialize() {
        JsonWrapper json = JsonWrapper.empty();

        json.add("cell", cell);
        json.add("pos", writePos(pos));
        json.add("world", world.getName());

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

    @Override
    public void deserialize(JsonElement element) {
        JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());
        setCell(Bounds3i.of(json.get("cell")));
        setWorld(Bukkit.getWorld(json.getString("world")));
        setPos(readPos(json.get("pos")));
    }
}