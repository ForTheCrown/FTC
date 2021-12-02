package net.forthecrown.commands.punishments;

public interface GenericPunisher {
    default long lengthTranslate(long length){
        return length == -1 ? -1 : System.currentTimeMillis() + length;
    }
}
