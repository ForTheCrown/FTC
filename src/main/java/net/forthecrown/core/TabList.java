package net.forthecrown.core;

import net.forthecrown.user.Users;
import net.forthecrown.user.property.Properties;
import net.forthecrown.utils.text.TextInfo;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang3.tuple.Triple;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import static net.forthecrown.utils.text.Text.gradient;
import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public final class TabList {
    private TabList() {}

    public static final int MIN_BORDER_PIXELS = 4 * 24; // Space character size times min amount of spaces in border

    public static final int CONNECTION_BARS_SIZE = 10;
    public static final int HEAD_TEXTURE_SIZE = 8;

    public static final int EXTRA_PIXELS = CONNECTION_BARS_SIZE + HEAD_TEXTURE_SIZE;

    public static final Component SERVER_TITLE = text()
            .style(Style.style(GOLD, TextDecoration.BOLD))
            .append(
                    gradient("For Th", YELLOW, GOLD),
                    gradient("e Crown", GOLD, YELLOW)
            )
            .build();

    public static final TextColor PLAYER_COUNT_COLOR = TextColor.fromHexString("#ffd135");

    public static final Component SCORE_FIELD = text("Score: ", GRAY);

    /**
     * Creates a header and footer.
     * <p>
     * This method returns a triple containing the following values:
     * <pre>
     * Left: The header tab list header.
     *
     * Middle: The list footer to display to admin users, or to display
     *         when there are no vanished users.
     *
     * Right: The list footer to display to regular users, this will be
     *        null, unless there are currently vanished users on the server,
     *        in which case, it'll be different from the Middle, as this
     *        doesn't include the "Online: #" count for the vanished users
     * </pre>
     *
     * This method is set up in this way to allow for the most optimized
     * way of updating the tab menu for each player. Currently, to update
     * the tab menu for all players, this method has to only be called 1,
     * instead of being called for each user separately.
     *
     * @return The formatted tab list display
     */
    public static Triple<Component, Component, Component> createHeaderFooter() {
        int largestPlayerName = 0;
        int playerCount = 0;
        int vanishedCount = 0;

        Objective displayObj = Bukkit.getScoreboardManager()
                .getMainScoreboard()
                .getObjective(DisplaySlot.PLAYER_LIST);

        for (var u: Users.getOnline()) {
            if (u.get(Properties.VANISHED)) {
                vanishedCount++;
                continue;
            }

            playerCount++;

            int playerNameSize = 0;

            if (displayObj != null) {
                int score = displayObj.getScore(u.getName())
                        .getScore();

                playerNameSize += TextInfo.getPxWidth(String.valueOf(score));
            }

            playerNameSize += TextInfo.getPxWidth(Text.plain(u.listDisplayName(true)));
            largestPlayerName = Math.max(largestPlayerName, playerNameSize);
        }

        // Max out the tab size, ensure it doesn't drop below the default
        int tabSize = Math.max(
                EXTRA_PIXELS + largestPlayerName,
                MIN_BORDER_PIXELS
        );

        // Since the border is 2 gradients placed right next to each other,
        // take the amount of border characters and divide it in half
        int borderSize = (tabSize / TextInfo.getCharPxWidth(' ')) / 2;
        String spaces = " ".repeat(borderSize);

        // Create the borderline by connecting 2 gradients
        Component border = text()
                .append(
                        gradient(spaces, YELLOW, GOLD  ),
                        gradient(spaces, GOLD,   YELLOW)
                )
                .decorate(TextDecoration.STRIKETHROUGH)
                .build();

        var header = text()
                .append(
                        space(),
                        border,
                        space(),

                        newline(),
                        SERVER_TITLE,
                        newline(),

                        space(),
                        border,
                        space()
                );

        // If we have an objective set to be displayed in
        // the tab list, display it
        if (displayObj != null) {
            header.append(
                    newline(),
                    text()
                            .color(GRAY)
                            .append(
                                    SCORE_FIELD,
                                    displayObj.displayName()
                            )
            );
        }

        var footer = createFooter(playerCount + vanishedCount);
        var vanishedFooter = vanishedCount == 0 ? null : createFooter(playerCount);

        return Triple.of(header.build(), footer, vanishedFooter);
    }

    private static Component createFooter(int count) {
        return text()
                .append(
                        newline(),

                        text("Online", GOLD, TextDecoration.BOLD),
                        text(": ", GRAY),
                        text(count, PLAYER_COUNT_COLOR),
                        text(" / " + Bukkit.getMaxPlayers(), GRAY)
                )
                .build();
    }

    public static void update() {
        // Left - Header
        // Middle - admin footer
        // Right - regular footer, null, if no vanished users
        // admin footer = Footer that includes vanished users
        var triple = createHeaderFooter();

        for (var u: Users.getOnline()) {
            if (triple.getRight() == null
                    || u.hasPermission(Permissions.VANISH_SEE)
            ) {
                u.sendPlayerListHeaderAndFooter(triple.getLeft(), triple.getMiddle());
            } else {
                u.sendPlayerListHeaderAndFooter(triple.getLeft(), triple.getRight());
            }
        }
    }
}