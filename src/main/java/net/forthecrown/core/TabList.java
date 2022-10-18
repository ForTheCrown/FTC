package net.forthecrown.core;

import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.user.property.Properties;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.DisplaySlot;

import static net.forthecrown.text.Text.gradient;
import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public final class TabList {
    private TabList() {}

    private static final Component BORDER = text()
            .append(
                    space(),
                    text()
                            .append(
                                    gradient("            ", YELLOW, GOLD),
                                    gradient("            ", GOLD, YELLOW)
                            ).decorate(TextDecoration.STRIKETHROUGH)
                            .build(),
                    space()
            )
            .build();

    public static final Component SERVER_TITLE = text()
            .style(Style.style(GOLD, TextDecoration.BOLD))
            .append(
                    BORDER, newline(),

                    gradient("For Th", GOLD, YELLOW),
                    gradient("e Crown", YELLOW, GOLD),

                    newline(), BORDER
            )
            .build();

    public static final TextColor PLAYER_COUNT_COLOR = TextColor.fromHexString("#ffd135");

    public static final Component SCORE_FIELD = text("Score: ", GRAY);

    public static Component createHeader() {
        var board = Bukkit.getScoreboardManager().getMainScoreboard();
        var score = board.getObjective(DisplaySlot.PLAYER_LIST);
        var scoreText = score == null ? null : score.displayName();

        var builder = text()
                .append(SERVER_TITLE);

        if (scoreText != null) {
            builder.append(
                    newline(),

                    text()
                            .color(GRAY)
                            .append(
                                    SCORE_FIELD,
                                    scoreText
                            )
            );
        }

        return builder.build();
    }

    public static Component createFooter(User user) {
        int onlineCount = 0;
        final boolean seeVanished = user.hasPermission(Permissions.VANISH_SEE);

        for (var u: Users.getOnline()) {
            if (!seeVanished && u.get(Properties.VANISHED)) {
                continue;
            }

            onlineCount++;
        }

        return text()
                .append(
                        newline(),

                        text("Online", GOLD, TextDecoration.BOLD),
                        text(": ", GRAY),
                        text(onlineCount, PLAYER_COUNT_COLOR),
                        text(" / " + Bukkit.getMaxPlayers(), GRAY)
                )
                .build();
    }

    public static void update() {
        Component formatted = createHeader();

        for (User u: Users.getOnline()) {
            u.sendPlayerListHeaderAndFooter(formatted, createFooter(u));
        }
    }
}