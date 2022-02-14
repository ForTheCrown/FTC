package net.forthecrown.core;

import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.Nameable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Represents the current king or queen on the server
 * <p></p>
 * Implementation: {@link FtcKingship}
 */
public interface Kingship extends Nameable {
    Component
            QUEEN_TITLE = makeTitle("Queen"),
            KING_TITLE  = makeTitle("King");

    private static Component makeTitle(String text) {
        return Component.text("[")
                .color(NamedTextColor.WHITE)
                .append(Component.text(text).color(NamedTextColor.YELLOW))
                .append(Component.text("] "))
                .decorate(TextDecoration.BOLD);
    }

    boolean hasKing();

    UUID getUniqueId();
    void set(@Nullable UUID uuid);

    boolean isFemale();

    /**
     * 
     * @param female
     */
    void setFemale(boolean female);

    /**
     * Gets the king's user object
     * @return King's user object, null, if there's no king
     */
    CrownUser getUser();

    /**
     * Gets the current monarch's title.
     * @return {@link Kingship#QUEEN_TITLE} if {@link Kingship#isFemale()} is true, {@link Kingship#KING_TITLE} if false
     */
    default Component getPrefix() {
        return isFemale() ? QUEEN_TITLE : KING_TITLE;
    }
}