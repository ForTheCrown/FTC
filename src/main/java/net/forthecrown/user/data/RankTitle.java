package net.forthecrown.user.data;

import com.google.gson.JsonElement;
import lombok.Getter;
import net.forthecrown.utils.JsonSerializable;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.text.Text;
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
    DEFAULT         (true,      -1, NONE,      "default"),

    LEGACY_FREE     (false,     -1, FREE,      "&8[&7Veteran Knight&8]"),
    KNIGHT          (true,      -1, FREE,      "&8[&7Knight&8]"),
    BARON           (false,      4, FREE,      "&8[&7Baron&8]"),
    BARONESS        (false,      3, FREE,      "&8[&7Baroness&8]"),
    VIKING          (false,     -1, FREE,      "&8[&7Viking&8]"), // how earn?
    BERSERKER       (false,     -1, FREE,      "&8[&7Berserker&8]"), // how earn?

    LEGACY_TIER_1   (false,     -1, TIER_1,    "&#959595[&6Veteran Lord&#959595]"),
    LORD            (true,       9, TIER_1,    "&#959595[&6Lord&#959595]"),
    LADY            (true,       8, TIER_1,    "&#959595[&6Lady&#959595]"),
    SAILOR          (false,     -1, TIER_1,    "&#959595[&6Sailor&#959595]"),
    WARRIOR         (false,     12, TIER_1,    "&#959595[&6Warrior&#959595]"),
    SHIELD_MAIDEN   (false,     11, TIER_1,    "&#959595[&6ShieldMaiden&#959595]"),

    LEGACY_TIER_2   (false,     -1, TIER_2,    "&7[&#ffbf15Veteran Duke&7]"),
    DUKE            (true,      15, TIER_2,    "&7[&#ffbf15Duke&7]"),
    DUCHESS         (true,      14, TIER_2,    "&7[&#ffbf15Duchess&7]"),
    CAPTAIN         (false,     -1, TIER_2,    "&7[&#ffbf15Captain&7]"),
    ELITE           (false,     -1, TIER_2,    "&7[&#ffbf15Elite&7]"),
    HERSIR          (false,     -1, TIER_2,    "&7[&#ffbf15Hersir&7]"),

    LEGACY_TIER_3   (false,     -1, TIER_3,    "&f[&#FBFF0FVeteran Prince&f]"),
    PRINCE          (true,      21, TIER_3,    "&f[&#FBFF0FPrince&f]"),
    PRINCESS        (true,      20, TIER_3,    "&f[&#FBFF0FPrincess&f]"),
    ADMIRAL         (false,     -1, TIER_3,    "&f[&#FBFF0FAdmiral&f]"),
    ROYAL           (false,     -1, TIER_3,    "&f[&#FBFF0FRoyal&f]"),
    JARL            (false,     -1, TIER_3,    "&f[&#FBFF0FJarl&f]"),
    LEGEND          (false,     -1, TIER_3,    "&#dfdfdf[&#fff147Legend&#dfdfdf]");

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

    private final int genderEquivalent;

    RankTitle(boolean defaultTitle, int gendered, RankTier tier, String prefix) {
        this.defaultTitle = defaultTitle;
        this.tier = tier;
        this.genderEquivalent = gendered;

        this.truncatedPrefix = fromString(prefix);
        this.prefix = fromString(prefix == null ? null : prefix + ' ');

        this.tier.titles.add(this);
    }

    private Component fromString(String prefix) {
        return prefix == null ? null : Text.renderString(prefix);
    }

    /** Gets the rank's opposite gender equivalent */
    public RankTitle getGenderEquivalent() {
        if (genderEquivalent == -1) {
            return null;
        }

        return values()[genderEquivalent];
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