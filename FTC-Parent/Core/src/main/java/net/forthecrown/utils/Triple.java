package net.forthecrown.utils;

public class Triple<F, S, T> extends Pair<F, S> {

    protected T third;

    public Triple(F value, S value1, T value2){
        this.first = value;
        this.second = value1;
        this.third = value2;
    }

    public Triple(F value, S value1) {
        super(value, value1);
    }

    public Triple(F value) {
        super(value);
    }

    public Triple() {
    }

    public T getThird() {
        return third;
    }

    public T getOrDefaultThird(T def){
        return third == null ? def : third;
    }
}
