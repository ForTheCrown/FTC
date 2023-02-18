package net.forthecrown.utils.dialogue;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.core.script2.Script;
import net.forthecrown.core.script2.ScriptSource;
import net.forthecrown.user.User;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.text.TextJoiner;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.apache.commons.lang3.ArrayUtils;

@Getter
public class DialogueNode implements Predicate<User> {
  private static final Predicate<User> DEFAULT_CONDITION = user -> true;

  private final String disguisedId;

  private final Predicate<User> condition;
  private final Consumer<User> onView;

  private final ImmutableList<Component> content;

  Dialogue entry;

  private DialogueNode(Builder builder) {
    this.condition = Objects.requireNonNullElse(
        builder.condition,
        DEFAULT_CONDITION
    );

    this.onView = builder.onView;
    this.content = builder.content.build();

    this.disguisedId =  DialogueManager.getDialogues().generateDisguisedId();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static DialogueNode deserialize(JsonWrapper json) {
    var builder = builder();
    JsonElement condition = json.get("condition");
    builder.condition(loadCondition(condition));

    var onView = json.get("on_view");
    if (onView != null) {
      var viewScriptSupplier = loadScript(onView, false);

      builder.onView(user -> {
        try (var viewScript = viewScriptSupplier.get()) {
          viewScript.put("reader", user);
          viewScript.eval();
        }
      });
    }

    if (json.has("text")) {
      JsonArray text = json.getArray("text");
      text.forEach(element -> {
        Component c = JsonUtils.readText(element);
        builder.content.add(c);
      });
    }

    return builder.build();
  }

  private static Supplier<Script> loadScript(JsonElement element,
                                             boolean assumeRawJs
  ) {
    ScriptSource source = ScriptSource.readSource(element, assumeRawJs);
    String[] args;

    if (element.isJsonObject()) {
      var obj = element.getAsJsonObject();
      var jsonArgs = obj.get("args");

      if (jsonArgs != null) {
        if (jsonArgs.isJsonPrimitive()) {
          args = jsonArgs.getAsString().split(" ");
        } else {
          args = JsonUtils.stream(jsonArgs.getAsJsonArray())
              .map(JsonElement::getAsString)
              .toArray(String[]::new);
        }
      } else {
        args = ArrayUtils.EMPTY_STRING_ARRAY;
      }
    } else {
      args = ArrayUtils.EMPTY_STRING_ARRAY;
    }

    return () -> {
      Script script = Script.of(source);
      script.compile(args);
      return script;
    };
  }

  private static Predicate<User> loadCondition(JsonElement condition) {
    if (condition == null) {
      return DEFAULT_CONDITION;
    }

    if (condition.isJsonPrimitive()
        && condition.getAsJsonPrimitive().isBoolean()
    ) {
      final boolean constValue = condition.getAsBoolean();
      return user -> constValue;
    }

    var supplier = loadScript(condition, true);

    return user -> {
      Script script = supplier.get();

      script.put("reader", user);
      var result = script.eval();
      script.close();

      return result.asBoolean()
          .orElseThrow();
    };
  }

  @Override
  public boolean test(User user) {
    return condition.test(user);
  }

  public Component createButton(Component text,
                                User user,
                                DialogueOptions options
  ) {
    boolean allowed = test(user);
    return DialogueRenderer.buttonize(
        text,
        allowed,
        ClickEvent.runCommand(getCommandString()),
        options
    );
  }

  public String getCommandString() {
    Objects.requireNonNull(entry, "Not bound to an entry");

    return String.format("/%s %s %s",
        DialogueManager.COMMAND_NAME,
        entry.getDisguisedId(),
        getDisguisedId()
    );
  }

  public void run(User user) {
    if (!test(user)) {
      return;
    }

    view(user);
  }

  public void view(User user) {
    if (onView != null) {
      onView.accept(user);
    }

    if (content.isEmpty()) {
      return;
    }

    DialogueRenderer renderer = new DialogueRenderer(user, entry);
    Component text = render(renderer);
    user.sendMessage(text);
  }

  public Component render(DialogueRenderer renderer) {
    return TextJoiner.onNewLine()
        .add(content.stream().map(renderer::render))
        .asComponent();
  }

  @Getter
  @Setter
  @Accessors(chain = true, fluent = true)
  public static class Builder {
    private Predicate<User> condition;
    private Consumer<User> onView;

    private final ImmutableList.Builder<Component> content
        = ImmutableList.builder();

    public DialogueNode build() {
      return new DialogueNode(this);
    }
  }
}