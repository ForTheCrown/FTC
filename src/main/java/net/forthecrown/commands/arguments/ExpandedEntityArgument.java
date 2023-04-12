package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.commands.manager.FtcSuggestions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.PermissionLevel;
import net.forthecrown.grenadier.Readers;
import net.forthecrown.grenadier.internal.VanillaMappedArgument;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.EntityArgument;
import net.forthecrown.grenadier.types.EntitySelector;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

public class ExpandedEntityArgument
    implements ArgumentType<EntitySelector>, VanillaMappedArgument
{

  private final EntityArgument argument;

  public ExpandedEntityArgument(boolean multiple, boolean playersOnly) {
    if (playersOnly) {
      this.argument = multiple
          ? ArgumentTypes.players()
          : ArgumentTypes.player();
    } else {
      this.argument = multiple
          ? ArgumentTypes.entities()
          : ArgumentTypes.entity();
    }
  }

  @Override
  public EntitySelector parse(StringReader reader)
      throws CommandSyntaxException
  {
    if (reader.canRead() && reader.peek() == '@') {
      return argument.parse(reader, true);
    }

    final int start = reader.getCursor();
    var str = Readers.readUntilWhitespace(reader);

    try {
      UUID uuid = UUID.fromString(str);
      Entity entity = Bukkit.getEntity(uuid);

      if (entity == null) {
        throw Grenadier.exceptions().noEntityFound();
      }
    } catch (IllegalArgumentException exc) {
      // Ignored, move onto parsing from username
    }

    reader.setCursor(start);

    UserArgument userArgument = argument.allowsMultiple()
        ? Arguments.ONLINE_USERS
        : Arguments.ONLINE_USER;

    var result = userArgument.parse(reader);
    return new UserSelectorResult(result);
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  ) {
    if (!(context.getSource() instanceof CommandSource s)) {
      return Suggestions.empty();
    }

    boolean hasPermission
        = s.hasPermission("minecraft.command.selector", PermissionLevel.GAME_MASTERS)
        || s.overrideSelectorPermissions();

    EntitySelectorParser parser = new EntitySelectorParser(
        Readers.forSuggestions(builder),
        hasPermission,
        true
    );

    try {
      parser.parse();
    } catch (CommandSyntaxException exc) {
      // Ignored
    }

    return parser.fillSuggestions(builder, builder1 -> {
      var entities = s.getEntitySuggestions();

      Completions.suggest(builder, entities);
      FtcSuggestions.suggestPlayerNames(s, builder1, false);
    });
  }

  @Override
  public ArgumentType<?> getVanillaType(CommandBuildContext context) {
    return ((VanillaMappedArgument) argument).getVanillaType(context);
  }
}