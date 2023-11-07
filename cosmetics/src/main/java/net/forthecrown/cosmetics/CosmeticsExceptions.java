package net.forthecrown.cosmetics;

import static net.forthecrown.command.Exceptions.format;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kyori.adventure.text.Component;

interface CosmeticsExceptions {

  static CommandSyntaxException alreadySetCosmetic(Component cosmeticName, Component typeName) {
    return format("{0} is already your {1} effect",
        cosmeticName, typeName
    );
  }
}