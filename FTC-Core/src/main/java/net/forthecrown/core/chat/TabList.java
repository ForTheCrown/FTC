package net.forthecrown.core.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Represents the TAB menu list.
 * <p></p>
 * Implementation: {@link FtcTabList}
 */
public interface TabList {
    Component SERVER_TITLE = Component.text()
            .style(Style.style(NamedTextColor.GOLD, TextDecoration.BOLD))

            .append(FtcFormatter.gradientText("---[F", NamedTextColor.YELLOW, NamedTextColor.GOLD))
            .append(Component.text("or The Crow"))
            .append(FtcFormatter.gradientText("n]---", NamedTextColor.GOLD, NamedTextColor.YELLOW))

            .build();

    Component SCORE_FIELD = Component.text("Score: ").color(NamedTextColor.GRAY);

    Component format();

    void setScore(Component score);
    Component currentScore();

    void updateList();
}
