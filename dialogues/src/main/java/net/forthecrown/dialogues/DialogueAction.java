package net.forthecrown.dialogues;

import com.google.common.base.Strings;
import net.forthecrown.Loggers;
import net.forthecrown.dialogues.commands.DialogueArgument;
import net.forthecrown.dialogues.commands.DialogueArgument.Result;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.registry.Registry;
import net.forthecrown.usables.Action;
import net.forthecrown.usables.BuiltType;
import net.forthecrown.usables.Interaction;
import net.forthecrown.usables.UsableComponent;
import net.forthecrown.usables.ObjectType;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

public class DialogueAction implements Action {

  public static final Logger LOGGER = Loggers.getLogger();

  public static final ObjectType<DialogueAction> TYPE = BuiltType.<DialogueAction>builder()
      .parser((reader, source) -> {
        Result result = DialogueArgument.dialogue().parse(reader);
        return new DialogueAction(result.dialogue(), result.nodeName());
      })

      .suggester((context, builder) -> {
        return DialogueArgument.dialogue().listSuggestions(context, builder);
      })

      .tagLoader(binaryTag -> {
        CompoundTag tag = binaryTag.asCompound();
        String dialogue = tag.getString("entry");
        String node = tag.getString("node", null);
        return new DialogueAction(dialogue, node);
      })

      .tagSaver(act -> {
        CompoundTag tag = BinaryTags.compoundTag();
        tag.putString("entry", act.dialogue);

        if (!Strings.isNullOrEmpty(act.nodeName)) {
          tag.putString("node", act.nodeName);
        }

        return tag;
      })

      .build();

  private final String dialogue;
  private final String nodeName;

  public DialogueAction(String dialogue, String nodeName) {
    this.dialogue = dialogue;
    this.nodeName = nodeName;
  }

  @Override
  public void onUse(Interaction interaction) {
    DialoguesPlugin plugin = JavaPlugin.getPlugin(DialoguesPlugin.class);
    Registry<Dialogue> registry = plugin.getManager().getRegistry();

    var dialogueOpt = registry.get(dialogue);

    if (dialogueOpt.isEmpty()) {
      LOGGER.warn("Unknown dialogue '{}'", dialogue);
      return;
    }

    Dialogue dia = dialogueOpt.get();
    DialogueNode node;

    if (Strings.isNullOrEmpty(nodeName)) {
      node = dia.getEntryPoint();

      if (node == null) {
        LOGGER.warn("Dialogue '{}' has no entry point node", dialogue);
        return;
      }
    } else {
      node = dia.getNodeByName(nodeName);

      if (node == null) {
        LOGGER.warn("Dialogue '{}' has no node named '{}'", dialogue, nodeName);
        return;
      }
    }

    node.accept(interaction.player());
  }

  @Override
  public ObjectType<? extends UsableComponent> getType() {
    return TYPE;
  }
}
