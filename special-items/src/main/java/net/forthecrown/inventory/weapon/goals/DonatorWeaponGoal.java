package net.forthecrown.inventory.weapon.goals;

import lombok.Getter;
import net.forthecrown.text.Messages;
import net.forthecrown.titles.RankTier;
import net.forthecrown.titles.UserTitles;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;

@Getter
public class DonatorWeaponGoal implements WeaponGoal {

  @Override
  public int getGoal() {
    return 1;
  }

  @Override
  public boolean test(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Player player)) {
      return false;
    }

    User user = Users.get(player);
    UserTitles titles = user.getComponent(UserTitles.class);

    return titles.hasTier(RankTier.TIER_1);
  }

  @Override
  public Component loreDisplay() {
    return Component.text("Bought Tier-1 Donator ")
        .append(Messages.HEART);
  }

  @Override
  public @NotNull String getName() {
    return "donator";
  }
}