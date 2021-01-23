package net.forthecrown.core.enums;

public enum SellAmount {
    PER_64 (64),
    PER_16 (16),
    PER_1 (1),
    ALL (-1);

    private final int i;

    SellAmount(int i) {
        this.i = i;
    }

    public Integer getInt(){
        return i;
    }
}
