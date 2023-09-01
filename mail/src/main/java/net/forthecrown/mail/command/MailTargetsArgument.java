package net.forthecrown.mail.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.arguments.UserArgument;
import net.forthecrown.command.arguments.UserParseResult;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.Readers;
import net.forthecrown.grenadier.internal.SimpleVanillaMapped;
import net.forthecrown.mail.MailPermissions;

class MailTargetsArgument implements ArgumentType<MailTargets>, SimpleVanillaMapped {

  private final UserArgument userArgument;

  public MailTargetsArgument(boolean multipleAllowed) {
    this.userArgument = multipleAllowed ? Arguments.USERS : Arguments.USER;
  }

  @Override
  public MailTargets parse(StringReader reader) throws CommandSyntaxException {
    if (Readers.startsWithArgument(reader, "-all")) {
      reader.setCursor(reader.getCursor() + 4);
      return MailTargets.ALL;
    }

    UserParseResult result = userArgument.parse(reader);

    return source -> {
      var users = result.getUsers(source, false);
      return CompletableFuture.completedFuture(users);
    };
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  ) {
    if (context.getSource() instanceof CommandSource src
        && src.hasPermission(MailPermissions.MAIL_ALL)
    ) {
      Completions.suggest(builder, "-all");
    }

    return userArgument.listSuggestions(context, builder);
  }

  @Override
  public ArgumentType<?> getVanillaType() {
    return userArgument.getVanillaType();
  }
}
