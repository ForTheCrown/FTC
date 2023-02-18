package net.forthecrown.utils.dialogue;

import com.google.common.base.Strings;
import com.mojang.brigadier.StringReader;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.core.registry.Keys;
import net.forthecrown.user.User;
import net.forthecrown.utils.io.JsonWrapper;
import org.jetbrains.annotations.Nullable;

public class Dialogue {

  private final Map<String, DialogueNode> byId
      = new Object2ObjectOpenHashMap<>();

  private final Map<String, DialogueNode> byName
      = new Object2ObjectOpenHashMap<>();

  @Getter
  private final String disguisedId;

  @Getter
  private DialogueOptions options;

  public Dialogue() {
    this.disguisedId =  DialogueManager.getDialogues().generateDisguisedId();
  }

  public DialogueNode getEntryPoint() {
    return options.getEntryPoint() != null
        ? getNodeByName(options.getEntryPoint())
        : null;
  }

  public Optional<String> run(User user, @Nullable String nodeName) {
    DialogueNode node;

    if (Strings.isNullOrEmpty(nodeName)) {
      node = getEntryPoint();

      if (node == null) {
        return Optional.of("No 'entry_node' set as entry point");
      }
    } else {
      node = getNodeByName(nodeName);

      if (node == null) {
        return Optional.of("No node named: '" + nodeName + "'");
      }
    }

    node.run(user);
    return Optional.empty();
  }

  public Optional<String> run(User user, StringReader reader) {
    if (!reader.canRead()) {
      return run(user, "");
    }

    var word = reader.readUnquotedString();
    var node = getNodeByRandomId(word);

    if (node == null) {
      return Optional.of(
          String.format("No node named '%s' found, input='%s'",
              word, reader.getString()
          )
      );
    }

    node.run(user);
    return Optional.empty();
  }

  public void addEntry(String name, DialogueNode node) {
    byId.put(node.getDisguisedId(), node );
    byName.put(name, node);
    node.entry = this;
  }

  public DialogueNode getNodeByRandomId(String randomId) {
    return byId.get(randomId);
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

      if (!Keys.isValidKey(key)) {
        Loggers.getLogger().error("Invalid key '{}', must follow pattern {}",
            key, Keys.VALID_KEY_REGEX
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