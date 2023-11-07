package net.forthecrown.command.request;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kyori.adventure.audience.Audience;

public interface RequestValidator<R extends PlayerRequest> {

  void validate(R request, Audience viewer) throws CommandSyntaxException;
}
