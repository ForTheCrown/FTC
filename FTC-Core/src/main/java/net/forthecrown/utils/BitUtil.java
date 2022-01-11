package net.forthecrown.utils;

public final class BitUtil {
    private BitUtil() {}

    public static boolean readBit(long num, int index) {
        return ((num >> index) & 1) == 1;
    }

    public static long setBit(long num, int index, boolean val) {
        return val ? (num | (1L << index)) : (num & ~(1L << index));
    }

    public static int setBit(int num, int index, boolean val) {
        return (int) setBit((long) num, index, val);
    }

    public static short setBit(short num, int index, boolean val) {
        return (short) setBit((long) num, index, val);
    }
}