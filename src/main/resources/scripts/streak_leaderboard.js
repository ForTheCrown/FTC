// Imports
const ArmorStandLeaderboard = Java.type("net.forthecrown.utils.stand.ArmorStandLeaderboard");
const LineFormatter = Java.type("net.forthecrown.utils.stand.ArmorStandLeaderboard.LineFormatter");
const UnitFormat = Java.type("net.forthecrown.utils.text.format.UnitFormat");
const Worlds = Java.type("net.forthecrown.core.Worlds");
const EventPrio = Java.type("org.bukkit.event.EventPriority");
const StreakCategory = Java.type("net.forthecrown.core.challenge.StreakCategory");
const StreakIncreaseEvent = Java.type("net.forthecrown.core.challenge.StreakIncreaseEvent");
const Challenges = Java.type("net.forthecrown.core.challenge.Challenges");
const C_Manager = Java.type("net.forthecrown.core.challenge.ChallengeManager");

// No streak constant, just 0 lol
const NO_STREAK = 0;

// Leaderboard
const leaderboard = new ArmorStandLeaderboard(
        new Location(Worlds.overworld(), 210.5, 72.65, 195.5)
);

// Seperation between header/footer and entries
leaderboard.setHeaderSeparation(0.1);
leaderboard.setFooterSeparation(0.1);

// Set header
//leaderboard.addHeader(Component.text("Top streaks:", NamedTextColor.YELLOW));

// Set entry formatter
leaderboard.setLineFormatter((index, entry, score) => {
    return Text.format("&e{0}) &r{1, user}: &e{2}",
                       index,
                       entry,
                       UnitFormat.unit(score, "Day")
    );
});
leaderboard.setMaxSize(3);

scanStreaks();
leaderboard.spawn();

events.register("onStreakIncrease", StreakIncreaseEvent)

function onStreakIncrease(/* StreakIncreaseEvent */ event) {
    if (event.getCategory() != StreakCategory.ITEMS) {
        return;
    }

    leaderboard.getValues().put(event.getEntry().getId(), event.getStreak());
    leaderboard.spawn();
}

function scanStreaks() {
    const entries = C_Manager.getInstance().getEntries();

    entries.forEach(entry => {
        let streak = Challenges.queryStreak(StreakCategory.ITEMS, entry.getUser()).orElse(0);

        if (streak == NO_STREAK) {
            return;
        }

        leaderboard.getValues().put(entry.getId(), streak);
    });
}

function __onClose() {
    leaderboard.kill();
}

// Update streaks on day change
function __onDayChange(time) {
    leaderboard.kill();
    leaderboard.getValues().clear();

    scanStreaks();
    leaderboard.spawn();
}