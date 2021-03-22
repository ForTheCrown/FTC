package net.forthecrown.core.api;

import net.forthecrown.core.utils.ComponentUtils;
import net.forthecrown.core.utils.CrownUtils;
import net.kyori.adventure.text.Component;

public interface Nameable {
    String getName();

    default boolean hasName(){
        return !CrownUtils.isNullOrBlank(getName());
    }

    default Component name(){
        return ComponentUtils.convertString(getName());
    }
}
