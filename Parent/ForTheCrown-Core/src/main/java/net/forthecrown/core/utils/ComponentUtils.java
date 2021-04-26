package net.forthecrown.core.utils;

import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftChatMessage;

/**
 * Utility functions relating to Components, mostly string converters lol
 */
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
        return IChatBaseComponent.ChatSerializer.a(GsonComponentSerializer.gson().serialize(convertString(text, translateColors)));
    }

    public static IChatBaseComponent stringToVanilla(String text){
        return stringToVanilla(text, true);
    }

    public static String getString(Component tex){
        return PaperAdventure.LEGACY_SECTION_UXRC.serialize(tex);
    }

    public static String getString(IChatBaseComponent component){
        return CraftChatMessage.fromComponent(component);
    }

    public static IChatBaseComponent fromJson(String json){
        return IChatBaseComponent.ChatSerializer.a(json);
    }
}