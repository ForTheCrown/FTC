package net.forthecrown.utils.io.configurate;

import static net.kyori.adventure.text.Component.text;

import io.leangen.geantyref.TypeToken;
import io.papermc.paper.adventure.providers.GsonComponentSerializerProviderImpl;
import java.lang.reflect.Type;
import net.forthecrown.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecorationAndState;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

public class ComponentSerializerType implements TypeSerializer<Component> {

  @Override
  public Component deserialize(Type type, ConfigurationNode node) throws SerializationException {
    String str = node.getString();
    if (str == null) {
      throw new SerializationException("Cannot deserialize text");
    }

    if (str.startsWith("[") || str.startsWith("{")) {
      return GsonComponentSerializer.gson().deserialize(str);
    }

    return Text.valueOf(str);
  }

  // Julie's Note: I stopped giving a shi-
/*
  private Component deserialize(ConfigurationNode node) throws SerializationException {
    if (node.isNull()) {
      return null;
    }

    if (node.isList()) {
      var list = node.childrenList();
      var builder = text();
      for (ConfigurationNode node1 : list) {
        var nodeText = deserialize(node1);
        builder.append(nodeText);
      }

      return builder.build();
    }

    if (node.isMap()) {
      Component baseComponent;

      if (node.hasChild("text")) {

      } else if (node.hasChild("translation")) {

      } else if (node.hasChild("")) {

      } else {
        throw new SerializationException()
      }

      Style style = loadStyle(node);
    }

    String strValue = node.getString();
    if (strValue != null) {
      return Text.valueOf(strValue);
    }


  }

  Style loadStyle(ConfigurationNode node) throws SerializationException {
    var builder = Style.style();

    if (node.hasChild("color")) {
      var colorNode = node.node("color");
      builder.color(loadColor(colorNode));
    }

    TextDecoration[] values = TextDecoration.values();

    for (TextDecoration value : values) {
      String key = value.name().toLowerCase();

      if (!node.hasChild(key)) {
        continue;
      }

      var decoNode = node.node(key);
      boolean state = decoNode.getBoolean();

      builder.decoration(value, state);
    }

    if (node.hasChild("clickEvent")) {

    }

    if (node.hasChild("hoverEvent")) {

    }
  }

  private HoverEvent<?> loadHover(ConfigurationNode node) throws SerializationException {

  }

  private

  private TextColor loadColor(ConfigurationNode colorNode) throws SerializationException {
    String str = colorNode.getString();

    TextColor color;

    if (str != null) {
      if (str.startsWith("0x")) {
        return TextColor.fromHexString("#" + str.substring(2));
      }

      if (str.startsWith("#")) {
        return TextColor.fromHexString(str);
      }

      color = NamedTextColor.NAMES.value(str);
      if (color == null) {
        throw new SerializationException("Unknown color value: " + str);
      }
      return color;
    }

    int intValue = colorNode.getInt(Integer.MAX_VALUE);

    if (intValue == Integer.MAX_VALUE) {
      throw new SerializationException("Don't know how to deserialize color: " + node);
    }

    return TextColor.color(intValue);
  }*/

  @Override
  public void serialize(Type type, @Nullable Component obj, ConfigurationNode node)
      throws SerializationException
  {
    if (obj == null) {
      node.set(null);
      return;
    }

    String str = GsonComponentSerializer.gson().serialize(obj);
    node.set(str);
  }
}
