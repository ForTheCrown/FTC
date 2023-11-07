package net.forthecrown.usables.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.usables.objects.UsableObject;

public interface UsableProvider<H extends UsableObject> {

  H get(CommandContext<CommandSource> context) throws CommandSyntaxException;

  default void postEdit(H holder) {

  }
}
