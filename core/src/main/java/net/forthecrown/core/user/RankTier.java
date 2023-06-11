package net.forthecrown.core.user;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.text.WordUtils;

/**
 * Represents a rank's tier.
 * @see UserRank
 * @see UserTitles
 */
@Getter
@RequiredArgsConstructor
public enum RankTier {

  /**
   * Tier that represents the absence of a tier, has the "default" luck perms group with 1 home
   */
  NONE("default"),

  /**
   * Free tier, for ranks that can be earned for free, 2 homes and "free-rank" luck perms group
   */
  FREE("free-rank"),

  /**
   * Tier 1 donators, 3 max homes and "donator-tier-1" luck perms group
   */
  TIER_1("donator-tier-1"),

  /**
   * Tier 2 donators, 4 max homes and "donator-tier-2" luck perms group
   */
  TIER_2("donator-tier-2"),

  /**
   * Tier 3 donators, 5 max homes and "donator-tier-3" luck perms group
   */
  TIER_3("donator-tier-3");

  /**
   * The tier's LuckPerms permission group
   */
  private final String luckPermsGroup;

  /**
   * All titles that belong to this tier
   */
  final ObjectList<UserRank> titles = new ObjectArrayList<>();

  /**
   * Gets a cloned enum set of all the titles this tier holds
   *
   * @return Cloned enum set of this tier's titles
   */
  public List<UserRank> getTitles() {
    return ObjectLists.unmodifiable(titles);
  }

  public String getDisplayName() {
    return WordUtils.capitalizeFully(name().replaceAll("_", " "));
  }
}