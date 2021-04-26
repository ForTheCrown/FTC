package net.forthecrown.vikings;

public class VikingUtils {

    public static byte findBiggestInArray(byte[] array){
        byte biggest = array[0];
        for (byte b: array){
            if(b > biggest) biggest = b;
        }
        return biggest;
    }

}