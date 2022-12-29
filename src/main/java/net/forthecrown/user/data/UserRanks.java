package net.forthecrown.user.data;

import static net.forthecrown.user.data.RankTier.FREE;
import static net.forthecrown.user.data.RankTier.NONE;
import static net.forthecrown.user.data.RankTier.TIER_1;
import static net.forthecrown.user.data.RankTier.TIER_2;
import static net.forthecrown.user.data.RankTier.TIER_3;
import static net.kyori.adventure.text.Component.text;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import net.forthecrown.core.module.OnEnable;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.economy.sell.MenuReader;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public final class UserRanks {
  private UserRanks() {}

  public static final Registry<UserRank> REGISTRY = Registries.newRegistry();

  public static final UserRank DEFAULT = UserRank.builder()
      .slot(1, 1)
      .defaultTitle(true)
      .hidden(true)
      .tier(NONE)
      .registered("default");

  // Auto generated from enum constants
  // Except for the menu slots and description

  public static final UserRank LEGACY_FREE = UserRank.builder()
      .tier(FREE)
      .prefix("&8[&7Veteran Knight&8]")
      .registered("legacy_free");

  public static final UserRank KNIGHT = UserRank.builder()
      .tier(FREE)
      .asDefault()
      .slot(3, 1)
      .prefix("&8[&7Knight&8]")
      .addDesc("Earned by defeating the first 3 Dungeon bosses")
      .registered("knight");

  public static final UserRank BARON = UserRank.builder()
      .tier(FREE)
      .slot(5, 1)
      .genderEquivalentKey("baroness")
      .prefix("&8[&7Baron&8]")
      .addDesc("Earned with /becomebaron")
      .registered("baron");

  public static final UserRank BARONESS = UserRank.builder()
      .tier(FREE)
      .slot(5, 2)
      .genderEquivalentKey("baron")
      .prefix("&8[&7Baroness&8]")
      .addDesc("Earned with /becomebaron")
      .registered("baroness");

  public static final UserRank VIKING = UserRank.builder()
      .tier(FREE)
      .prefix("&8[&7Viking&8]")
      .hidden(true)
      .registered("viking");

  public static final UserRank BERSERKER = UserRank.builder()
      .tier(FREE)
      .prefix("&8[&7Berserker&8]")
      .hidden(true)
      .registered("berserker");


  public static final UserRank LEGACY_TIER_1 = UserRank.builder()
      .tier(TIER_1)
      .prefix("&#959595[&eVeteran Lord&#959595]")
      .genderEquivalentKey("legacy_tier_1_fem")
      .hidden(true)
      .registered("legacy_tier_1");

  public static final UserRank LEGACY_TIER_1_F = UserRank.builder()
      .tier(TIER_1)
      .prefix("&#959595[&eVeteran Lady&#959595]")
      .genderEquivalentKey("legacy_tier_1")
      .hidden(true)
      .registered("legacy_tier_1_fem");

  public static final UserRank LORD = UserRank.builder()
      .tier(TIER_1)
      .asDefault()
      .slot(3, 1)
      .genderEquivalentKey("lady")
      .prefix("&#959595[&eLord&#959595]")
      .registered("lord");

  public static final UserRank LADY = UserRank.builder()
      .tier(TIER_1)
      .asDefault()
      .slot(5, 1)
      .genderEquivalentKey("lord")
      .prefix("&#959595[&eLady&#959595]")
      .registered("lady");

  public static final UserRank SAILOR = UserRank.builder()
      .tier(TIER_1)
      .prefix("&#959595[&eSailor&#959595]")
      .registered("sailor");

  public static final UserRank WARRIOR = UserRank.builder()
      .tier(TIER_1)
      .genderEquivalentKey("shield_maiden")
      .prefix("&#959595[&eWarrior&#959595]")
      .hidden(true)
      .registered("warrior");

  public static final UserRank SHIELD_MAIDEN = UserRank.builder()
      .tier(TIER_1)
      .genderEquivalentKey("warrior")
      .prefix("&#959595[&eShieldMaiden&#959595]")
      .hidden(true)
      .registered("shield_maiden");


  public static final UserRank LEGACY_TIER_2 = UserRank.builder()
      .tier(TIER_2)
      .prefix("&7[&6Veteran Duke&7]")
      .genderEquivalentKey("legacy_tier_2_fem")
      .hidden(true)
      .registered("legacy_tier_2");

  public static final UserRank LEGACY_TIER_2_F = UserRank.builder()
      .tier(TIER_2)
      .prefix("&7[&6Veteran Duchess&7]")
      .genderEquivalentKey("legacy_tier_2")
      .hidden(true)
      .registered("legacy_tier_2_fem");

  public static final UserRank DUKE = UserRank.builder()
      .tier(TIER_2)
      .asDefault()
      .slot(3, 1)
      .genderEquivalentKey("duchess")
      .prefix("&7[&6Duke&7]")
      .registered("duke");

  public static final UserRank DUCHESS = UserRank.builder()
      .tier(TIER_2)
      .asDefault()
      .slot(5, 1)
      .genderEquivalentKey("duke")
      .prefix("&7[&6Duchess&7]")
      .registered("duchess");

  public static final UserRank CAPTAIN = UserRank.builder()
      .tier(TIER_2)
      .prefix("&7[&6Captain&7]")
      .hidden(true)
      .registered("captain");

  public static final UserRank ELITE = UserRank.builder()
      .tier(TIER_2)
      .prefix("&7[&6Elite&7]")
      .registered("elite");

  public static final UserRank HERSIR = UserRank.builder()
      .tier(TIER_2)
      .prefix("&7[&6Hersir&7]")
      .hidden(true)
      .registered("hersir");


  public static final UserRank LEGACY_TIER_3 = UserRank.builder()
      .tier(TIER_3)
      .truncatedPrefix(getTier3Prefix("Veteran Prince"))
      .genderEquivalentKey("legacy_tier_3_fem")
      .hidden(true)
      .registered("legacy_tier_3");

  public static final UserRank LEGACY_TIER_3_F = UserRank.builder()
      .tier(TIER_3)
      .truncatedPrefix(getTier3Prefix("Veteran Princess"))
      .genderEquivalentKey("legacy_tier_3")
      .hidden(true)
      .registered("legacy_tier_3_fem");

  public static final UserRank PRINCE = UserRank.builder()
      .tier(TIER_3)
      .asDefault()
      .slot(3, 1)
      .genderEquivalentKey("princess")
      .truncatedPrefix(getTier3Prefix("Prince"))
      .registered("prince");

  public static final UserRank PRINCESS = UserRank.builder()
      .tier(TIER_3)
      .asDefault()
      .slot(5, 1)
      .genderEquivalentKey("prince")
      .truncatedPrefix(getTier3Prefix("Princess"))
      .registered("princess");

  public static final UserRank ADMIRAL = UserRank.builder()
      .tier(TIER_3)
      .prefix("&f[&#fbff0fAdmiral&f]")
      .hidden(true)
      .registered("admiral");

  public static final UserRank ROYAL = UserRank.builder()
      .tier(TIER_3)
      .prefix("&f[&#fbff0fRoyal&f]")
      .hidden(true)
      .registered("royal");

  public static final UserRank JARL = UserRank.builder()
      .tier(TIER_3)
      .prefix("&f[&#fbff0fJarl&f]")
      .hidden(true)
      .registered("jarl");

  public static final UserRank LEGEND = UserRank.builder()
      .tier(TIER_3)
      .prefix("&#dfdfdf[&#fff147Legend&#dfdfdf]")
      .hidden(true)
      .registered("legend");

  @OnEnable
  private static void init() {
    // Cache all ranks in their tiers
    for (var r: REGISTRY) {
      r.getTier().titles.add(r);
    }
  }

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

  public static DataResult<UserRank> parse(JsonElement element) {
    if (element == null || !element.isJsonObject()) {
      return null;
    }

    JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());
    var prefix = json.getComponent("prefix");

    if (prefix == null) {
      return DataResult.error("No prefix set");
    }

    var builder = UserRank.builder()
        .truncatedPrefix(prefix)
        .genderEquivalentKey(json.getString("genderEquivalent"))
        .hidden(json.getBool("hidden", false));

    if (json.getBool("defaultTitle", false)) {
      builder.asDefault();
    }

    if (json.has("tier")) {
      builder.tier(json.getEnum("tier", RankTier.class));

      if (builder.tier() == NONE) {
        return DataResult.error("Tier NONE is not supported for ranks");
      }
    } else {
      return DataResult.error("No tier set");
    }

    json.getList("description", JsonUtils::readText)
        .forEach(builder::addDesc);

    return DataResult.success(
        builder.menuSlot(json.get("slot", MenuReader::readSlot))
            .build()
    );
  }
}