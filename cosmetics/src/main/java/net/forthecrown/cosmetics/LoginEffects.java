package net.forthecrown.cosmetics;

import static net.forthecrown.cosmetics.Cosmetic.create;
import static net.kyori.adventure.text.Component.text;

import net.forthecrown.command.Exceptions;
import net.forthecrown.menu.MenuNode;
import net.forthecrown.menu.Slot;
import net.forthecrown.registry.Registry;
import net.forthecrown.text.Messages;
import net.forthecrown.titles.RankTier;
import net.forthecrown.user.User;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;

public final class LoginEffects {
  private LoginEffects() {}

  public static final CosmeticType<LoginEffect> TYPE = CosmeticType.<LoginEffect>builder()
      .displayName(text("Login Effects"))
      .factory(createFactory())
      .predicate((user, cosmetic) -> cosmetic.getValue().predicate().test(user))
      .build();

  private static final TextColor
      FR_COLOR = NamedTextColor.GRAY,
      T1_COLOR = NamedTextColor.YELLOW,
      T2_COLOR = NamedTextColor.GOLD,
      T3_COLOR = TextColor.fromHexString("#fe771c"),
      BOOSTER_COLOR = TextColor.fromHexString("#be91fd");

  public static final LoginEffect EFFECT_FREE
      = new LoginEffect(RankTier.FREE, text(">", FR_COLOR), text("<", FR_COLOR));

  public static final LoginEffect EFFECT_T1
      = new LoginEffect(RankTier.TIER_1, text(">", T1_COLOR), text("<", T1_COLOR));

  public static final LoginEffect EFFECT_T2
      = new LoginEffect(RankTier.TIER_2, text(">", T2_COLOR), text("<", T2_COLOR));

  public static final LoginEffect EFFECT_T3
      = new LoginEffect(RankTier.TIER_3, text(">", T3_COLOR), text("<", T3_COLOR));

  public static final LoginEffect EFFECT_BOOSTER
      = new LoginEffect("booster", text(">", BOOSTER_COLOR), text("<", BOOSTER_COLOR));

  public static final Cosmetic<LoginEffect> FREE
      = create(EFFECT_FREE, Slot.of(2, 1).getIndex(), "Free Rank");

  public static final Cosmetic<LoginEffect> TIER_1
      = create(EFFECT_T1, Slot.of(3, 1).getIndex(), "Tier 1");

  public static final Cosmetic<LoginEffect> TIER_2
      = create(EFFECT_T2, Slot.of(4, 1).getIndex(), "Tier 2");

  public static final Cosmetic<LoginEffect> TIER_3
      = create(EFFECT_T3, Slot.of(5, 1).getIndex(), "Tier 3");

  public static final Cosmetic<LoginEffect> BOOSTER
      = create(EFFECT_BOOSTER, Slot.of(6, 1).getIndex(), "Booster");

  public static void registerAll(Registry<Cosmetic<LoginEffect>> r) {
    r.register("free_rank", FREE);
    r.register("tier_1", TIER_1);
    r.register("tier_2", TIER_2);
    r.register("tier_3", TIER_3);
    r.register("booster", BOOSTER);
  }

  public static Component getDisplayName(LoginEffect effect, User user, Audience viewer) {
    return Component.text()
        .append(effect.prefix())
        .append(user.displayName(viewer))
        .append(effect.suffix())
        .build();
  }

  private static MenuNodeFactory<LoginEffect> createFactory() {
    return cosmetic -> {
      return MenuNode.builder()
          .setItem((user, context) -> {
            var builder = ItemStacks.builder(Cosmetics.getCosmeticMaterial(cosmetic.test(user)))
                .setName(cosmetic.getDisplayName())
                .addLore("&7Join/Leave decoration")
                .addLore("&7Example:")
                .addLore(Messages.joinMessage(getDisplayName(cosmetic.getValue(), user, user)));

            boolean active = cosmetic.equals(user.getComponent(CosmeticData.class).get(TYPE));

            if (active) {
              builder.setFlags(ItemFlag.HIDE_ENCHANTS).addEnchant(Enchantment.BINDING_CURSE, 1);
            }

            if (!cosmetic.test(user)) {
              builder.addLore(Component.text("Not unlocked", NamedTextColor.RED));
            }

            return builder.build();
          })

          .setRunnable((user, context) -> {
            if (!cosmetic.test(user)) {
              throw Exceptions.NOT_UNLOCKED;
            }

            var cosmetics = user.getComponent(CosmeticData.class);
            boolean active = cosmetic.equals(cosmetics.get(TYPE));

            if (active) {
              throw CosmeticsExceptions.alreadySetCosmetic(
                  cosmetic.displayName(),
                  cosmetic.getType().getDisplayName()
              );
            }

            user.sendMessage(CMessages.setCosmetic(cosmetic));
            cosmetics.set(TYPE, cosmetic);
            context.shouldReloadMenu(true);
          })

          .build();
    };
  }
}