package net.forthecrown.core.utils;

import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.server.v1_16_R3.ChatComponentText;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import net.minecraft.server.v1_16_R3.IChatMutableComponent;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftChatMessage;

public final class ComponentUtils {

    public static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder()
            .extractUrls()
            .hexColors()
            .build();

    public static TextComponent convertString(String text, boolean translateColors){
        return Component.text()
                .append(SERIALIZER.deserialize(translateColors ? CrownUtils.translateHexCodes(text) : text))
                .color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                .build();
    }

    public static TextComponent convertString(String text){
        return convertString(text, true);
    }

    public static IChatBaseComponent stringToVanilla(String text, boolean translateColors){
        IChatBaseComponent[] texts = CraftChatMessage.fromString(translateColors ? CrownUtils.translateHexCodes(text) : text);
        IChatMutableComponent merged = new ChatComponentText("");
        for (IChatBaseComponent c: texts) {
            merged.addSibling(c);
        }
        return merged;
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
