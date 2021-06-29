package net.forthecrown.cosmetics.effects;

import net.forthecrown.cosmetics.custominvs.CustomInv;
import net.kyori.adventure.text.TextComponent;

/**
 * Final methods can not be overridden.
 * Abstract methods have to be overridden.
 * Normal methods can be overridden.
 */
public interface CosmeticMenu {

    CustomInv getCustomInv();

    TextComponent getTitle();

    int getSize();
}
