package net.forthecrown.user.data;

import static net.forthecrown.user.data.RankTier.FREE;
import static net.forthecrown.user.data.RankTier.NONE;
import static net.forthecrown.user.data.RankTier.TIER_1;
import static net.forthecrown.user.data.RankTier.TIER_2;
import static net.forthecrown.user.data.RankTier.TIER_3;
import static net.kyori.adventure.text.Component.text;

import com.google.gson.JsonElement;
import lombok.Getter;
import net.forthecrown.utils.JsonSerializable;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

/**
 * Rank titles are the predetermined prefixes that all/most users can select from to be displayed in
 * the TAB menu.
 * <p>
 * Each rank has a tier, which broadly states which tier is required to access said title, some
 * titles come default with a tier, for these ranks {@link #isDefaultTitle()} will return true, for
 * the rest, it will return false.
 *
 * @see RankTitle
 * @see UserTitles
 * @see UserTitles#ensureSynced()
 */
@Getter
public enum RankTitle implements JsonSerializable, ComponentLike {
  DEFAULT(true, -1, NONE, "default"),

  LEGACY_FREE(false, -1, FREE, "&8[&7Veteran Knight&8]"),
  KNIGHT(true, -1, FREE, "&8[&7Knight&8]"),
  BARON(false, 4, FREE, "&8[&7Baron&8]"),
  BARONESS(false, 3, FREE, "&8[&7Baroness&8]"),
  VIKING(false, -1, FREE, "&8[&7Viking&8]"), // how earn?
  BERSERKER(false, -1, FREE, "&8[&7Berserker&8]"), // how earn?

  LEGACY_TIER_1(false, -1, TIER_1, "&#959595[&eVeteran Lord&#959595]"),
  LORD(true, 9, TIER_1, "&#959595[&eLord&#959595]"),
  LADY(true, 8, TIER_1, "&#959595[&eLady&#959595]"),
  SAILOR(false, -1, TIER_1, "&#959595[&eSailor&#959595]"),
  WARRIOR(false, 12, TIER_1, "&#959595[&eWarrior&#959595]"),
  SHIELD_MAIDEN(false, 11, TIER_1, "&#959595[&eShieldMaiden&#959595]"),

  LEGACY_TIER_2(false, -1, TIER_2, "&7[&6Veteran Duke&7]"), // old duke color: #ffbf15
  DUKE(true, 15, TIER_2, "&7[&6Duke&7]"),
  DUCHESS(true, 14, TIER_2, "&7[&6Duchess&7]"),
  CAPTAIN(false, -1, TIER_2, "&7[&6Captain&7]"),
  ELITE(false, -1, TIER_2, "&7[&6Elite&7]"),
  HERSIR(false, -1, TIER_2, "&7[&6Hersir&7]"),

  LEGACY_TIER_3(false, -1, TIER_3, getTier3Prefix("Veteran Prince")),
  PRINCE(true, 21, TIER_3, getTier3Prefix("Prince")),
  PRINCESS(true, 20, TIER_3, getTier3Prefix("Princess")),
  ADMIRAL(false, -1, TIER_3, "&f[&#FBFF0FAdmiral&f]"),
  ROYAL(false, -1, TIER_3, "&f[&#FBFF0FRoyal&f]"),
  JARL(false, -1, TIER_3, "&f[&#FBFF0FJarl&f]"),
  LEGEND(false, -1, TIER_3, "&#dfdfdf[&#fff147Legend&#dfdfdf]");

  private static Component getTier3Prefix(String s) {
      return text()
              .color(NamedTextColor.WHITE)
              .append(
                      text("["),
                      Text.gradient(s, NamedTextColor.GOLD, TextColor.fromHexString("#fe771c")),
                      text("]")
              )
              .build();
  }

  /**
   * Determines if this title comes with the tier or whether the title must be attained separately
   */
  private final boolean defaultTitle;

  /**
   * The title's tier
   */
  private final RankTier tier;

  /**
   * The title's TAB prefix with a trailing space to leave room for the player's name
   */
  private final Component prefix;

  /**
   * The truncated prefix of this title, this has no leading or trailing spaces
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

  RankTitle(boolean defaultTitle, int gendered, RankTier tier, Component prefix) {
    this.defaultTitle = defaultTitle;
    this.tier = tier;
    this.genderEquivalent = gendered;

    this.truncatedPrefix = prefix;
    this.prefix = prefix.append(Component.space());

    this.tier.titles.add(this);
  }

  private Component fromString(String prefix) {
    return prefix == null ? null : Text.renderString(prefix);
  }

  /**
   * Gets the rank's opposite gender equivalent
   */
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