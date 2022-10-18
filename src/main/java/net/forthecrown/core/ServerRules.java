package net.forthecrown.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import lombok.Getter;
import net.forthecrown.utils.io.JsonUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the server's rules and displays them
 */
public class ServerRules extends FtcConfig.Section {
    @Getter
    private final List<Component> rules = new ArrayList<>();

    public ServerRules(){
        super("rules");

        Crown.logger().info("Server rules loaded");
    }

    //Displays the rules
    public Component display() {
        Component border = Component.text("          ").style(Style.style(NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));
        TextComponent.Builder builder = Component.text()
                .append(border)
                .append(Component.text(" Rules "))
                .append(border);

        for (int index = 0; index < rules.size(); index++){
            builder
                    .append(Component.newline())
                    .append(
                            Component.text("[")
                                    .color(NamedTextColor.RED)
                                    .append(Component.text(index + 1))
                                    .append(Component.text("] "))
                    )
                    .append(rules.get(index))
                    .append(Component.text("."));
        }

        return builder.build();
    }

    @Override
    public void deserialize(JsonElement element) {
        rules.clear();

        JsonArray array = element.getAsJsonArray();

        for (JsonElement e: array) {
            rules.add(JsonUtils.readText(e));
        }
    }

    @Override
    public JsonElement serialize() {
        JsonArray array = new JsonArray();

        for (Component c: rules) {
            array.add(JsonUtils.writeText(c));
        }

        return array;
    }
}