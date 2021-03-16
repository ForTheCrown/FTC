package net.forthecrown.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import org.jetbrains.annotations.Nullable;

public class ComponentUtils {

    public static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder()
            .extractUrls()
            .hexColors()
            .build();

    public static TextComponent convertString(String text){
        TextComponent textComponent = SERIALIZER.deserialize(CrownUtils.translateHexCodes(text));
        return Component.text("")
                .color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                .append(textComponent);
    }

    public static TextComponent makeComponent(String text, @Nullable TextColor color, @Nullable ClickEvent click, @Nullable HoverEvent hover){
        TextComponent component = Component.text(text);
        if(color != null) component = component.color(color);
        if(click != null) component = component.clickEvent(click);
        if(hover != null) component = component.hoverEvent(hover);
        return component;
    }

    public static String getString(Component tex){
        IChatBaseComponent base = IChatBaseComponent.ChatSerializer.a(GsonComponentSerializer.gson().serialize(tex));
        return base.getString();
    }
}
