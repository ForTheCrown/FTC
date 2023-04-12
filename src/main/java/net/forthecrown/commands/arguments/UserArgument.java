package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcSuggestions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.internal.VanillaMappedArgument;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.EntitySelector;
import net.forthecrown.user.User;
import net.forthecrown.user.UserLookupEntry;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.Users;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.ScoreHolderArgument;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;

public class UserArgument
    implements ArgumentType<UserParseResult>, VanillaMappedArgument
{

  public final boolean allowMultiple;
  public final boolean allowOffline;

  UserArgument(boolean allowMultiple, boolean allowOffline) {
    this.allowMultiple = allowMultiple;
    this.allowOffline = allowOffline;
  }

  @Override
  public UserParseResult parse(StringReader reader) throws CommandSyntaxException {
    if (reader.peek() == '@') {
      EntitySelector selector = allowMultiple
          ? ArgumentTypes.players().parse(reader, true)
          : ArgumentTypes.player().parse(reader, true);

      return new UserParseResult(selector, allowOffline);
    }

    final int cursor = reader.getCursor();

    String name = reader.readUnquotedString();
    UserLookupEntry entry = UserManager.get()
        .getUserLookup()
        .get(name);

    if (entry == null) {
      throw Exceptions.unknownUser(reader, cursor, name);
    }

    User result = Users.get(entry.getUniqueId());
    if (!result.isOnline() && !allowOffline) {
      result.unloadIfOffline();
      throw Exceptions.notOnline(result);
    }

    return new UserParseResult(result, allowOffline);
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context,
                                                            SuggestionsBuilder builder
  ) {
    if (!(context.getSource() instanceof CommandSource)) {
      return Suggestions.empty();
    }

    StringReader reader = new StringReader(builder.getInput());
    reader.setCursor(builder.getStart());

    EntitySelectorParser parser = new EntitySelectorParser(reader, true);

    try {
      parser.parse();
    } catch (CommandSyntaxException ignored) {
    }

    return parser.fillSuggestions(builder, builder1 -> {
      FtcSuggestions.suggestPlayerNames(
          (CommandSource) context.getSource(),
          builder1,
          allowOffline
      );
    });
  }

  @Override
  public ArgumentType<?> getVanillaType(CommandBuildContext context) {
    if (allowOffline) {
      if (allowMultiple) {
        return ScoreHolderArgument.scoreHolders();
      }

      return ScoreHolderArgument.scoreHolder();
    }

    if (allowMultiple) {
      return net.minecraft.commands.arguments.EntityArgument.players();
    }

    return net.minecraft.commands.arguments.EntityArgument.player();
  }
}