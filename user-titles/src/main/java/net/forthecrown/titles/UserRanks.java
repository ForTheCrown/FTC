package net.forthecrown.titles;

import static net.forthecrown.titles.RankTier.FREE;
import static net.forthecrown.titles.RankTier.NONE;
import static net.forthecrown.titles.RankTier.TIER_1;
import static net.forthecrown.titles.RankTier.TIER_2;
import static net.forthecrown.titles.RankTier.TIER_3;
import static net.forthecrown.titles.TitleSettings.SEE_RANKS;
import static net.kyori.adventure.text.Component.text;

import com.google.gson.JsonElement;
import net.forthecrown.menu.Slot;
import net.forthecrown.registry.Holder;
import net.forthecrown.registry.Ref;
import net.forthecrown.registry.Ref.KeyRef;
import net.forthecrown.registry.Registries;
import net.forthecrown.registry.Registry;
import net.forthecrown.registry.RegistryListener;
import net.forthecrown.text.Text;
import net.forthecrown.user.name.DisplayContext;
import net.forthecrown.user.name.DisplayIntent;
import net.forthecrown.utils.Result;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

@SuppressWarnings("unused")
public final class UserRanks {
  private UserRanks() {}

  public static final Registry<UserRank> REGISTRY = Registries.newRegistry();

  static {
    REGISTRY.setListener(new RegistryListener<>() {
      @Override
      public void onRegister(Holder<UserRank> value) {
        UserRank title = value.getValue();
        RankTier tier = title.getTier();
        tier.titles.add(title);
      }

      @Override
      public void onUnregister(Holder<UserRank> value) {
        UserRank title = value.getValue();
        RankTier tier = title.getTier();
        tier.titles.remove(title);
      }
    });
  }

  public static final String DEFAULT_NAME = "default";

  public static final KeyRef<UserRank> DEFAULT_REF = Ref.key(DEFAULT_NAME);

  public static final UserRank DEFAULT = UserRank.builder()
      .slot(1, 1)
      .defaultTitle(true)
      .prefix("No Title")
      .hidden(true)
      .tier(NONE)
      .registered(DEFAULT_NAME);

  // Auto generated from enum constants
  // Except for the menu slots and description

  public static final UserRank LEGACY_FREE = UserRank.builder()
      .tier(FREE)
      .prefix("&8[&7Veteran Knight&8]")
      .addDesc("Given to players that earned")
      .addDesc("the Knight rank before 1.18.")
      .registered("legacy_free");

  public static final UserRank KNIGHT = UserRank.builder()
      .tier(FREE)
      .asDefault()
      .slot(3, 1)
      .prefix("&8[&7Knight&8]")
      .addDesc("Earned by defeating the first")
      .addDesc("three bosses in the Dungeons.")
      .registered("knight");

  public static final UserRank BARON = UserRank.builder()
      .tier(FREE)
      .slot(5, 1)
      .genderEquivalentKey("baroness")
      .prefix("&8[&7Baron&8]")
      .addDesc("Earned with /becomebaron.")
      .registered("baron");

  public static final UserRank BARONESS = UserRank.builder()
      .tier(FREE)
      .slot(5, 2)
      .genderEquivalentKey("baron")
      .prefix("&8[&7Baroness&8]")
      .addDesc("Earned with /becomebaron.")
      .registered("baroness");

  public static final UserRank VIKING = UserRank.builder()
      .tier(FREE)
      .prefix("&8[&7Viking&8]")
      .hidden(true)
      .addDesc("Unobtainable")
      .registered("viking");

  public static final UserRank BERSERKER = UserRank.builder()
      .tier(FREE)
      .prefix("&8[&7Berserker&8]")
      .hidden(true)
      .addDesc("Unobtainable")
      .registered("berserker");


  public static final UserRank LEGACY_TIER_1 = UserRank.builder()
      .tier(TIER_1)
      .prefix("&#959595[&eVeteran Lord&#959595]")
      .genderEquivalentKey("legacy_tier_1_fem")
      .addDesc("Given to players that had")
      .addDesc("the Lord rank before 1.18.")
      .hidden(true)
      .registered("legacy_tier_1");

  public static final UserRank LEGACY_TIER_1_F = UserRank.builder()
      .tier(TIER_1)
      .prefix("&#959595[&eVeteran Lady&#959595]")
      .genderEquivalentKey("legacy_tier_1")
      .addDesc("Given to players that had")
      .addDesc("the Lady rank before 1.18.")
      .hidden(true)
      .registered("legacy_tier_1_fem");

  public static final UserRank LORD = UserRank.builder()
      .tier(TIER_1)
      .asDefault()
      .slot(3, 1)
      .genderEquivalentKey("lady")
      .addDesc("Included in Tier-1 rank package.")
      .prefix("&#959595[&eLord&#959595]")
      .registered("lord");

  public static final UserRank LADY = UserRank.builder()
      .tier(TIER_1)
      .asDefault()
      .slot(5, 1)
      .genderEquivalentKey("lord")
      .addDesc("Included in Tier-1 rank package.")
      .prefix("&#959595[&eLady&#959595]")
      .registered("lady");

  public static final UserRank SAILOR = UserRank.builder()
      .tier(TIER_1)
      .prefix("&#959595[&eSailor&#959595]")
      .addDesc("Unobtainable")
      .registered("sailor");

  public static final UserRank WARRIOR = UserRank.builder()
      .tier(TIER_1)
      .genderEquivalentKey("shield_maiden")
      .prefix("&#959595[&eWarrior&#959595]")
      .addDesc("Unobtainable")
      .hidden(true)
      .registered("warrior");

  public static final UserRank SHIELD_MAIDEN = UserRank.builder()
      .tier(TIER_1)
      .genderEquivalentKey("warrior")
      .prefix("&#959595[&eShieldMaiden&#959595]")
      .addDesc("Unobtainable")
      .hidden(true)
      .registered("shield_maiden");


  public static final UserRank LEGACY_TIER_2 = UserRank.builder()
      .tier(TIER_2)
      .prefix("&7[&6Veteran Duke&7]")
      .genderEquivalentKey("legacy_tier_2_fem")
      .addDesc("Given to players that had")
      .addDesc("the Duke rank before 1.18.")
      .hidden(true)
      .registered("legacy_tier_2");

  public static final UserRank LEGACY_TIER_2_F = UserRank.builder()
      .tier(TIER_2)
      .prefix("&7[&6Veteran Duchess&7]")
      .genderEquivalentKey("legacy_tier_2")
      .addDesc("Given to players that had")
      .addDesc("the Duchess rank before 1.18.")
      .hidden(true)
      .registered("legacy_tier_2_fem");

  public static final UserRank DUKE = UserRank.builder()
      .tier(TIER_2)
      .asDefault()
      .slot(3, 1)
      .genderEquivalentKey("duchess")
      .addDesc("Included in Tier-2 rank package.")
      .prefix("&7[&6Duke&7]")
      .registered("duke");

  public static final UserRank DUCHESS = UserRank.builder()
      .tier(TIER_2)
      .asDefault()
      .slot(5, 1)
      .genderEquivalentKey("duke")
      .addDesc("Included in Tier-2 rank package.")
      .prefix("&7[&6Duchess&7]")
      .registered("duchess");

  public static final UserRank CAPTAIN = UserRank.builder()
      .tier(TIER_2)
      .prefix("&7[&6Captain&7]")
      .addDesc("Was available in the webshop during 2022.")
      .hidden(true)
      .registered("captain");

  public static final UserRank ELITE = UserRank.builder()
      .tier(TIER_2)
      .prefix("&7[&6Elite&7]")
      .addDesc("Was available in the webshop during 2022.")
      .registered("elite");

  public static final UserRank HERSIR = UserRank.builder()
      .tier(TIER_2)
      .prefix("&7[&6Hersir&7]")
      .addDesc("Unobtainable")
      .hidden(true)
      .registered("hersir");


  public static final UserRank LEGACY_TIER_3 = UserRank.builder()
      .tier(TIER_3)
      .truncatedPrefix(getTier3Prefix("Veteran Prince"))
      .genderEquivalentKey("legacy_tier_3_fem")
      .addDesc("Given to players that had")
      .addDesc("the Prince rank before 1.18.")
      .hidden(true)
      .registered("legacy_tier_3");

  public static final UserRank LEGACY_TIER_3_F = UserRank.builder()
      .tier(TIER_3)
      .truncatedPrefix(getTier3Prefix("Veteran Princess"))
      .genderEquivalentKey("legacy_tier_3")
      .addDesc("Given to players that had")
      .addDesc("the Princess rank before 1.18.")
      .hidden(true)
      .registered("legacy_tier_3_fem");

  public static final UserRank PRINCE = UserRank.builder()
      .tier(TIER_3)
      .asDefault()
      .slot(3, 1)
      .genderEquivalentKey("princess")
      .addDesc("Available in Tier-3 rank package.")
      .truncatedPrefix(getTier3Prefix("Prince"))
      .registered("prince");

  public static final UserRank PRINCESS = UserRank.builder()
      .tier(TIER_3)
      .asDefault()
      .slot(5, 1)
      .genderEquivalentKey("prince")
      .addDesc("Available in Tier-3 rank package.")
      .truncatedPrefix(getTier3Prefix("Princess"))
      .registered("princess");

  public static final UserRank ADMIRAL = UserRank.builder()
      .tier(TIER_3)
      .prefix("&f[&#fbff0fAdmiral&f]")
      .addDesc("Was available in the webshop during 2022.")
      .hidden(true)
      .registered("admiral");

  public static final UserRank ROYAL = UserRank.builder()
      .tier(TIER_3)
      .prefix("&f[&#fbff0fRoyal&f]")
      .addDesc("Was available in the webshop during 2022.")
      .hidden(true)
      .registered("royal");

  public static final UserRank JARL = UserRank.builder()
      .tier(TIER_3)
      .prefix("&f[&#fbff0fJarl&f]")
      .addDesc("Unobtainable")
      .hidden(true)
      .registered("jarl");

  public static final UserRank LEGEND = UserRank.builder()
      .tier(TIER_3)
      .prefix("&#dfdfdf[&#fff147Legend&#dfdfdf]")
      .addDesc("Was available in the webshop during 2020.")
      .hidden(true)
      .registered("legend");

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
   * Removes all dynamically loaded ranks from the rank registry.
   * <p>
   * Whether a rank is dynamically loaded is tested with {@link UserRank#isReloadable()}
   */
  public static void clearNonConstants() {
    REGISTRY.removeIf(h -> h.getValue().isReloadable());
  }

  public static boolean showRank(DisplayContext context) {
    // Don't display rank prefix if the user has disabled it,
    // only in certain circumstances though
    return !context.intentMatches(DisplayIntent.UNSET, DisplayIntent.HOVER_TEXT)
         || context.viewerProperty(SEE_RANKS);
  }

  public static Result<UserRank> deserialize(JsonElement element) {
    if (element == null || !element.isJsonObject()) {
      return Result.error("Invalid JSON: " + element);
    }

    JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());
    var prefix = json.getComponent("prefix");

    if (prefix == null) {
      return Result.error("No prefix set");
    }

    var builder = UserRank.builder()
        .reloadable(true)
        .truncatedPrefix(prefix)
        .genderEquivalentKey(json.getString("genderEquivalent"))
        .hidden(json.getBool("hidden", false))
        .defaultTitle(json.getBool("defaultTitle", false));

    if (json.has("tier")) {
      builder.tier(json.getEnum("tier", RankTier.class));

      if (builder.tier() == NONE) {
        return Result.error("Tier NONE is not supported for ranks");
      }
    } else {
      return Result.error("No tier set");
    }

    json.getList("description", JsonUtils::readText)
        .forEach(builder::addDesc);

    return Result.success(
        builder.menuSlot(json.get("slot", Slot::load))
            .build()
    );
  }
}