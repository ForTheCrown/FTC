package net.forthecrown.dialogues;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.time.Duration;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.scripts.ExecResults;
import net.forthecrown.scripts.Script;
import net.forthecrown.scripts.Scripts;
import net.forthecrown.text.TextJoiner;
import net.forthecrown.text.UserClickCallback;
import net.forthecrown.user.User;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.source.Source;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.apache.commons.lang3.ArrayUtils;

@Getter
public class DialogueNode implements Predicate<User>, UserClickCallback {
  private static final Predicate<User> DEFAULT_CONDITION = user -> true;

  private final Predicate<User> condition;
  private final Consumer<User> onView;

  private final ImmutableList<Component> content;

  Dialogue entry;

  ClickEvent clickEvent;

  private DialogueNode(Builder builder) {
    this.condition = Objects.requireNonNullElse(
        builder.condition,
        DEFAULT_CONDITION
    );

    this.onView = builder.onView;
    this.content = builder.content.build();
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
          viewScript.evaluate();
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

  private static Supplier<Script> loadScript(JsonElement element, boolean assumeRawJs) {
    Source source = Scripts.loadScriptSource(element, assumeRawJs);
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
      Script script = Scripts.newScript(source);
      script.setArguments(args);
      script.compile();
      return script;
    };
  }

  private static Predicate<User> loadCondition(JsonElement condition) {
    if (condition == null) {
      return DEFAULT_CONDITION;
    }

    if (condition.isJsonPrimitive() && condition.getAsJsonPrimitive().isBoolean()) {
      final boolean constValue = condition.getAsBoolean();
      return user -> constValue;
    }

    var supplier = loadScript(condition, true);

    return user -> {
      Script script = supplier.get();

      script.put("reader", user);
      var result = script.evaluate();

      if (!result.isSuccess()) {
        script.close();
        return false;
      }

      script.close();
      return ExecResults.toBoolean(result).result().orElse(false);
    };
  }

  @Override
  public boolean test(User user) {
    return condition.test(user);
  }

  public ClickEvent getClickEvent() {
    if (clickEvent != null) {
      return clickEvent;
    }

    clickEvent = ClickEvent.callback(
        this,
        builder -> builder.uses(-1).lifetime(Duration.ofDays(365))
    );

    return clickEvent;
  }

  public Component createButton(Component text, User user, DialogueOptions options) {
    boolean allowed = test(user);
    return DialogueRenderer.buttonize(text, allowed, getClickEvent(), options);
  }

  @Override
  public void accept(User user) {
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