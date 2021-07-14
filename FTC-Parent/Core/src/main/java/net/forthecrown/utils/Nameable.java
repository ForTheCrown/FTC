package net.forthecrown.utils;

import net.forthecrown.core.chat.ChatUtils;
import net.kyori.adventure.text.Component;

/**
 * A thing that can be named :3
 */
public interface Nameable {

    /**
     * The name of the object
     * @return
     */
    String getName();

    /**
     * Returns whether the object has a set name
     * @return ^^^^
     */
    default boolean hasName(){
        return !FtcUtils.isNullOrBlank(getName());
    }

    /**
     * Gets the name of the object as a component
     * @return Component name
     */
    default Component name(){
        return ChatUtils.convertString(getName());
    }
}
