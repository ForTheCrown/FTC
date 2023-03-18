package net.forthecrown.useables.actions;

import com.google.common.base.Strings;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.arguments.RegistryArguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.useables.ConstructType;
import net.forthecrown.useables.Usable;
import net.forthecrown.useables.UsableConstructor;
import net.forthecrown.useables.UsageAction;
import net.forthecrown.useables.UsageType;
import net.forthecrown.user.Users;
import net.forthecrown.utils.dialogue.Dialogue;
import net.forthecrown.utils.dialogue.DialogueManager;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.Logger;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

@Getter
public class ActionDialogue extends UsageAction {

  private static final Logger LOGGER = Loggers.getLogger();

  private static final RegistryArguments<Dialogue> PARSER
      = new RegistryArguments<>(
          DialogueManager.getDialogues().getRegistry(),
          "Conversation"
      );

  public static final UsageType<ActionDialogue> TYPE
      = UsageType.of(ActionDialogue.class)
      .setSuggests(ActionDialogue::suggest);

  private final String entryName;
  private final String nodeName;

  public ActionDialogue(String entryName, String nodeName) {
    super(TYPE);
    this.entryName = entryName;
    this.nodeName = nodeName;
  }

  @Override
  public void onUse(Player player, Usable holder) {
    var registry = DialogueManager.getDialogues().getRegistry();

    LOGGER.debug("onUse: entry={} node={}", entryName, nodeName);

    registry.get(entryName).ifPresentOrElse(entry -> {
      entry.run(Users.get(player), nodeName).ifPresent(s -> {
        LOGGER.warn("Couldn't run '{}': {}", entryName, s);
      });

    }, () -> {
      LOGGER.warn("Couldn't find dialog '{}'", entryName);
    });
  }

  @Override
  public @Nullable Component displayInfo() {
    return Text.format("dialog={0}, node={1}", entryName, nodeName);
  }

  @Override
  public @Nullable BinaryTag save() {
    CompoundTag tag = BinaryTags.compoundTag();
    tag.putString("entry", entryName);

    if (!Strings.isNullOrEmpty(nodeName)) {
      tag.putString("node", nodeName);
    }

    return tag;
  }

  @UsableConstructor(ConstructType.TAG)
  public static ActionDialogue load(BinaryTag nbt) {
    CompoundTag tag = (CompoundTag) nbt;
    String entryName = tag.getString("entry");
    String nodeName;

    if (tag.contains("node")) {
      nodeName = tag.getString("node");
    } else {
      nodeName = "";
    }

    return new ActionDialogue(entryName, nodeName);
  }

  @UsableConstructor(ConstructType.PARSE)
  public static ActionDialogue parse(StringReader reader, CommandSource source)
      throws CommandSyntaxException {
    Holder<Dialogue> holder = PARSER.parse(reader);
    String nodeName;

    reader.skipWhitespace();

    if (reader.canRead()) {
      nodeName = Arguments.FTC_KEY.parse(reader);

      if (Strings.isNullOrEmpty(nodeName)) {
        LOGGER.warn("nodeName empty after canRead() returned true?????");
        throw Exceptions.format("Internal parsing error!");
      }

      var entry = holder.getValue();

      if (entry.getNodeByName(nodeName) == null) {
        throw Exceptions.format("No node named '{0}' found in {1}",
            nodeName, holder.getKey()
        );
      }
    } else if (holder.getValue().getEntryPoint() == null) {
      throw Exceptions.format(
          """
          Dialog '{0}' has no 'entry_node' specified!
          Either specify a node with this input: <dialog file> <node name>
          Or set a "entryPoint":"<node_name>" in the dialog's settings, in the JSON file
          """,
          holder.getKey()
      );
    } else {
      nodeName = "";
    }

    LOGGER.debug("nodeName={}, input='{}'", nodeName, reader.getString());
    return new ActionDialogue(holder.getKey(), nodeName);
  }

  public static CompletableFuture<Suggestions> suggest(
      CommandContext<CommandSource> context,
      SuggestionsBuilder builder
  ) throws CommandSyntaxException {
    String input = builder.getRemainingLowerCase();

    if (Strings.isNullOrEmpty(input) || input.isBlank()) {
      return PARSER.listSuggestions(context, builder);
    }

    StringReader reader = new StringReader(builder.getInput());
    reader.setCursor(builder.getStart());

    Holder<Dialogue> holder;
    try {
      holder = PARSER.parse(reader);
    } catch (CommandSyntaxException exc) {
      return PARSER.listSuggestions(context, builder);
    }

    if (!reader.canRead()) {
      return PARSER.listSuggestions(context, builder);
    }

    reader.skipWhitespace();
    builder = builder.createOffset(reader.getCursor());

    return Completions.suggest(
        builder,
        holder.getValue().getNodeNames()
    );
  }
}