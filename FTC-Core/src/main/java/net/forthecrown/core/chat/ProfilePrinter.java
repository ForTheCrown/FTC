package net.forthecrown.core.chat;

import net.forthecrown.core.ComVars;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.admin.PunishmentEntry;
import net.forthecrown.core.admin.Punishments;
import net.forthecrown.economy.market.MarketDisplay;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.user.*;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.ListUtils;
import net.forthecrown.utils.TimeUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Statistic;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

import java.util.function.Function;

public class ProfilePrinter implements ComponentPrinter {
    private final FtcUser user;
    private final boolean self;
    private final boolean adminViewer;
    private final boolean profilePublic;

    private final TextComponent.Builder builder;
    private int headerLength;

    private static final Style BORDER_STYLE = Style.style(NamedTextColor.GOLD, TextDecoration.STRIKETHROUGH);
    private static final Style LINE_STYLE = Style.style(NamedTextColor.YELLOW);

    public ProfilePrinter(CrownUser user, CommandSource source) {
        this(
                user,
                source.textName().equalsIgnoreCase(user.getName()),
                source.hasPermission(Permissions.PROFILE_BYPASS)
        );
    }

    public ProfilePrinter(CrownUser user, boolean self, boolean adminViewer) {
        this.user = (FtcUser) user;
        this.self = self;
        this.adminViewer = adminViewer;
        this.profilePublic = user.isProfilePublic();

        builder = Component.text();
    }

    public boolean isViewingAllowed() {
        return self || adminViewer || profilePublic;
    }

    public Component printFull() {
        header();
        optionalInfo();
        basicInfo();

        if(adminViewer) adminInfo();

        footer();
        return printCurrent();
    }

    public Component printForHover() {
        append(user.nickOrName().color(NamedTextColor.GOLD));

        if(profilePublic) {
            optionalInfo();
            basicInfo();
        }

        if(adminViewer) adminInfo();

        line("ID", user.getUniqueId().toString());
        return printCurrent();
    }

    public ProfilePrinter header() {
        String header = "---- " + (self ? "Your" : (user.getNickOrName() + "'s")) + " profile ----";
        headerLength = header.length();

        append(
                Component.text("    ")
                        .style(BORDER_STYLE)
        );

        append(Component.space());
        append(headerDisplay());
        append(Component.space());

        append(
                Component.text("    ")
                        .style(BORDER_STYLE)
        );

        return this;
    }

    public ProfilePrinter footer() {
        newLine();
        return append(
                Component.text(" ".repeat(headerLength < 1 ? 10 : headerLength))
                        .style(BORDER_STYLE)
        );
    }

    public ProfilePrinter basicInfo() {
        line("Rank", user.getTitle().truncatedPrefix(), user.getTitle() != RankTitle.DEFAULT);

        if(!user.isOnline()) {
            long offlineTime = TimeUtil.timeSince(user.getLastOnline());
            TimePrinter printer = new TimePrinter(offlineTime);

            line("Last online", printer.printStringBiggest() + " ago");
        }

        line("Gems", FtcFormatter.gemsNonTrans(user.getGems()), user.getGems() > 0);
        line("Rhines", FtcFormatter.rhines(Crown.getEconomy().get(user.getUniqueId())));

        return this;
    }

    public ProfilePrinter optionalInfo() {
        line("AFK", user.getAfkReason(), user.isAfk());
        line("Play time", FtcFormatter.decimalizeNumber(playTime()) + " hours");
        line("Married to", marriedMessage());

        Objective crown = user.getScoreboard().getObjective("crown");
        Score crownScore = crown.getScore(user.getName());
        line("Crown score",
                Component.text(ComVars.isEventTimed() ? timer(crownScore.getScore()) : crownScore.getScore() + ""),
                crownScore.getScore() > 0 && ComVars.isEventActive()
        );

        return this;
    }

    private String timer(long timeInMillis) {
        long minutes = (timeInMillis / 60000) % 60;
        long seconds = (timeInMillis / 1000) % 60;
        long milliseconds = (timeInMillis /10) % 100;

        return new StringBuilder()
                .append(String.format("%02d", minutes)).append(":")
                .append(String.format("%02d", seconds)).append(":")
                .append(String.format("%02d", milliseconds))
                .toString();
    }

    public ProfilePrinter adminInfo() {
        Punishments list = Crown.getPunishments();
        PunishmentEntry entry = list.getEntry(user.getUniqueId());

        Component locMessage = user.getLocation() == null ? null : FtcFormatter.clickableLocationMessage(user.getLocation(), true);
        Component punishmentDisplay = entry == null ? null : Component.newline().append(entry.display(false));

        Component ignored = user.interactions.blocked.isEmpty() ?
                null :
                Component.text(ListUtils.join(user.interactions.blocked, id -> UserManager.getUser(id).getName()));

        Component separated = user.interactions.separated.isEmpty() ?
                null :
                Component.text(ListUtils.join(user.interactions.separated, id -> UserManager.getUser(id).getName()));

        append(Component.text("\n\n"));
        append("&eAdmin Info:");

        line(" Titles", ListUtils.join(user.getAvailableTitles(), r -> r.name().toLowerCase()));

        newLine();
        onlineTimeThing();

        line(" IP", user.ip);
        line(" PreviousNames", user.previousNames.isEmpty() ? null : ListUtils.join(user.previousNames, Function.identity()));

        line(" Ignored", ignored);
        line(" Separated", separated);

        line(" OwnedShop",
                user.marketOwnership.currentlyOwnsShop() ?
                        MarketDisplay.displayName(Crown.getMarkets().get(user.getUniqueId())) :
                        null
        );

        line(" MarriageCooldown", marriageCooldown());
        line(" Location", locMessage, user.isOnline());

        append(punishmentDisplay);
        return this;
    }

    public ProfilePrinter newLine() {
        return append(Component.newline());
    }

    public ProfilePrinter append(Component text) {
        if(text != null) builder.append(text);
        return this;
    }

    public ProfilePrinter append(String text) {
        return append(FtcUtils.isNullOrBlank(text) ? null : ChatUtils.convertString(text));
    }

    public ProfilePrinter line(String line, Component text) {
        return line(line, text, text != null);
    }

    public ProfilePrinter line(String line, String text) {
        return line(line, text == null ? null : ChatUtils.convertString(text));
    }

    public ProfilePrinter line(String line, String text, boolean shouldInclude) {
        return line(line, text == null ? null : ChatUtils.convertString(text), shouldInclude);
    }

    public ProfilePrinter line(String line, Component text, boolean shouldInclude) {
        if(!shouldInclude || text == null) return this;

        newLine();
        append(Component.text(line + ": ").style(LINE_STYLE));
        return append(text);
    }

    private long playTime() {
        return user.getOfflinePlayer().getStatistic(Statistic.PLAY_ONE_MINUTE)/20/60/60;
    }

    private Component marriedMessage(){
        if(user.getInteractions().getSpouse() == null) return null;

        return user.getInteractions().spouseUser().nickOrName();
    }

    private Component headerDisplay() {
        Component result;

        if(self) result = Component.translatable("user.profile.header.your");
        else {
            result = Component.translatable(
                    "user.profile.header",
                    user.nickDisplayName()
            );
        }

        return result.style(LINE_STYLE);
    }

    private void onlineTimeThing() {
        long lastOnline = user.getLastOnline();
        Component time = new TimePrinter(TimeUtil.timeSince(lastOnline))
                .print()
                .color(NamedTextColor.WHITE)
                .hoverEvent(FtcFormatter.formatDate(lastOnline));

        append(
                Component.text(" Has been " + (user.isOnline() ? "online" : "offline") + " for ")
                        .color(NamedTextColor.YELLOW)
                        .append(time)
        );
    }

    private Component marriageCooldown() {
        UserInteractions interactions = user.getInteractions();

        if(interactions.canChangeMarriageStatus()) return null;

        long time = interactions.getLastMarriageChange();
        return FtcFormatter.formatDate(time);
    }

    @Override
    public Component print() {
        return printFull();
    }

    public Component printCurrent() {
        return builder.build();
    }

    public boolean isAdminViewer() {
        return adminViewer;
    }

    public boolean isProfilePublic() {
        return profilePublic;
    }

    public boolean isSelf() {
        return self;
    }

    public CrownUser getUser() {
        return user;
    }
}
