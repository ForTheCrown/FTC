package net.forthecrown.utils;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.commons.lang.Validate;

import java.util.*;

/**
 * A random to provide some more methods for greater function
 */
public class CrownRandom extends Random {
    public CrownRandom() {}
    public CrownRandom(long seed) { super(seed); }

    /**
     * Returns an int in the given range
     * @param i The first bound
     * @param j The second bound
     * @return A random integer in the given bounds
     */
    public int intInRange(int i, int j){
        int min = Math.min(i, j);
        int max = Math.max(i, j);

        if (min >= max) return 0;
        return min + nextInt((max - min) + 1);
    }

    /**
     * Gets random entries from a given collection
     * @param from The collection to get entries from
     * @param size The amount of entries to get
     * @param <T> The type of the items
     * @return Random entries from the given list, size of the size param
     */
    public <T> List<T> pickRandomEntries(Collection<T> from, int size){
        Validate.isTrue(from.size() > size, "Collection size was smaller than specified size");

        List<T> orig = from instanceof List ? (List<T>) from : new ArrayList<>(from);
        List<T> result = new ObjectArrayList<>();

        while(result.size() < size){
            T value = orig.get(nextInt(orig.size()));

            while (!attemptAdding(result, value)){
                value = orig.get(nextInt(orig.size()));
            }
        }

        return result;
    }

    /**
     * Picks a singular random entry from the given collection
     * @param from The collection to get from
     * @param <T> The type of the item
     * @return A random entry from the given collection
     */
    public <T> T pickRandomEntry(Collection<T> from){
        if(from.isEmpty()) return null;
        if(from.size() == 1) return from.iterator().next();

        return new ArrayList<>(from).get(nextInt(from.size()));
    }

    private <T> boolean attemptAdding(Collection<T> to, T value) {
        return !to.contains(value) && to.add(value);
    }
}
