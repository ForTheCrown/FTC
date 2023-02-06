package net.forthecrown.utils.text.function;

import static net.forthecrown.utils.text.ChatParser.FLAG_EMOTES;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.regex.MatchResult;
import net.forthecrown.utils.text.ChatEmotes;
import net.forthecrown.utils.text.TextFunction;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

public class EmoteFunction extends TextFunction {

  public EmoteFunction() {
    super(FLAG_EMOTES, ":[^:]+:");
  }

  @Override
  public @Nullable Component render(MatchResult group, int flags)
      throws CommandSyntaxException {
    return ChatEmotes.TOKEN_2_EMOTE.get(group.group());
  }
}