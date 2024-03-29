package net.forthecrown.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.FtcSuggestions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.internal.SimpleVanillaMapped;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.EntitySelector;
import net.forthecrown.user.User;
import net.forthecrown.user.UserLookup.LookupEntry;
import net.forthecrown.user.UserService;
import net.forthecrown.user.Users;
import net.minecraft.commands.arguments.ScoreHolderArgument;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;

public class UserArgument
    implements ArgumentType<UserParseResult>, SimpleVanillaMapped
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

    UserService service = Users.getService();
    LookupEntry entry = service.getLookup().query(name);

    if (entry == null) {
      reader.setCursor(cursor);
      throw Exceptions.unknownUser(reader, name);
    }

    User result = Users.get(entry.getUniqueId());
    if (!result.isOnline() && !allowOffline) {
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
  public ArgumentType<?> getVanillaType() {
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