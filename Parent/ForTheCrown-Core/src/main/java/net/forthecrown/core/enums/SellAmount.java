package net.forthecrown.core.enums;

public enum SellAmount {
    PER_64 ((byte) 64),
    PER_16 ((byte) 16),
    PER_1 ((byte) 1),
    ALL ((byte) 1);

    private final byte i;

    SellAmount(byte i) {
        this.i = i;
    }

    public Byte getInt(){
        return i;
    }

    public static SellAmount fromInt(byte i){
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
