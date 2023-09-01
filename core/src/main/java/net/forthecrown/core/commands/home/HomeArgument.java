package net.forthecrown.core.commands.home;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.command.Exceptions;
import net.forthecrown.core.CorePermissions;
import net.forthecrown.core.user.UserHomes;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Readers;
import net.forthecrown.user.Users;

public class HomeArgument implements ArgumentType<HomeParseResult> {
  private HomeArgument() {}

  public static final HomeArgument HOME = new HomeArgument();

  @Override
  public HomeParseResult parse(StringReader reader) throws CommandSyntaxException {
    int cursor = reader.getCursor();
    String name = readName(reader);

    StringReader startReader = Readers.copy(reader, cursor);

    if (reader.canRead() && reader.peek() == ':') {
      reader.skip();

      String homeName = readName(reader);
      var users = Users.getService();
      var entry = users.getLookup().query(name);

      if (entry == null) {
        reader.setCursor(cursor);
        throw Exceptions.unknownUser(reader, name);
      }

      return new HomeParseResult(startReader, entry, homeName);
    }

    return new HomeParseResult(startReader, name);
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  ) {
    CommandSource source = (CommandSource) context.getSource();
    StringReader reader = new StringReader(builder.getInput());
    reader.setCursor(builder.getStart());

    var name = readName(reader);

    // Source can view other players' homes
    if (source.hasPermission(CorePermissions.HOME_OTHERS)) {
      var entry = Users.getService().getLookup().query(name);

      // If valid name given
      if (entry != null) {

        // If ':' type in, skip it,
        if (reader.canRead() && reader.peek() == ':') {
          reader.skip();
          builder = builder.createOffset(reader.getCursor());
        }
        // Else do a lil hack to get Brigadier to prepend ':'
        // to all suggestions
        else {
          builder = builder.createOffset(reader.getCursor());
          builder.suggest(":");

          var newBuilder = builder.createOffset(reader.getCursor());
          newBuilder.add(builder);

          builder = newBuilder;
        }

        var user = Users.get(entry);
        user.getComponent(UserHomes.class).suggestHomeNames(builder);

        return builder.buildFuture();
      }
    }

    // Suggest source's home names
    if (source.isPlayer()) {
      var user = Users.get(source.asPlayerOrNull());
      user.getComponent(UserHomes.class).suggestHomeNames(builder);
    }

    return builder.buildFuture();
  }

  private String readName(StringReader reader) {
    int start = reader.getCursor();

    while (reader.canRead() && reader.peek() != ':' && !Character.isWhitespace(reader.peek())) {
      reader.skip();
    }

    return reader.getString().substring(start, reader.getCursor());
  }

  @Override
  public Collection<String> getExamples() {
    return Arrays.asList("home", "JulieWoolie:home", "Robinoh:nether", "base", "farm");
  }
}