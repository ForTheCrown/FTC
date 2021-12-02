package net.forthecrown.core.admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.forthecrown.core.Crown;
import net.forthecrown.serializer.AbstractJsonSerializer;
import net.forthecrown.serializer.JsonWrapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import java.util.ArrayList;
import java.util.List;

public class ServerRules extends AbstractJsonSerializer {

    private final List<Component> rules = new ArrayList<>();
    private final GsonComponentSerializer serializer = GsonComponentSerializer.gson();

    public ServerRules(){
        super("rules");

        if(!fileExists) reload();
        reload();
        Crown.logger().info("Server rules loaded");
    }

    //Displays the rules
    public Component display(){
        Component border = Component.text(" -------- ").style(Style.style(NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));
        TextComponent.Builder builder = Component.text()
                .append(border)
                .append(Component.text("Rules"))
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
    protected void save(JsonWrapper json) {
        JsonArray array = new JsonArray();

        for (Component c: rules){
            array.add(serializer.serializeToTree(c));
        }

        json.add("rules", array);
    }

    @Override
    protected void reload(JsonWrapper json) {
        JsonArray array = json.getArray("rules");

        rules.clear();
        for (JsonElement e: array){
            rules.add(serializer.deserializeFromTree(e));
        }
    }

    @Override
    protected void createDefaults(JsonWrapper json) {
        JsonArray array = new JsonArray();

        array.add(ser(Component.text("No hacking or using xray")));
        array.add(ser(Component.text("Be respectful to other players")));
        array.add(ser(Component.text("No spamming or advertising")));
        array.add(ser(Component.text("No unwanted PvP")));
        array.add(ser(Component.text("No impersonating other players")));
        array.add(ser(Component.text("No auto-clickers")));
        array.add(ser(Component.text("No gameplay altering mods, which give you an unfair advantage")));
        array.add(ser(Component.text("Only play on 1 account at a time")));
        array.add(ser(Component.text("No lag machines")));

        json.add("rules", array);
    }

    private JsonElement ser(Component component){
        return serializer.serializeToTree(component);
    }
}
