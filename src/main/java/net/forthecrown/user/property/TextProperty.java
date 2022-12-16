package net.forthecrown.user.property;

import com.google.gson.JsonElement;
import net.forthecrown.utils.io.JsonUtils;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class TextProperty extends UserProperty<Component> {
    public TextProperty(@NotNull String name, @NotNull Component defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public Component deserialize(JsonElement element) {
        return JsonUtils.readText(element);
    }

    @Override
    public JsonElement serialize(Component component) {
        return JsonUtils.writeText(component);
    }
}