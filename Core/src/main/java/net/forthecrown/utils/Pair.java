package net.forthecrown.utils;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * A class containing two values
 * @param <T> The first value
 * @param <V> The second value
 */
public class Pair<T, V> {

    protected T first;
    protected V second;

    public Pair(T value, V value1){
        this.first = value;
        this.second = value1;
    }

    public Pair(T value){
        this.first = value;
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

    public Pair<V, T> swap(){
        return new Pair<>(getSecond(), getFirst());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Pair<?, ?> pair = (Pair<?, ?>) o;

        return new EqualsBuilder()
                .append(getFirst(), pair.getFirst())
                .append(getSecond(), pair.getSecond())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getFirst())
                .append(getSecond())
                .toHashCode();
    }
}
