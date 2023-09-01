package net.forthecrown.dialogues.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.arguments.RegistryArguments;
import net.forthecrown.dialogues.Dialogue;
import net.forthecrown.dialogues.DialogueNode;
import net.forthecrown.dialogues.DialoguesPlugin;
import net.forthecrown.dialogues.commands.DialogueArgument.Result;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.registry.Holder;

public class DialogueArgument implements ArgumentType<Result> {

  private static DialogueArgument instance;

  private final RegistryArguments<Dialogue> dialogueParser;

  public static DialogueArgument dialogue() {
    return instance;
  }

  public static void setInstance(DialogueArgument instance) {
    DialogueArgument.instance = instance;
  }

  public DialogueArgument(DialoguesPlugin plugin) {
    var registry = plugin.getManager().getRegistry();
    dialogueParser = new RegistryArguments<>(registry, "Dialogue");
  }

  @Override
  public Result parse(StringReader reader) throws CommandSyntaxException {
    Holder<Dialogue> holder = dialogueParser.parse(reader);
    var dialogue = holder.getValue();

    if (reader.canRead() && reader.peek() == ';') {
      reader.skip();
      String key = Arguments.FTC_KEY.parse(reader);
      DialogueNode node = dialogue.getNodeByName(key);

      if (node == null) {
        throw Exceptions.format("No node named '{0}' inside dialogue '{1}'", key, holder.getKey());
      }

      return new Result(holder.getKey(), key, node);
    }

    var entry = dialogue.getEntryPoint();
    if (entry == null) {
      throw Exceptions.format("Dialogue '{0}' has no entry node", holder.getKey());
    }

    return new Result(holder.getKey(), null, entry);
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  ) {
    String remaining = builder.getRemainingLowerCase();
    var registry = dialogueParser.getRegistry();

    if (!remaining.contains(";")) {
      return suggestKeys(builder);
    }

    String[] split = remaining.split(";", 2);
    String dialogueName = split[0];

    var opt = registry.get(dialogueName);

    if (opt.isEmpty()) {
      return suggestKeys(builder);
    }

    Dialogue dialogue = opt.get();
    int offset = builder.getStart() + dialogueName.length() + 1;

    return Completions.suggest(builder.createOffset(offset), dialogue.getNodeNames());
  }

  private CompletableFuture<Suggestions> suggestKeys(SuggestionsBuilder builder) {
    return dialogueParser.listSuggestions(null, builder);
  }

  public record Result(String dialogue, String nodeName, DialogueNode node) {
  }
}
