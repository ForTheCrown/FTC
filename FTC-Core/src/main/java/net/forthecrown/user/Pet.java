package net.forthecrown.user;

import com.google.gson.JsonElement;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Parrot;

/**
 * Represents a pet a pirate user can have
 */
public enum Pet implements JsonSerializable {
    GRAY_PARROT (Parrot.Variant.GRAY, NamedTextColor.GRAY, null, 50000,
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzFiZTcyM2FhMTczOTNkOTlkYWRkYzExOWM5OGIyYzc5YzU0YjM1ZGViZTA1YzcxMzhlZGViOGQwMjU2ZGM0NiJ9fX0="
    ),
    GREEN_PARROT (Parrot.Variant.GREEN, NamedTextColor.GREEN, null, 50000,
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmExZGMzMzExNTIzMmY4MDA4MjVjYWM5ZTNkOWVkMDNmYzE4YWU1NTNjMjViODA1OTUxMzAwMGM1OWUzNTRmZSJ9fX0="
    ),
    BLUE_PARROT (Parrot.Variant.BLUE, NamedTextColor.BLUE, null, 100000,
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjk0YmQzZmNmNGQ0NjM1NGVkZThmZWY3MzEyNmRiY2FiNTJiMzAxYTFjOGMyM2I2Y2RmYzEyZDYxMmI2MWJlYSJ9fX0="
    ),
    RED_PARROT (Parrot.Variant.RED, NamedTextColor.RED, "ftc.donator2", RankTitle.CAPTAIN,
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDBhM2Q0N2Y1NGU3MWE1OGJmOGY1N2M1MjUzZmIyZDIxM2Y0ZjU1YmI3OTM0YTE5MTA0YmZiOTRlZGM3NmVhYSJ9fX0="
    ),
    AQUA_PARROT (Parrot.Variant.CYAN, NamedTextColor.AQUA, "ftc.donator3", RankTitle.ADMIRAL,
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzI2OGNlMzdiZTg1MDdlZDY3ZTNkNDBiNjE3ZTJkNzJmNjZmOWQyMGIxMDZlZmIwOGU2YmEwNDFmOWI5ZWYxMCJ9fX0="
    );

    private final Parrot.Variant variant;
    private final Component name;
    private final String permission;
    private final String texture;

    private final RankTitle required;
    private final int requiredBal;

    Pet(Parrot.Variant variant, TextColor color, String permission, RankTitle required, String texture){
        this.variant = variant;
        this.name = Component.text(FtcFormatter.normalEnum(this)).color(color);
        this.permission = permission;
        this.texture = texture;

        this.required = required;
        this.requiredBal = -1;
    }

    Pet(Parrot.Variant variant, TextColor color, String permission, int required, String texture){
        this.variant = variant;
        this.name = Component.text(FtcFormatter.normalEnum(this)).color(color);
        this.permission = permission;
        this.texture = texture;

        this.required = null;
        this.requiredBal = required;
    }

    public Parrot.Variant getVariant() {
        return variant;
    }

    public Component getName() {
        return name;
    }

    public String getTexture() {
        return texture;
    }

    public String getPermission() {
        return permission;
    }

    public boolean testPermission(CommandSender sender){
        if(permission == null) return true;
        return sender.hasPermission(permission);
    }

    public Component requirementDisplay(){
        Component needed = requiresRank() ? required.truncatedPrefix() : FtcFormatter.rhinesNonTrans(requiredBal);

        return Component.text()
                .color(NamedTextColor.YELLOW)
                .append(Component.text("Value: "))
                .append(needed)
                .build();
    }

    public boolean requiresRank() {
        return required != null;
    }

    public int getRequiredBal() {
        return requiredBal;
    }

    public RankTitle getRequiredRank() {
        return required;
    }

    @Override
    public JsonElement serialize() {
        return JsonUtils.writeEnum(this);
    }
}
