package net.forthecrown.utils.dialogue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

@Getter
public class DialogueOptions {

  public static final TextColor BUTTON_AVAILABLE = NamedTextColor.AQUA;
  public static final TextColor BUTTON_UNAVAILABLE = NamedTextColor.GRAY;

  private static final Gson GSON;

  static {
    var builder =  new GsonBuilder()
        .setPrettyPrinting();

    GsonComponentSerializer.gson()
        .populator()
        .apply(builder);

    GSON = builder.create();
  }

  private final TextColor buttonAvailableColor;
  private final TextColor buttonUnavailableColor;
  private final String entryPoint;

  private DialogueOptions(Builder builder) {
    this.buttonAvailableColor = Objects.requireNonNullElse(
        builder.buttonAvailableColor,
        BUTTON_AVAILABLE
    );

    this.buttonUnavailableColor = Objects.requireNonNullElse(
        builder.buttonUnavailableColor,
        BUTTON_UNAVAILABLE
    );

    this.entryPoint = builder.entryPoint;
  }

  public static DialogueOptions load(JsonElement element) {
    return GSON.fromJson(element, Builder.class).build();
  }

  public static DialogueOptions defaultOptions() {
    return builder().build();
  }

  public static Builder builder() {
    return new Builder();
  }

  @Getter
  @Setter
  @Accessors(fluent = true, chain = true)
  public static class Builder {
    TextColor buttonAvailableColor;
    TextColor buttonUnavailableColor;

    String entryPoint;

    public DialogueOptions build() {
      return new DialogueOptions(this);
    }
  }
}