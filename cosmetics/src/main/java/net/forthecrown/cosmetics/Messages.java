package net.forthecrown.cosmetics;

import static net.forthecrown.text.Text.format;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

interface Messages {

  static Component setCosmetic(Cosmetic<?> cosmetic) {
    return format(
        "Set &e{0}&r to be your &6{1}&r effect.",
        NamedTextColor.GRAY,

        cosmetic.displayName(),
        cosmetic.getType().getDisplayName()
    );
  }
}