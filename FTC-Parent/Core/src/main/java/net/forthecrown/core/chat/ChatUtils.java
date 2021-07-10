package net.forthecrown.core.chat;

import com.google.gson.JsonElement;
import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftChatMessage;

/**
 * Utility functions relating to Components, mostly string converters lol
 */
public final class ChatUtils {

    public static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder()
            .extractUrls()
            .hexColors()
            .build();

    public static final GsonComponentSerializer GSON = GsonComponentSerializer.gson();
    public static final PlainTextComponentSerializer PLAIN_SERIALIZER = PlainTextComponentSerializer.plainText();

    public static TextComponent convertString(String text, boolean translateColors){
        return SERIALIZER.deserialize(translateColors ? ChatFormatter.translateHexCodes(text) : text);
    }

    public static TextComponent convertString(String text){
        return convertString(text, true);
    }

    public static net.minecraft.network.chat.Component stringToVanilla(String text, boolean translateColors){
        return net.minecraft.network.chat.Component.Serializer.fromJson(GsonComponentSerializer.gson().serialize(convertString(text, translateColors)));
    }

    public static net.minecraft.network.chat.Component stringToVanilla(String text){
        return stringToVanilla(text, true);
    }

    public static net.minecraft.network.chat.Component adventureToVanilla(Component text){
        return net.minecraft.network.chat.Component.Serializer.fromJson(GsonComponentSerializer.gson().serialize(text));
    }

    public static String getString(Component tex) {
        return PaperAdventure.LEGACY_SECTION_UXRC.serialize(tex);
    }

    public static String plainText(Component text){
        return PLAIN_SERIALIZER.serialize(text);
    }

    public static String getString(net.minecraft.network.chat.Component component){
        return CraftChatMessage.fromComponent(component);
    }

    public static net.minecraft.network.chat.Component fromJson(String json){
        return net.minecraft.network.chat.Component.Serializer.fromJson(json);
    }

    public static JsonElement toJson(Component component){
        return GSON.serializeToTree(component);
    }

    public static Component fromJson(JsonElement element){
        return GSON.deserializeFromTree(element);
    }

    public static Component vanillaToAdventure(net.minecraft.network.chat.Component component){
        return GsonComponentSerializer.gson().deserialize(net.minecraft.network.chat.Component.Serializer.toJson(component));
    }
}