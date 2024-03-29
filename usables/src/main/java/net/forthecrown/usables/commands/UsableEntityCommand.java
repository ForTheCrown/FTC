package net.forthecrown.usables.commands;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.usables.UPermissions;
import net.forthecrown.usables.Usables;
import net.forthecrown.usables.objects.UsableEntity;
import org.bukkit.entity.Entity;
import org.bukkit.permissions.Permission;

public class UsableEntityCommand extends InWorldUsableCommand<UsableEntity> {

  public UsableEntityCommand() {
    super("usableentity", "entity");
    setAliases("usable_entity");
    setPermission(UPermissions.ENTITY);
  }

  @Override
  public Permission getAdminPermission() {
    return UPermissions.ENTITY;
  }

  @Override
  protected String usagePrefix() {
    return "<entity: uuid | @selector>";
  }

  @Override
  protected ArgumentType<?> getArgumentType() {
    return ArgumentTypes.entity();
  }

  @Override
  protected SuggestionProvider<CommandSource> getSuggestions() {
    return Completions.combine(
        ArgumentTypes.entity()::listSuggestions,

        (context, builder) -> {
          var source = context.getSource();

          if (!source.isPlayer()) {
            return Suggestions.empty();
          }

          var player = source.asPlayer();
          var hit = player.rayTraceEntities(5, false);

          if (hit == null || hit.getHitEntity() == null) {
            return Suggestions.empty();
          }

          Completions.suggest(builder, hit.getHitEntity().getUniqueId().toString());
          return builder.buildFuture();
        }
    );
  }

  @Override
  protected UsableProvider<UsableEntity> getProvider(String argument) {
    return new UsableProvider<>() {
      @Override
      public UsableEntity get(CommandContext<CommandSource> context) throws CommandSyntaxException {
        Entity entity = ArgumentTypes.getEntity(context, argument);
        var usable = Usables.entity(entity);

        if (Usables.isUsable(entity)) {
          usable.load();
        }

        return usable;
      }

      @Override
      public void postEdit(UsableEntity holder) {
        holder.save();
      }
    };
  }
}
