package net.forthecrown.cosmetics;

import static net.forthecrown.text.Text.nonItalic;

import java.util.List;
import java.util.function.IntSupplier;
import net.forthecrown.menu.MenuNode;
import net.forthecrown.text.Text;
import net.forthecrown.utils.inventory.ItemBuilder;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;

public interface MenuNodeFactory<T> {

  static <T> MenuNodeFactory<T> defaultFactory(IntSupplier priceGetter) {
    return cosmetic -> {
      return MenuNode.builder()
          .setItem(user -> {
            CosmeticData data = user.getComponent(CosmeticData.class);
            boolean owned = cosmetic.test(user);
            boolean active = data.isActive(cosmetic);
            int price = priceGetter.getAsInt();

            Material material = Cosmetics.getCosmeticMaterial(owned);

            ItemBuilder builder = ItemStacks.builder(material)
                .setName(cosmetic.getDisplayName());

            List<Component> desc = cosmetic.getDescription();
            for (Component c : desc) {
              builder.addLoreRaw(c.style(nonItalic(NamedTextColor.GRAY)));
            }

            builder.addLoreRaw(Component.empty());

            if (!owned) {
              int gems = user.getGems();

              // If can afford
              if (gems >= price) {
                builder.addLoreRaw(
                    Text.format("Click to purchase for &6{0, gems}",
                        nonItalic(NamedTextColor.GRAY),
                        price
                    )
                );
              } else {
                builder.addLore(
                    Text.format("Cannot afford {0, gems}", NamedTextColor.RED, price)
                );
              }
            }

            if (active) {
              builder
                  .addLore(Component.text("Currently active", NamedTextColor.GREEN))
                  .addEnchant(Enchantment.CHANNELING, 1)
                  .setFlags(ItemFlag.HIDE_ENCHANTS);
            }

            return builder.build();
          })

          .setRunnable((user, click) -> {
            CosmeticData data = user.getComponent(CosmeticData.class);
            boolean owned = cosmetic.test(user);
            boolean active = data.isActive(cosmetic);

            int price = priceGetter.getAsInt();

            if (owned) {
              if (active) {
                throw CosmeticsExceptions.alreadySetCosmetic(
                    cosmetic.displayName(),
                    cosmetic.getType().getDisplayName()
                );
              }

              data.set(cosmetic.getType(), cosmetic);
              user.sendMessage(CMessages.setCosmetic(cosmetic));
            } else {
              if (user.getGems() < price) {
                user.sendMessage(
                    Text.format("Cannot afford {0, gems}",
                        NamedTextColor.RED,
                        price
                    )
                );

                return;
              }

              user.setGems(user.getGems() - price);

              data.add(cosmetic);
              data.set(cosmetic.getType(), cosmetic);

              user.sendMessage(
                  Text.format("Bought {0} for {1, gems}",
                      NamedTextColor.GRAY,

                      cosmetic.displayName(),
                      price
                  )
              );
            }

            click.shouldReloadMenu(true);
          })

          .build();
    };
  }

  MenuNode createNode(Cosmetic<T> cosmetic);
}