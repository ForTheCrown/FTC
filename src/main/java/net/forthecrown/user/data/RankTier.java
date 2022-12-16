package net.forthecrown.user.data;

import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.utils.JsonSerializable;
import net.forthecrown.utils.io.JsonUtils;

import java.util.Set;

/**
 * Represents a rank's tier.
 * <p>
 * Currently, a rank's tier determines how many homes a
 * user is allowed to have and what their luck perms tier
 * is. Although the last one goes both ways. When a user
 * joins {@link UserTitles#ensureSynced()} is called to
 * make sure both the luck perms and rank tiers are synced
 * to each other.
 *
 * @see UserTitles#ensureSynced()
 * @see RankTitle
 * @see UserTitles
 */
@Getter
@RequiredArgsConstructor
public enum RankTier implements JsonSerializable {

    /**
     * Tier that represents the absence of a tier, has the "default"
     * luck perms group with 1 home
     */
    NONE   ("default"       ),

    /**
     * Free tier, for ranks that can be earned for free,
     * 2 homes and "free-rank" luck perms group
     */
    FREE   ("free-rank"     ),

    /** Tier 1 donators, 3 max homes and "donator-tier-1" luck perms group */
    TIER_1 ("donator-tier-1"),

    /** Tier 2 donators, 4 max homes and "donator-tier-2" luck perms group */
    TIER_2 ("donator-tier-2"),

    /** Tier 3 donators, 5 max homes and "donator-tier-3" luck perms group */
    TIER_3 ("donator-tier-3");

    /** The tier's LuckPerms permission group */
    private final String luckPermsGroup;

    /** All titles that belong to this tier */
    final ObjectSet<RankTitle> titles = new ObjectOpenHashSet<>();

    /**
     * Gets a cloned enum set of all the
     * titles this tier holds
     * @return Cloned enum set of this tier's titles
     */
    public Set<RankTitle> getTitles() {
        return ObjectSets.unmodifiable(titles);
    }

    @Override
    public JsonElement serialize() {
        return JsonUtils.writeEnum(this);
    }
}