package net.forthecrown.inventory.weapon.upgrades;

import net.forthecrown.inventory.weapon.RoyalSword;
import net.forthecrown.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public record GemReward(int amount) implements WeaponUpgrade {

  @Override
  public void apply(RoyalSword sword, ItemStack item, ItemMeta meta) {
    if (sword.isDisplayItem()) {
      return;
    }

    var owner = sword.getOwnerUser();
    if (owner == null) {
      return;
    }

    owner.addGems(amount);
    owner.sendMessage(Text.format("You got &6{0, gems}!", NamedTextColor.YELLOW, amount));
  }

  @Override
  public Component loreDisplay() {
    return Text.format("{0, gems}", amount);
  }
}
