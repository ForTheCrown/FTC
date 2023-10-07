package net.forthecrown.usables.actions;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.usables.Action;
import net.forthecrown.usables.Interaction;
import net.forthecrown.usables.UsableComponent;
import net.forthecrown.usables.ObjectType;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class CommandAction implements Action {

  public static final ObjectType<CommandAction> AS_PLAYER = new CommandActionType(true);
  public static final ObjectType<CommandAction> AS_SELF = new CommandActionType(false);

  private final String command;
  private final boolean asPlayer;

  public CommandAction(String command, boolean asPlayer) {
    this.command = command;
    this.asPlayer = asPlayer;
  }

  static String formatPlaceholders(String cmd, Player player) {
    var l = player.getLocation();

    return cmd
        .replace("%p", player.getName())
        .replace("%plr", player.getName())
        .replace("%player", player.getName())
        .replace("%player.block", String.format("%s %s %s", l.getBlockX(), l.getBlockY(), l.getBlockZ()))
        .replace("%player.pos", String.format("%s %s %s", l.getX(), l.getY(), l.getZ()))
        .replace("%player.x", l.getX() + "")
        .replace("%player.y", l.getY() + "")
        .replace("%player.z", l.getZ() + "")
        .replace("%player.bx", l.getBlockX() + "")
        .replace("%player.by", l.getBlockY() + "")
        .replace("%player.bz", l.getBlockZ() + "")
        .replace("%player.yaw", l.getYaw() + "")
        .replace("%player.pitch", l.getPitch() + "")
        .replace("%player.uuid", player.getUniqueId() + "");
  }

  @Override
  public void onUse(Interaction interaction) {
    var player = interaction.player();
    String formattedCommand = formatPlaceholders(command, player);

    if (asPlayer) {
      player.performCommand(formattedCommand);
    } else {
      CommandSender sender = interaction.object().getCommandSender();
      Bukkit.dispatchCommand(sender, formattedCommand);
    }
  }

  @Override
  public ObjectType<? extends UsableComponent> getType() {
    return asPlayer ? AS_PLAYER : AS_SELF;
  }

  @Override
  public @Nullable Component displayInfo() {
    return Component.text(command);
  }
}

class CommandActionType implements ObjectType<CommandAction> {

  private final boolean asPlayer;

  public CommandActionType(boolean asPlayer) {
    this.asPlayer = asPlayer;
  }

  @Override
  public CommandAction parse(StringReader reader, CommandSource source)
      throws CommandSyntaxException
  {
    String remaining = reader.getRemaining();
    reader.setCursor(reader.getTotalLength());
    return new CommandAction(remaining, asPlayer);
  }

  @Override
  public @NotNull <S> DataResult<CommandAction> load(@Nullable Dynamic<S> dynamic) {
    return dynamic.asString().map(s -> new CommandAction(s, asPlayer));
  }

  @Override
  public <S> DataResult<S> save(@NotNull CommandAction value, @NotNull DynamicOps<S> ops) {
    return DataResult.success(ops.createString(value.getCommand()));
  }

  @Override
  public CompletableFuture<Suggestions> getSuggestions(
      CommandContext<CommandSource> context,
      SuggestionsBuilder builder
  ) {
    try {
      return Grenadier.suggestAllCommands().getSuggestions(context, builder);
    } catch (CommandSyntaxException e) {
      throw new RuntimeException(e);
    }
  }
}
