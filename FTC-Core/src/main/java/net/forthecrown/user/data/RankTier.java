package net.forthecrown.user.data;

import com.google.gson.JsonElement;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

/**
 * Represents a rank's tier
 */
public enum RankTier implements Comparable<RankTier>, JsonSerializable {

    NONE (1, NamedTextColor.WHITE, "default"),
    FREE (2, NamedTextColor.GRAY, "free-rank"),

    TIER_1 (3, NamedTextColor.GRAY, "donator-tier-1"),
    TIER_2 (4, NamedTextColor.GOLD, "donator-tier-2"),
    TIER_3 (5, NamedTextColor.YELLOW, "donator-tier-3");

    public final byte maxHomes;
    public final TextColor color;
    public final String luckPermsGroup;

    RankTier(int maxHomes, TextColor color, String luckPermsGroup){
        this.maxHomes = (byte) maxHomes;
        this.luckPermsGroup = luckPermsGroup;
        this.color = color;
    }

    /**
     * Returns whether the tier should use colors
     * @return ^^^^
     */
    public boolean shouldUseColor(){
        return this == TIER_2;
    }

    /**
     * Returns whether the given tier is lower than this tier
     * @param tier The tier to check against
     * @return Whether this tier is higher than the given tier
     */
    public boolean isHigherTierThan(RankTier tier){
        return ordinal() > tier.ordinal();
    }

    public RankTier getLowerTier() {
        int ordinal = ordinal() - 1;
        if(ordinal == -1) return null;

        return values()[ordinal];
    }

    @Override
    public JsonElement serialize() {
        return JsonUtils.writeEnum(this);
    }
}
