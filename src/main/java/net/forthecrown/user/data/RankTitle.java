package net.forthecrown.user.data;

import com.google.gson.JsonElement;
import lombok.Getter;
import net.forthecrown.text.Text;
import net.forthecrown.utils.JsonSerializable;
import net.forthecrown.utils.io.JsonUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.NotNull;

import static net.forthecrown.user.data.RankTier.*;

/**
 * Rank titles are the predetermined prefixes
 * that all/most users can select from to be
 * displayed in the TAB menu.
 * <p>
 * Each rank has a tier, which broadly states
 * which tier is required to access said title,
 * some titles come default with a tier, for
 * these ranks {@link #isDefaultTitle()} will
 * return true, for the rest, it will return
 * false.
 *
 * @see RankTitle
 * @see UserTitles
 * @see UserTitles#ensureSynced()
 */
@Getter
public enum RankTitle implements JsonSerializable, ComponentLike {
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

    /**
     * Determines if this title comes with
     * the tier or whether the title must
     * be attained separately
     */
    private final boolean defaultTitle;

    /**
     * The title's tier
     */
    private final RankTier tier;

    /**
     * The title's TAB prefix with a trailing space
     * to leave room for the player's name
     */
    private final Component prefix;

    /**
     * The truncated prefix of this title, this
     * has no leading or trailing spaces
     */
    private final Component truncatedPrefix;

    RankTitle(boolean defaultTitle, RankTier tier, String prefix) {
        this.defaultTitle = defaultTitle;
        this.tier = tier;

        this.truncatedPrefix = fromString(prefix);
        this.prefix = fromString(prefix == null ? null : prefix + ' ');

        this.tier.titles.add(this);
    }

    private Component fromString(String prefix) {
        return prefix == null ? null : Text.renderString(prefix);
    }

    @Override
    public JsonElement serialize() {
        return JsonUtils.writeEnum(this);
    }

    @Override
    public @NotNull Component asComponent() {
        return getTruncatedPrefix();
    }
}