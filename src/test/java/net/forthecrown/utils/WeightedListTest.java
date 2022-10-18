package net.forthecrown.utils;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WeightedListTest {
    static final Random RANDOM = new Random();

    @Test
    void add() {
        WeightedList<String> list = new WeightedList<>();
        list.add(10, "string_1");
        list.add(10, "string_2");

        assertSame(list.size(), 2);
        assertSame(list.getTotalWeight(), 20);
        assertFalse(list.isEmpty());

        assertTrue(list.get(RANDOM).contains("string_"));
    }

    @Test
    void iterator() {
        WeightedList<String> randomStrings = new WeightedList<>();
        var it = randomStrings.iterator(RANDOM);

        assertFalse(it.hasNext());

        Set<String> strings = Set.of(
                "string_1", "string_2", "string_3", "string_4", "string_5"
        );

        for (var s: strings) {
            randomStrings.add(10, s);
        }

        assertSame(randomStrings.size(), strings.size());

        it = randomStrings.iterator(RANDOM);
        assertTrue(it.hasNext());
        assertSame(strings.size() * 10, randomStrings.getTotalWeight());

        Set<String> found = new HashSet<>();
        System.out.println(randomStrings.getTotalWeight());

        while (it.hasNext()) {
            found.add(it.next());
        }

        assertSame(strings.size(), found.size());
    }
}