package net.forthecrown.structure;

import net.forthecrown.DrawThing;
import net.forthecrown.math.Mirror;
import net.forthecrown.math.Rot;
import net.forthecrown.math.Transform;
import net.forthecrown.math.Vec2i;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import static net.forthecrown.Main.LOGGER;

public class Template {
    private final Color color;
    private final Vec2i size;
    private final Vec2i[] data;

    public Template(Color color, Vec2i size, Vec2i[] data) {
        this.color = color;
        this.size = size;
        this.data = data;

        LOGGER.info("Template created: color: {}, size: {}, data: {}", color, size, Arrays.toString(data));
    }

    public static Template of(Color color, int[][] data) {
        List<Vec2i> dataResult = new ArrayList<>();

        for (var i = 0; i < data.length; i++) {
            int[] at = data[i];

            dataResult.add(new Vec2i(at[0], at[1]));
        }

        return new Template(color, findSize(dataResult), dataResult.toArray(Vec2i[]::new));
    }

    private static Vec2i findSize(Iterable<Vec2i> of) {
        Vec2i result = Vec2i.ZERO;

        for (Vec2i v: of) {
            result = v.max(result);
        }

        return result.add(1, 1);
    }

    public void draw(DrawThing draw, Vec2i pivot, Vec2i place, Rot rotation, Transform transform) {
        for (Vec2i v: getData()) {
            Vec2i output = transform.transform(place, v, pivot, Mirror.NONE, rotation);

            draw.draw(output, color);
        }
    }

    public Vec2i getSize() {
        return size;
    }

    public Color getColor() {
        return color;
    }

    public Vec2i[] getData() {
        return data;
    }
}
