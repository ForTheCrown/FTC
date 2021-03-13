package net.forthecrown.core.enums;

public enum SellAmount {
    PER_64 (64),
    PER_16 (16),
    PER_1 (1),
    ALL (1);

    private final int i;

    SellAmount(int i) {
        this.i = i;
    }

    public Integer getInt(){
        return i;
    }

    public static SellAmount fromInt(int i){
        switch (i){
            case 64:
                return PER_64;
            case 16:
                return PER_16;
            case 1:
                return PER_1;
            case -1:
                return ALL;
            default:
                return null;
        }
    }
}
