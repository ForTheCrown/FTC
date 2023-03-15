package net.forthecrown.cosmetics.login;

import java.util.function.Predicate;
import lombok.Getter;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.Messages;
import net.forthecrown.cosmetics.Cosmetic;
import net.forthecrown.cosmetics.Cosmetics;
import net.forthecrown.user.User;
import net.forthecrown.user.data.RankTier;
import net.forthecrown.user.data.UserRanks;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.inventory.menu.Slot;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;

@Getter
public class LoginEffect extends Cosmetic {

  private final Component prefix, suffix;

  private final Predicate<User> hasUnlocked;

  public LoginEffect(String name, Slot slot, String titleKey, Component prefix, Component suffix) {
      this(name, slot, user -> {
        var title = UserRanks.REGISTRY.get(titleKey);

        if (title.isEmpty()) {
          return false;
        }

        return user.getTitles().hasTitle(title.get());
      }, prefix, suffix);
  }

  public LoginEffect(String name, Slot slot, RankTier tier, Component prefix, Component suffix) {
      this(name, slot, user -> user.getTitles().hasTier(tier), prefix, suffix);
  }

  public LoginEffect(String name, Slot slot, Predicate<User> hasUnlocked, Component prefix, Component suffix) {
      super(name, Cosmetics.LOGIN, slot);
      this.hasUnlocked = hasUnlocked;
      this.prefix = prefix;
      this.suffix = suffix;
  }

  @Override
  public MenuNode createNode() {
    return MenuNode.builder()
        .setItem((user, context) -> {
          var builder = ItemStacks.builder(
                  displayData.getMaterial(hasUnlocked.test(user))
              )
              .setName(displayData.getItemDisplayName())
              .addLore("&7Join/Leave decoration")
              .addLore("&7Example:")
              .addLore(
                  Messages.joinMessage(
                          LoginEffects.getDisplayName(user, user),
                          this
                  )
              );

          boolean active = equals(
              user.getCosmeticData()
                  .get(Cosmetics.LOGIN)
          );

          if (active) {
            builder
                .setFlags(ItemFlag.HIDE_ENCHANTS)
                .addEnchant(Enchantment.BINDING_CURSE, 1);
          }

          if (!hasUnlocked.test(user)) {
            builder.addLore(
                Component.text("Not unlocked",
                    NamedTextColor.RED
                )
            );
          }

          return builder.build();
        })

        .setRunnable((user, context) -> {
          if (!hasUnlocked.test(user)) {
            throw Exceptions.NOT_UNLOCKED;
          }

          var cosmetics = user.getCosmeticData();
          boolean active = equals(
              cosmetics.get(Cosmetics.LOGIN)
          );

          if (active) {
            throw Exceptions.alreadySetCosmetic(
                displayName(),
                type.getDisplayName()
            );
          }

          user.sendMessage(Messages.setCosmetic(this));
          cosmetics.set(type, this);
          context.shouldReloadMenu(true);
        })

        .build();
  }
}