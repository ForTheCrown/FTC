package net.forthecrown.dialogues;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import net.forthecrown.Loggers;
import net.forthecrown.registry.Registries;
import net.forthecrown.utils.io.JsonWrapper;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.event.ClickCallback;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class Dialogue implements ClickCallback<Audience> {

  public static final Logger LOGGER = Loggers.getLogger();

  private final Map<String, DialogueNode> byName
      = new Object2ObjectOpenHashMap<>();

  @Getter
  private DialogueOptions options;

  public DialogueNode getEntryPoint() {
    return options.getEntryPoint() != null
        ? getNodeByName(options.getEntryPoint())
        : null;
  }

  @Override
  public void accept(@NotNull Audience audience) {
    DialogueNode node = getEntryPoint();

    if (node == null) {
      LOGGER.error("No 'entry_node' set as entry point");
      return;
    }

    node.accept(audience);
  }

  public void addEntry(String name, DialogueNode node) {
    byName.put(name, node);
    node.entry = this;
  }

  public DialogueNode getNodeByName(String name) {
    return byName.get(name);
  }

  public Set<String> getNodeNames() {
    return byName.keySet();
  }

  public static Dialogue deserialize(JsonWrapper json) {
    Dialogue entry = new Dialogue();

    if (json.has("settings")) {
      entry.options = DialogueOptions.load(json.get("settings"));
      json.remove("settings");
    } else {
      entry.options = DialogueOptions.defaultOptions();
    }

    for (var e: json.entrySet()) {
      String key = e.getKey();

      if (!Registries.isValidKey(key)) {
        Loggers.getLogger().error("Invalid key '{}', must follow pattern {}",
            key, Registries.VALID_KEY_REGEX
        );

        continue;
      }

      var node = DialogueNode.deserialize(
          JsonWrapper.wrap(e.getValue().getAsJsonObject())
      );

      entry.addEntry(key, node);
    }

    return entry;
  }
}