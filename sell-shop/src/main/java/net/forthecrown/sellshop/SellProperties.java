package net.forthecrown.sellshop;

import net.forthecrown.user.Properties;
import net.forthecrown.user.UserProperty;

public class SellProperties {

  public static final UserProperty<SellAmount> SELL_AMOUNT
      = Properties.enumProperty(SellAmount.class)
      .defaultValue(SellAmount.PER_1)
      .key("sellAmount")
      .build();

  public static final UserProperty<Boolean> SELLING_NAMED
      = Properties.booleanProperty("sellingNamedItems", false);

  public static final UserProperty<Boolean> SELLING_LORE
      = Properties.booleanProperty("sellingLoreItems", false);

  public static final UserProperty<Boolean> COMPACTED
      = Properties.booleanProperty("sellingCompact", false);

  public static void registerAll() {
    // Empty initializer stub
    // :(
  }
}
