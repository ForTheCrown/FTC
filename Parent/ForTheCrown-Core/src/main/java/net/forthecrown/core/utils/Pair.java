package net.forthecrown.core.utils;

/**
 * A class containing two values
 * @param <T> The first value
 * @param <V> The second value
 */
public class Pair<T, V> {

    private T first;
    private V second;

    public Pair(T value, V value1){
        this.first = value;
        this.second = value1;
    }

    public Pair(){}

    public T getFirst() {
        return first;
    }

    public void setFirst(T first) {
        this.first = first;
    }

    public V getSecond() {
        return second;
    }

    public void setSecond(V second) {
        this.second = second;
    }

    public T getOrDefaultFirst(T def){
        return first == null ? def : first;
    }

    public V getOrDefaultSecond(V def){
        return second == null ? def : second;
    }
}
