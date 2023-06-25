package net.forthecrown.cosmetics;

import java.util.function.Predicate;
import net.forthecrown.titles.RankTier;
import net.forthecrown.titles.UserRanks;
import net.forthecrown.titles.UserTitles;
import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;

public record LoginEffect(Predicate<User> predicate, Component prefix, Component suffix) {

  public LoginEffect(RankTier tier, Component prefix, Component suffix) {
    this(hasTier(tier), prefix, suffix);
  }

  public LoginEffect(String rankName, Component prefix, Component suffix) {
    this(hasTitle(rankName), prefix, suffix);
  }

  private static Predicate<User> hasTitle(String rankName) {
    return user -> {
      return UserRanks.REGISTRY.get(rankName)
          .filter(userRank -> {
            UserTitles titles = user.getComponent(UserTitles.class);
            return titles.hasTitle(userRank);
          })
          .isPresent();
    };
  }

  private static Predicate<User> hasTier(RankTier tier) {
    return user -> {
      UserTitles titles = user.getComponent(UserTitles.class);
      return titles.hasTier(tier);
    };
  }
}