package net.forthecrown.usables.actions;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.DataResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.text.Text;
import net.forthecrown.text.placeholder.PlaceholderRenderer;
import net.forthecrown.text.placeholder.Placeholders;
import net.forthecrown.usables.Action;
import net.forthecrown.usables.BuiltType;
import net.forthecrown.usables.Interaction;
import net.forthecrown.usables.UsableComponent;
import net.forthecrown.usables.ObjectType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

@Getter
@RequiredArgsConstructor
public class TextAction implements Action {

  public static final ObjectType<TextAction> TYPE = BuiltType.<TextAction>builder()
      .parser((reader, source) -> {
        var remain = reader.getRemaining();
        reader.setCursor(reader.getTotalLength());
        return new TextAction(remain);
      })
      .saver((value, ops) -> DataResult.success(ops.createString(value.getText())))
      .loader(dynamic -> dynamic.asString().map(TextAction::new))
      .build();

  private final String text;

  private Component getBaseText(Audience viewer) {
    try {
      StringReader reader = new StringReader(text);
      return Arguments.CHAT.parse(reader).create(viewer);
    } catch (CommandSyntaxException exc) {
      return Text.valueOf(text, viewer);
    }
  }

  @Override
  public void onUse(Interaction interaction) {
    var player = interaction.player();

    PlaceholderRenderer list = Placeholders.newRenderer().useDefaults();

    Component base = getBaseText(player);
    Component rendered = list.render(base, player, interaction.context());

    player.sendMessage(rendered);
  }

  @Override
  public ObjectType<? extends UsableComponent> getType() {
    return TYPE;
  }

  @Override
  public @Nullable Component displayInfo() {
    return getBaseText(null);
  }
}
