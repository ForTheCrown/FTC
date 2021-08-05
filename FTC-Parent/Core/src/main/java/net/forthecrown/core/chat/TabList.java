package net.forthecrown.core.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Represents the TAB menu list.
 * <p></p>
 * Implementation: {@link CrownTabList}
 */
public interface TabList {
    Component SERVER_TITLE = ChatUtils.convertString("&e-&#fff147-&#ffcd00-&#ffb107&l[&6&lFor The Crown&#ffb107&l]&#ffcd00-&#fff147-&e-", true);
    Component SCORE_FIELD = Component.text("Score: ").color(NamedTextColor.GRAY);

    Component format();

    void setScore(Component score);
    Component currentScore();

    void updateList();
}
