package net.forthecrown.core.utils;

import org.apache.commons.lang.Validate;

import java.util.*;

public class CrownRandom extends Random {

    public CrownRandom() {
    }

    public CrownRandom(long seed) {
        super(seed);
    }

    public int intInRange(int i, int j){
        int min = Math.min(i, j);
        int max = Math.max(i, j);

        if (min >= max) return 0;
        return min + nextInt((max - min) + 1);
    }

    public <T> List<T> pickRandomEntries(Collection<T> from, int size){
        Validate.isTrue(from.size() > size, "Collection size was smaller than specified size");

        List<T> orig = new ArrayList<>(from);
        List<T> result = new ArrayList<>();

        while(result.size() < size){
            T value = orig.get(nextInt(orig.size()));

            while (!attemptAdding(result, value)){
                value = orig.get(nextInt(orig.size()));
            }
        }

        return result;
    }

    private <T> boolean attemptAdding(Collection<T> to, T value){
        if(to.contains(value)) return false;
        to.add(value);
        return true;
    }
}
