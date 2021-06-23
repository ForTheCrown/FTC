package net.forthecrown.user.enums;

import com.google.gson.JsonElement;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.core.chat.ChatFormatter;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Parrot;

public enum Pet implements JsonSerializable {
    GRAY_PARROT (Parrot.Variant.GRAY, NamedTextColor.GRAY, null),
    GREEN_PARROT (Parrot.Variant.GREEN, NamedTextColor.GREEN, null),
    BLUE_PARROT (Parrot.Variant.BLUE, NamedTextColor.BLUE, null),
    RED_PARROT (Parrot.Variant.RED, NamedTextColor.RED, "ftc.donator2"),
    AQUA_PARROT (Parrot.Variant.CYAN, NamedTextColor.AQUA, "ftc.donator3");

    private final Parrot.Variant variant;
    private final Component name;
    private final String permission;

    Pet(Parrot.Variant variant, TextColor color, String permission){
        this.variant = variant;
        this.name = Component.text(ChatFormatter.normalEnum(this)).color(color);
        this.permission = permission;
    }

    public Parrot.Variant getVariant() {
        return variant;
    }

    public Component getName() {
        return name;
    }

    public String getPermission() {
        return permission;
    }

    public boolean testPermission(CommandSender sender){
        if(permission == null) return true;
        return sender.hasPermission(permission);
    }

    @Override
    public JsonElement serialize() {
        return JsonUtils.serializeEnum(this);
    }
}
