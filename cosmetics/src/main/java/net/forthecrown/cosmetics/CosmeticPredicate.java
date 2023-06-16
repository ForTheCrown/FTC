package net.forthecrown.cosmetics;

import net.forthecrown.user.User;

/**
 * Predicate used to test if a cosmetic is owned by a specified user
 *
 * @param <T> Cosmetic's value type
 */
public interface CosmeticPredicate<T> {

  CosmeticPredicate DEFAULT = (user, cosmetic) -> {
    CosmeticData data = user.getComponent(CosmeticData.class);
    return data.contains(cosmetic);
  };

  static <T> CosmeticPredicate<T> defaultPredicate() {
    return DEFAULT;
  }

  boolean test(User user, Cosmetic<T> cosmetic);
}