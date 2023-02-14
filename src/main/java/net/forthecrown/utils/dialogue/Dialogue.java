package net.forthecrown.utils.dialogue;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.core.registry.Keys;
import net.forthecrown.user.User;
import net.forthecrown.utils.io.JsonWrapper;

public class Dialogue {
  private final Map<String, DialogueNode> byId
      = new Object2ObjectOpenHashMap<>();

  private final Map<String, DialogueNode> byName
      = new Object2ObjectOpenHashMap<>();

  @Getter @Setter
  private String entryNode;

  @Getter
  private final String disguisedId;

  @Getter
  private DialogueOptions options;

  public Dialogue() {
    this.disguisedId =  DialogueManager.getDialogues().generateDisguisedId();
  }

  public DialogueNode getEntryPoint() {
    return entryNode == null ? null : getNodeByName(entryNode);
  }

  public void run(User user) {
    var node = getEntryPoint();

    if (node == null) {
      Loggers.getLogger().warn("No entry_node found/set, entryNode={}",
          entryNode
      );

      return;
    }

    if (!node.test(user)) {
      return;
    }

    node.view(user);
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

    entry.entryNode = json.getString("entry_node", null);
    json.remove("entry_node");

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