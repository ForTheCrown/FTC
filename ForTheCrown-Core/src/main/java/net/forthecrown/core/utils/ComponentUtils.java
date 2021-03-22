package net.forthecrown.core.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.server.v1_16_R3.ChatComponentText;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftChatMessage;
import org.jetbrains.annotations.Nullable;

public final class ComponentUtils {

    public static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder()
            .extractUrls()
            .hexColors()
            .build();

    public static TextComponent convertString(String text, boolean translateColors){
        return SERIALIZER.deserialize(translateColors ? CrownUtils.translateHexCodes(text) : text);
    }

    public static TextComponent convertString(String text){
        return convertString(text, true);
    }

    public static IChatBaseComponent stringToVanilla(String text, boolean translateColors){
        IChatBaseComponent[] texts = CraftChatMessage.fromString(translateColors ? CrownUtils.translateHexCodes(text) : text);
        ChatComponentText merged = new ChatComponentText("");
        for (IChatBaseComponent c: texts) {
            merged.addSibling(c);
        }
        return merged;
    }

    public static IChatBaseComponent stringToVanilla(String text){
        return stringToVanilla(text, true);
    }

    public static TextComponent makeComponent(String text, @Nullable TextColor color, @Nullable ClickEvent click, @Nullable HoverEvent hover){
        TextComponent component = Component.text(text);
        if(color != null) component = component.color(color);
        if(click != null) component = component.clickEvent(click);
        if(hover != null) component = component.hoverEvent(hover);
        return component;
    }

    public static String getString(Component tex){
        return SERIALIZER.serialize(tex);
    }

    public static String getString(IChatBaseComponent component){
        return CraftChatMessage.fromComponent(component);
    }
}
