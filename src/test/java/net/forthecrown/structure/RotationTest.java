package net.forthecrown.structure;

import org.junit.jupiter.api.Test;
import org.spongepowered.math.vector.Vector3i;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RotationTest {

    @Test
    void add() {
        assertEquals(Rotation.NONE, Rotation.COUNTERCLOCKWISE_90.add());
        assertEquals(Rotation.CLOCKWISE_90, Rotation.NONE.add());
        assertEquals(Rotation.CLOCKWISE_180, Rotation.CLOCKWISE_90.add());
        assertEquals(Rotation.CLOCKWISE_180, Rotation.NONE.add().add());
        assertEquals(Rotation.COUNTERCLOCKWISE_90, Rotation.CLOCKWISE_180.add());
    }

    @Test
    void rotate() {
        var input = Vector3i.from(1, 2, 3);
        var v180R = Vector3i.from(-1, 2, -3);

        assertEquals(v180R, Rotation.CLOCKWISE_180.rotate(input));
        assertEquals(v180R, Rotation.CLOCKWISE_90.rotate(Rotation.CLOCKWISE_90.rotate(input)));
        assertEquals(v180R, Rotation.COUNTERCLOCKWISE_90.rotate(Rotation.COUNTERCLOCKWISE_90.rotate(input)));
        assertEquals(input, Rotation.CLOCKWISE_180.rotate(Rotation.CLOCKWISE_180.rotate(input)));

        assertEquals(Vector3i.from(3, 2, -1), Rotation.COUNTERCLOCKWISE_90.rotate(input));
    }
}