package net.forthecrown.cosmetics.login;

import net.forthecrown.core.Messages;
import net.forthecrown.cosmetics.Cosmetics;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.user.data.RankTier;
import net.forthecrown.utils.inventory.menu.Slot;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;

public class LoginEffects {
    private static final TextColor
            T1_COLOR = NamedTextColor.YELLOW,
            T2_COLOR = TextColor.fromHexString("#ffd82e"),
            T3_COLOR = NamedTextColor.GOLD;

    public static final LoginEffect
            TIER_1 = create("Tier 1", Slot.of(3, 1), RankTier.TIER_1, text("<", T1_COLOR), text(">", T1_COLOR)),
            TIER_2 = create("Tier 2", Slot.of(4, 1), RankTier.TIER_2, text("<<", T2_COLOR), text(">>", T2_COLOR)),
            TIER_3 = create("Tier 3", Slot.of(5, 1), RankTier.TIER_3, text("<<<", T3_COLOR), text(">>>", T3_COLOR));

    private static LoginEffect create(String name, Slot slot, RankTier tier, Component prefix, Component suffix) {
        return new LoginEffect(name, slot, tier, prefix, suffix);
    }

    public static Component createDisplayName(User user, Audience viewer) {
        LoginEffect effect = user.getCosmeticData()
                .get(Cosmetics.LOGIN);

        return createDisplayName(user, viewer, effect);
    }

    public static Component createDisplayName(User user, Audience viewer, LoginEffect effect) {
        boolean prependRank = Users.allowsRankedChat(viewer);

        if (effect == null) {
            return user.listDisplayName(prependRank)
                    .color(Messages.getJoinColor(user));
        }

        return Users.createListName(
                        user,
                        textOfChildren(
                                effect.getPrefix(),
                                user.getTabName(),
                                effect.getSuffix()
                        ),
                        prependRank
                )
                .color(Messages.getJoinColor(user));
    }
}