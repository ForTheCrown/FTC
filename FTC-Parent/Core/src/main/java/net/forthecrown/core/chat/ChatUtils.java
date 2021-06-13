package net.forthecrown.core.chat;

import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftChatMessage;

/**
 * Utility functions relating to Components, mostly string converters lol
 */
public final class ChatUtils {

    public static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder()
            .extractUrls()
            .hexColors()
            .build();

    public static final PlainComponentSerializer PLAIN_SERIALIZER = PlainComponentSerializer.plain();

    public static TextComponent convertString(String text, boolean translateColors){
        return SERIALIZER.deserialize(translateColors ? ChatFormatter.translateHexCodes(text) : text);
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

    public static IChatBaseComponent adventureToVanilla(Component text){
        return IChatBaseComponent.ChatSerializer.a(GsonComponentSerializer.gson().serialize(text));
    }

    public static String getString(Component tex){
        return PaperAdventure.LEGACY_SECTION_UXRC.serialize(tex);
    }

    public static String getPlainString(Component text){
        return PLAIN_SERIALIZER.serialize(text);
    }

    public static String getString(IChatBaseComponent component){
        return CraftChatMessage.fromComponent(component);
    }

    public static IChatBaseComponent fromJson(String json){
        return IChatBaseComponent.ChatSerializer.a(json);
    }

    public static Component vanillaToAdventure(IChatBaseComponent component){
        return GsonComponentSerializer.gson().deserialize(IChatBaseComponent.ChatSerializer.a(component));
    }
}