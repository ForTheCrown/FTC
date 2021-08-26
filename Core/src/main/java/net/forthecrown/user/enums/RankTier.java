package net.forthecrown.user.enums;

import com.google.gson.JsonElement;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

/**
 * Represents a rank's tier
 */
public enum RankTier implements Comparable<RankTier>, JsonSerializable {
    NONE (1, -1, NamedTextColor.WHITE),
    FREE (2, 0, NamedTextColor.GRAY),
    TIER_1 (3, 1, NamedTextColor.GRAY),
    TIER_2 (4, 2, NamedTextColor.GOLD),
    TIER_3 (5, 3, NamedTextColor.YELLOW);

    public final byte maxHomes;
    public final byte asByte;
    public final TextColor color;

    RankTier(int maxHomes, int asByte, TextColor color){
        this.maxHomes = (byte) maxHomes;
        this.asByte = (byte) asByte;
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
        return asByte > tier.asByte;
    }

    @Override
    public JsonElement serialize() {
        return JsonUtils.writeEnum(this);
    }
}
