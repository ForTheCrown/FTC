package net.forthecrown.user;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.guilds.Guild;
import net.forthecrown.utils.text.TextInfo;
import net.forthecrown.core.Messages;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.economy.Economy;
import net.forthecrown.economy.market.MarketDisplay;
import net.forthecrown.economy.market.Markets;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.user.data.RankTitle;
import net.forthecrown.user.data.TimeField;
import net.forthecrown.user.data.UserInteractions;
import net.forthecrown.user.data.UserTitles;
import net.forthecrown.user.property.Properties;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.format.PeriodFormat;
import net.forthecrown.utils.text.format.UnitFormat;
import net.forthecrown.utils.text.writer.TextWriter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;

import static net.kyori.adventure.text.Component.text;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UserFormat {
    /* ----------------------------- CONSTANTS ------------------------------ */

    private static final Component BORDER = text("        ",
            NamedTextColor.GOLD, TextDecoration.STRIKETHROUGH
    );

    public static final int
            ADMIN_VIEWER = 0x1,
            SELF = 0x2,
            PUBLIC_PROFILE = 0x4,
            NO_HOVER_EVENTS = 0x8,
            FOR_HOVER = 0x10;

    /* ----------------------------- INSTANCE FIELDS ------------------------------ */

    private final User user;
    private final int flags;

    private int headerSize;

    /* ----------------------------- STATIC CONSTRUCTOR ------------------------------ */

    public static UserFormat create(User user) {
        return new UserFormat(user, privacyFlag(user));
    }

    /* ----------------------------- FLAG TESTING ------------------------------ */

    public boolean isViewingAllowed() {
        return hasFlag(SELF) || hasFlag(ADMIN_VIEWER) || hasFlag(PUBLIC_PROFILE);
    }

    public boolean hasFlag(int flags) {
        return (this.flags & flags) == flags;
    }

    /* ----------------------------- CLONE METHODS ------------------------------ */

    public UserFormat with(int flags) {
        return hasFlag(flags) ? this : new UserFormat(user, flags | this.flags);
    }

    public UserFormat withViewer(CommandSource source) {
        UserFormat format = this;

        if (source.textName().equals(getUser().getName())) {
            format = format.with(SELF);
        }

        if (source.hasPermission(Permissions.PROFILE_BYPASS)) {
            format = format.with(ADMIN_VIEWER);
        }

        return format;
    }

    public UserFormat disableHover() {
        return with(NO_HOVER_EVENTS);
    }

    /* ----------------------------- FORMATTING ------------------------------ */

    public void format(TextWriter writer) {
        if (hasFlag(FOR_HOVER)) {
            writer.line(user.getTabName().colorIfAbsent(NamedTextColor.GOLD));
        } else {
            writeHeader(writer);
        }

        writeContents(writer);

        if (hasFlag(ADMIN_VIEWER)) {
            writeAdminInfo(writer);
        }

        if (hasFlag(FOR_HOVER)) {
            writer.field("ID", user.getUniqueId());
        } else {
            writeFooter(writer);
        }
    }

    private void writeHeader(TextWriter writer) {
        Component profileTitle = hasFlag(SELF) ?
                Messages.YOUR : Text.format("{0}'s", user.getTabName());

        Component header = Text.format("{0} {1} profile {0}",
                NamedTextColor.YELLOW,
                BORDER, profileTitle
        );

        this.headerSize = TextInfo.getPxWidth(Text.plain(header));
        writer.line(header);
    }

    private void writeFooter(TextWriter writer) {
        int spaceSize = TextInfo.getCharPxWidth(' ') + 1;
        int requiredCharacters = headerSize / spaceSize;

        writer.line(" ".repeat(requiredCharacters), BORDER.style());
    }

    private void writeContents(TextWriter writer) {
        UserInteractions inter = user.getInteractions();
        UserTitles titles = user.getTitles();
        var time = user.getTimeTracker();

        // Real name if nickname or TAB_NAME is set
        if (user.hasNickname()
                || user.getProperties().contains(Properties.TAB_NAME)
        ) {
            writer.field("Name", user.getName());
        }

        // profile public state
        if (!hasFlag(PUBLIC_PROFILE) || hasFlag(SELF)) {
            writer.field("Profile public", !user.get(Properties.PROFILE_PRIVATE));
        }

        // Last login date, if offline
        if (!user.isOnline() && time.isSet(TimeField.LAST_LOGIN)) {
            writer.field("Last online",
                    PeriodFormat.timeStamp(time.get(TimeField.LAST_LOGIN))
                            .retainBiggest()
            );
            writer.write(" ago", writer.getFieldValueStyle());
        }

        // First join date
        if (time.isSet(TimeField.FIRST_JOIN)) {
            writer.field("First joined",
                    Text.formatDate(time.get(TimeField.FIRST_JOIN))
            );
        }

        // AFK reason
        if (user.isAfk()
                && user.getAfkReason() != null
        ) {
            writer.field("AFK", user.getAfkReason());
        }

        // Guild info
        Guild guild = user.getGuild();
        if (guild != null) {
            writer.field("Guild", guild.displayName());
        }

        // Spouse's displayName
        if (inter.isMarried()) {
            Component spouseDisplayName;

            // tabName just returns the name to display, while
            // display name returns a fully formatted display name.
            if (hasFlag(NO_HOVER_EVENTS)) {
                spouseDisplayName = inter.spouseUser().getTabName();
            } else {
                spouseDisplayName = inter.spouseUser().displayName();
            }

            writer.field("Spouse", spouseDisplayName);
        }

        if (titles.getTitle() != RankTitle.DEFAULT) {
            writer.field("Rank", titles.getTitle());
        }

        writeMapsInfo(writer);
    }

    private void writeMapsInfo(TextWriter writer) {
        UserManager manager = UserManager.get();
        int rhines = user.getBalance();

        if (rhines != GeneralConfig.startRhines) {
            writer.field("Rhines", UnitFormat.rhines(rhines));
        }

        // Rest is private if so wished, so don't write
        if (!hasFlag(PUBLIC_PROFILE) && !hasFlag(SELF)) {
            return;
        }

        int gems = user.getGems();
        int votes = manager.getVotes().get(user.getUniqueId());

        if (gems > 0) {
            writer.field("Gems", UnitFormat.gems(gems));
        }

        writer.field("Playtime",
                UnitFormat.playTime(
                        manager.getPlayTime()
                                .get(user.getUniqueId())
                )
        );

        if (votes > 0) {
            writer.field("Votes", UnitFormat.votes(votes));
        }
    }

    private void writeAdminInfo(TextWriter writer) {
        writer.newLine();
        writer.newLine();
        writer.field("Admin Info", "");

        long lastLogin = user.getLastLogin();
        var joinFormat = PeriodFormat.timeStamp(lastLogin)
                .asComponent()
                .hoverEvent(Text.formatDate(lastLogin));

        if (user.isOnline()) {
            writer.field("Joined", joinFormat);
        } else {
            writer.field("Last joined", joinFormat);
        }
        writer.write(" ago", writer.getFieldValueStyle());

        if (!Strings.isNullOrEmpty(user.getIp())) {
            writer.field("IP", user.getIp());
        }

        if (!user.getPreviousNames().isEmpty()) {
            writer.field("Previous names", Joiner.on(", ").join(user.getPreviousNames()));
        }

        if (user.getReturnLocation() != null) {
            writer.field("Return", Text.clickableLocation(user.getReturnLocation(), true));
        }

        if (user.getLocation() != null) {
            writer.field("Location", Text.clickableLocation(user.getLocation(), true));
        }

        UserInteractions inter = user.getInteractions();

        if (!inter.getBlocked().isEmpty()) {
            writer.field("Blocked", hoverToSee(
                    Messages.listBlocked(inter.getBlocked())
            ));
        }

        if (!inter.getSeparated().isEmpty()) {
            writer.field("Separated", hoverToSee(
                    Messages.joinIds(inter.getSeparated(), text("Blocked players: "))
            ));
        }

        if (Markets.ownsShop(user)) {
            writer.field("Owned Shop",
                    MarketDisplay.displayName(
                            Economy.get()
                                    .getMarkets()
                                    .get(user.getUniqueId())
                    )
            );
        }
    }

    public static void applyProfileStyle(TextWriter writer) {
        writer.setFieldStyle(Style.style(NamedTextColor.YELLOW));
        writer.setFieldValueStyle(Style.style(NamedTextColor.WHITE));

        writer.setFieldSeparator(Component.text(": ", NamedTextColor.YELLOW));
    }

    private static int privacyFlag(User user) {
        return user.get(Properties.PROFILE_PRIVATE) ? 0 : PUBLIC_PROFILE;
    }

    private static Component hoverToSee(Component text) {
        return text("[Hover to see]", NamedTextColor.AQUA)
                .hoverEvent(text);
    }
}