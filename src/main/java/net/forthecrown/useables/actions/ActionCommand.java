package net.forthecrown.useables.actions;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import lombok.Getter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.useables.ConstructType;
import net.forthecrown.useables.Usable;
import net.forthecrown.useables.UsableConstructor;
import net.forthecrown.useables.UsageAction;
import net.forthecrown.useables.UsageType;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

@Getter
public class ActionCommand extends UsageAction {

  private static final SuggestionProvider<CommandSource> COMMAND_SUGGESTIONS = (context, builder) -> {
    String filteredInput = builder.getInput()
        .replaceAll("%p", "ap")
        .replaceAll("%plr", "aplr")
        .replaceAll("%player", "aplayer");

    builder = new SuggestionsBuilder(
        filteredInput,
        builder.getStart()
    );

    return Grenadier.suggestAllCommands()
        .getSuggestions(context, builder);
  };

  private static final CommandSender SILENT_SENDER
      = Bukkit.createCommandSender(component -> {});

  // --- TYPE ---
  public static final UsageType<ActionCommand> TYPE_PLAYER = UsageType.of(ActionCommand.class)
      .setSuggests(COMMAND_SUGGESTIONS);

  public static final UsageType<ActionCommand> TYPE_SERVER = UsageType.of(ActionCommand.class)
      .setSuggests(COMMAND_SUGGESTIONS);

  private final boolean server;
  private final String command;

  public ActionCommand(UsageType<ActionCommand> type, String command) {
    super(type);
    this.command = command;

    this.server = type == TYPE_SERVER;
  }

  @Override
  public void onUse(Player player, Usable holder) {
    String command = replaceSelectors(player.getName(), this.command);

    Bukkit.dispatchCommand(
        server
            ? (holder.isSilent() ? SILENT_SENDER : Bukkit.getConsoleSender())
            : player,

        command
    );
  }

  public static String replaceSelectors(String name, String command) {
    return command
        .replaceAll("%plr", name)
        .replaceAll("%player", name)
        .replaceAll("%p", name);
  }

  @Override
  public @Nullable Component displayInfo() {
    return Text.format("cmd='{0}'", command);
  }

  @Override
  public @Nullable BinaryTag save() {
    return BinaryTags.stringTag(command);
  }

  // --- TYPE CONSTRUCTORS ---

  @UsableConstructor(ConstructType.PARSE)
  public static ActionCommand parse(UsageType<ActionCommand> type,
                                    StringReader reader,
                                    CommandSource source
  ) throws CommandSyntaxException {
    String result = reader.getRemaining();
    reader.setCursor(reader.getTotalLength());

    if (result.startsWith("/")) {
      source.sendMessage(
          "Command starts with '/', this isn't required for most commands"
              + "\n(Is this intentional?)"
      );
    }

    return new ActionCommand(type, result);
  }

  @UsableConstructor(ConstructType.TAG)
  public static ActionCommand load(UsageType<ActionCommand> type, BinaryTag tag) {
    return new ActionCommand(type, tag.toString());
  }
}