package net.forthecrown.cosmetics.login;

import lombok.Getter;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.Messages;
import net.forthecrown.cosmetics.Cosmetic;
import net.forthecrown.cosmetics.Cosmetics;
import net.forthecrown.user.data.RankTier;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.inventory.menu.Slot;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;

@Getter
public class LoginEffect extends Cosmetic {
    private final RankTier tier;
    private final Component prefix, suffix;

    public LoginEffect(String name, Slot slot, RankTier tier, Component prefix, Component suffix) {
        super(name, Cosmetics.LOGIN, slot);
        this.tier = tier;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    @Override
    public MenuNode createNode() {
        return MenuNode.builder()
                .setItem((user, context) -> {
                    var builder = ItemStacks.builder(
                            displayData.getMaterial(user.getTitles().hasTier(tier))
                    )
                            .setName(displayData.getItemDisplayName())
                            .addLore("&7Join/Leave decoration")
                            .addLore("&7Example:")
                            .addLore(
                                    Messages.joinMessage(
                                            LoginEffects.createDisplayName(
                                                    user, user, this
                                            )
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

                    if (!user.getTitles().hasTier(tier)) {
                        builder.addLore(
                                Component.text("Requires " + Text.prettyEnumName(tier),
                                        NamedTextColor.RED
                                )
                        );
                    }

                    return builder.build();
                })

                .setRunnable((user, context) -> {
                    if (!user.getTitles().hasTier(tier)) {
                        throw Exceptions.DONT_HAVE_TIER;
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