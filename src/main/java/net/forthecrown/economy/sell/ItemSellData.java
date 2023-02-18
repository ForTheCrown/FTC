package net.forthecrown.economy.sell;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the sell data of a single item.
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
   * The amount of {@link #material} it takes to create a single {@link #compactMaterial}.
   * <p>
   * This also acts as a price scalar
   */
  private final int compactMultiplier;

  /**
   * The maximum amount of rhines that can be earned from this item, by default
   * {@link PriceMapReader#DEF_MAX_EARNINGS}
   */
  private final int maxEarnings;

  /**
   * The inventory slot of this item within the menu it is in
   */
  private final int inventoryIndex;

  /**
   * Vars for math
   */
  private final double
          B = 0.00015d,
          C = B * maxEarnings / 2d,
          D = price / 2d,
          A = -price / (2* Math.atan(C));

  /* ----------------------------- METHODS ------------------------------ */

  /**
   * Tests if this item can be compacted
   *
   * @return True, if this item can be compacted, false otherwise
   */
  public boolean canBeCompacted() {
    return compactMaterial != null && compactMultiplier > 0;
  }

  /**
   * Calculates the price of this item with the given parameter using Wout's algorithm
   *
   * @param earned The amount of rhines that have already been earned from this data
   * @return The calculated price, in bounds [{@link #getPrice()}..0]
   */
  public int calculatePrice(int earned) {
    if (earned >= maxEarnings) {
      return 0;
    }

    return Math.max(0, (int) Math.ceil(A * Math.atan(B * earned - C) + D));
  }

  // Idk what to call this, squiglyBoy()?
  private double h(double x) {
    return (Math.tan((x - D) / A) + C) / B;
  }

  // Inverse of calculatePrice()
  private double calculatePriceInv(double x) {
    return ((Math.tan(Math.atan(B*x - C) - 1/A) + C) / B ) - x;
  }

  /**
   * Calculate in which segment of the calculatePrice(earned) function "earned" is part of.
   * @param earned The amount of rhines that have already been earned from this data
   * @return The number of the segment, in bounds [{s-1}..0]
   */
  private int calcN(double earned) {
    int segmentNb = price-1;
    double limit = h(segmentNb);

    while (limit < earned || segmentNb == 0) {
      segmentNb--;
      limit = h(segmentNb);
    }

    return segmentNb;
  }

  /**
   * Calculate how much can still be earned before the price of this data drops
   * @param earned The amount of rhines that have already been earned from this data
   * @return The amount of rhines to earn at current price.
   */
  public int calcPriceDrop(double earned) {
    // temp vars
    int n = calcN(earned);
    double hn = h(n);
    double hn1 = h(n+1);

    return Math.max(0, (int) Math.ceil( (calculatePriceInv(hn1) * (earned - hn)) / (hn1 - hn) ));
  }

}