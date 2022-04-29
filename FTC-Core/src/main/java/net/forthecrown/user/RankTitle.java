package net.forthecrown.user;

import com.google.gson.JsonElement;
import lombok.Getter;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import static net.forthecrown.user.RankTier.*;

public enum RankTitle implements JsonSerializable {
    DEFAULT         (true,      NONE,      "default"),

    LEGACY_FREE     (false,     FREE,      "&8[&7Veteran Knight&8]"),
    KNIGHT          (true,      FREE,      "&8[&7Knight&8]"),
    BARON           (false,     FREE,      "&8[&7Baron&8]"),
    BARONESS        (false,     FREE,      "&8[&7Baroness&8]"),
    VIKING          (false,     FREE,      "&8[&7Viking&8]"), // how earn?
    BERSERKER       (false,     FREE,      "&8[&7Berserker&8]"), // how earn?

    LEGACY_TIER_1   (false,     TIER_1,    "&#959595[&6Veteran Lord&#959595]"),
    LORD            (true,      TIER_1,    "&#959595[&6Lord&#959595]"),
    LADY            (true,      TIER_1,    "&#959595[&6Lady&#959595]"),
    SAILOR          (false,     TIER_1,    "&#959595[&6Sailor&#959595]"),
    WARRIOR         (false,     TIER_1,    "&#959595[&6Warrior&#959595]"),
    SHIELD_MAIDEN   (false,     TIER_1,    "&#959595[&6ShieldMaiden&#959595]"),

    LEGACY_TIER_2   (false,     TIER_2,    "&7[&#ffbf15Veteran Duke&7]"),
    DUKE            (true,      TIER_2,    "&7[&#ffbf15Duke&7]"),
    DUCHESS         (true,      TIER_2,    "&7[&#ffbf15Duchess&7]"),
    CAPTAIN         (false,     TIER_2,    "&7[&#ffbf15Captain&7]"),
    ELITE           (false,     TIER_2,    "&7[&#ffbf15Elite&7]"),
    HERSIR          (false,     TIER_2,    "&7[&#ffbf15Hersir&7]"),

    LEGACY_TIER_3   (false,     TIER_3,    "[&#FBFF0FVeteran Prince&f]"),
    PRINCE          (true,      TIER_3,    "[&#FBFF0FPrince&f]"),
    PRINCESS        (true,      TIER_3,    "[&#FBFF0FPrincess&f]"),
    ADMIRAL         (false,     TIER_3,    "[&#FBFF0FAdmiral&f]"),
    ROYAL           (false,     TIER_3,    "[&#FBFF0FRoyal&f]"),
    JARL            (false,     TIER_3,    "[&#FBFF0FJarl&f]"),
    LEGEND          (false,     TIER_3,    "&#dfdfdf[&#fff147Legend&#dfdfdf]");


    @Getter private final boolean defaultTitle;
    @Getter private final RankTier tier;

    private final Component prefix, noEndSpacePrefix;

    RankTitle(boolean defaultTitle, RankTier tier, String prefix) {
        this.defaultTitle = defaultTitle;
        this.tier = tier;

        this.noEndSpacePrefix = fromString(prefix);
        this.prefix = fromString(prefix == null ? null : prefix + ' ');
    }

    private Component fromString(String prefix) {
        return prefix == null ? null : LegacyComponentSerializer.legacyAmpersand().deserialize(prefix);
    }

    public Component prefix() {
        return prefix;
    }

    public Component truncatedPrefix() {
        return noEndSpacePrefix;
    }

    @Override
    public JsonElement serialize() {
        return JsonUtils.writeEnum(this);
    }
}