package net.forthecrown.cosmetics.login;

import static net.kyori.adventure.text.Component.text;

import net.forthecrown.user.User;
import net.forthecrown.user.data.RankTier;
import net.forthecrown.utils.inventory.menu.Slot;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class LoginEffects {

  private static final TextColor
      FR_COLOR = NamedTextColor.GRAY,
      T1_COLOR = NamedTextColor.YELLOW,
      T2_COLOR = NamedTextColor.GOLD,
      T3_COLOR = TextColor.fromHexString("#fe771c");

  public static final LoginEffect
      FREE_RANK = create("Free rank", Slot.of(2, 1), RankTier.FREE, text(">", FR_COLOR),
          text("<", FR_COLOR)),
      TIER_1 = create("Tier 1", Slot.of(3, 1), RankTier.TIER_1, text(">", T1_COLOR),
          text("<", T1_COLOR)),
      TIER_2 = create("Tier 2", Slot.of(4, 1), RankTier.TIER_2, text(">", T2_COLOR),
          text("<", T2_COLOR)),
      TIER_3 = create("Tier 3", Slot.of(5, 1), RankTier.TIER_3, text(">", T3_COLOR),
          text("<", T3_COLOR));

  private static LoginEffect create(String name, Slot slot, RankTier tier, Component prefix,
                                    Component suffix
  ) {
    return new LoginEffect(name, slot, tier, prefix, suffix);
  }

  public static Component getDisplayName(User user, Audience viewer) {
      return user.listDisplayName(true)
          .color(NamedTextColor.YELLOW);
  }
}