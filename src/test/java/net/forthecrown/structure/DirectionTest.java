package net.forthecrown.structure;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DirectionTest {

    @Test
    void opposite() {
        assertEquals(Direction.NORTH.opposite(), Direction.SOUTH);
        assertEquals(Direction.WEST.opposite(), Direction.EAST);
        assertEquals(Direction.SOUTH.opposite(), Direction.NORTH);
        assertEquals(Direction.EAST.opposite(), Direction.WEST);
        assertEquals(Direction.UP.opposite(), Direction.DOWN);
        assertEquals(Direction.DOWN.opposite(), Direction.UP);
    }

    @Test
    void left() {
        assertEquals(Direction.NORTH.left(), Direction.WEST);
        assertEquals(Direction.WEST.left(), Direction.SOUTH);
        assertEquals(Direction.SOUTH.left(), Direction.EAST);
        assertEquals(Direction.EAST.left(), Direction.NORTH);

        assertNull(Direction.UP.left());
        assertNull(Direction.DOWN.left());
    }

    @Test
    void right() {
        assertEquals(Direction.NORTH.right(), Direction.EAST);
        assertEquals(Direction.EAST.right(), Direction.SOUTH);
        assertEquals(Direction.SOUTH.right(), Direction.WEST);
        assertEquals(Direction.WEST.right(), Direction.NORTH);

        assertNull(Direction.UP.right());
        assertNull(Direction.DOWN.right());
    }

    @Test
    void rotate() {
        assertEquals(Direction.NORTH.rotate(Rotation.CLOCKWISE_180), Direction.SOUTH);
        assertThrows(IllegalArgumentException.class, () -> Direction.UP.rotate(Rotation.CLOCKWISE_90));
    }

    @Test
    void deriveRotationFrom() {
        assertEquals(
                Direction.WEST.deriveRotationFrom(Direction.WEST),
                Rotation.NONE
        );

        assertEquals(
                Direction.WEST.deriveRotationFrom(Direction.NORTH),
                Rotation.CLOCKWISE_90
        );

        assertEquals(
                Direction.NORTH.deriveRotationFrom(Direction.WEST),
                Rotation.COUNTERCLOCKWISE_90
        );

        assertEquals(
                Direction.NORTH.deriveRotationFrom(Direction.SOUTH),
                Rotation.CLOCKWISE_180
        );
    }
}