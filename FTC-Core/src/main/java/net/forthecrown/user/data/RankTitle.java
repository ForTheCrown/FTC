package net.forthecrown.user.data;

import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Collection;
import java.util.List;

import static net.forthecrown.user.data.RankTier.*;

public enum RankTitle implements JsonSerializable {
    DEFAULT         (true,      NONE,      null),

    //Legacy ranks
    LEGACY_FREE     (false,     FREE,      ""),
    LEGACY_TIER_1   (false,     TIER_1,    ""),
    LEGACY_TIER_2   (false,     TIER_2,    ""),
    LEGACY_TIER_3   (false,     TIER_3,    ""),

    KNIGHT          (true,      FREE,      "&8[&7Knight&8]"),
    BARON           (true,      FREE,      "&8[&7Baron&8]"),
    BARONESS        (true,      FREE,      "&8[&7Baroness&8]"),
    SAILOR          (false,     FREE,      "&8&l{&7Sailor&8&l}"),
    VIKING          (false,     FREE,      ""),
    BERSERKER       (false,     FREE,      ""),

    LORD            (true,      TIER_1,    "&#959595[&6Lord&#959595]"),
    LADY            (true,      TIER_1,    "&#959595[&6Lady&#959595]"),
    PIRATE          (false,     TIER_1,    "&8&l{&7Pirate&8&l}"),
    WARRIOR         (false,     TIER_1,    ""),
    SHIELD_MAIDEN   (false,     TIER_1,    ""),

    DUKE            (true,      TIER_2,    "&7[&#ffbf15Duke&7]"),
    DUCHESS         (true,      TIER_2,    "&7[&#ffbf15Duchess&7]"),
    CAPTAIN         (false,     TIER_2,    "&7{&6Captain&7}"),
    ELITE           (false,     TIER_2,    ""),
    HERSIR          (false,     TIER_2,    ""),

    PRINCE          (true,      TIER_3,    "[&#FBFF0FPrince&f]"),
    PRINCESS        (true,      TIER_3,    "[&#FBFF0FPrincess&f]"),
    ADMIRAL         (false,     TIER_3,    "{&eAdmiral&f}"),
    ROYAL           (false,     TIER_3,    ""),
    JARL            (false,     TIER_3,    ""),
    LEGEND          (false,     TIER_3,    "&#dfdfdf[&#fff147Legend&#dfdfdf]");

    private final boolean defaultTitle;
    private final RankTier tier;
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

    public RankTier getTier() {
        return tier;
    }

    public boolean isDefaultTitle() {
        return defaultTitle;
    }

    public Component prefix() {
        return prefix;
    }

    public Component noEndSpacePrefix() {
        return noEndSpacePrefix;
    }

    @Override
    public JsonElement serialize() {
        return JsonUtils.writeEnum(this);
    }

    public static Collection<RankTitle> getDefaultsFor(RankTier tier) {
        List<RankTitle> titles = new ObjectArrayList<>();

        for (RankTitle t: values()) {
            if(t.isDefaultTitle() && tier == t.getTier()) titles.add(t);
        }

        return titles;
    }
}
