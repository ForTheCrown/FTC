package net.forthecrown.economy.sell;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the sell data of a single
 * item.
 */
@Getter
@RequiredArgsConstructor
@Builder(builderClassName = "Builder")
public class ItemSellData {
    /* ----------------------------- INSTANCE FIELDS ------------------------------ */

    /**
     * The primary material this data represents
     */
    private final Material material;

    /**
     * The compact material this data represents, may be null
     */
    @Nullable
    private final Material compactMaterial;

    /**
     * The default price of this item
     */
    private final int price;

    /**
     * The amount of {@link #material} it
     * takes to create a single {@link #compactMaterial}.
     * <p>
     * This also acts as a price scalar
     */
    private final int compactMultiplier;

    /**
     * The maximum amount of rhines that can be earned
     * from this item, by default {@link PriceMapReader#DEF_MAX_EARNINGS}
     */
    private final int maxEarnings;

    /**
     * The inventory slot of this item
     * within the menu it is in
     */
    private final int inventoryIndex;

    /* ----------------------------- METHODS ------------------------------ */

    /**
     * Tests if this item can be compacted
     * @return True, if this item can be compacted, false otherwise
     */
    public boolean canBeCompacted() {
        return compactMaterial != null && compactMultiplier > 0;
    }

    /**
     * Calculates the price of this item
     * with the given parameter using
     * Wout's algorithm
     *
     * @param earned The amount of rhines that have
     *               already been earned from this
     *               data
     * @return The calculated price, in bounds [{@link #getPrice()}..0]
     */
    public int calculatePrice(int earned) {
        if (earned >= maxEarnings) {
            return 0;
        }

        int m = maxEarnings;
        int x = earned;
        int s = price;

        double B = 0.00015d;
        double C = B*m/2d;
        double D = s / 2d;
        double A = -s / (2d * Math.atan(C));

        return Math.max(0, (int) Math.ceil(A * Math.atan(B * x - C) + D));
    }
}