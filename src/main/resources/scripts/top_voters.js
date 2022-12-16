// Imports
const ArmorStandLeaderboard = Java.type("net.forthecrown.utils.stand.ArmorStandLeaderboard");
const LineFormatter = Java.type("net.forthecrown.utils.stand.ArmorStandLeaderboard.LineFormatter");
const UnitFormat = Java.type("net.forthecrown.utils.text.format.UnitFormat");
const Worlds = Java.type("net.forthecrown.core.Worlds");
const U_Manager = Java.type("net.forthecrown.user.UserManager");
const EventPrio = Java.type("org.bukkit.event.EventPriority");
const Runnable = Java.type("java.lang.Runnable");
const PostVoteEvent = Java.type("com.bencodez.votingplugin.events.PlayerPostVoteEvent");


// Leaderboard
const leaderboard = new ArmorStandLeaderboard(
        new Location(Worlds.voidWorld(), 21.5, 55.50, -109.5)
);
// Seperation between header/footer and entries
leaderboard.setHeaderSeparation(0.1);
leaderboard.setFooterSeparation(0.1);

// Set header
leaderboard.addHeader(Component.text("All time top voters:", NamedTextColor.AQUA));

// Set entry formatter
leaderboard.setLineFormatter((index, entry, score) => {
    return Text.format("&e{0}) &r{1, user}: &6{2}",
        index,
        entry,
        UnitFormat.votes(score)
    );
});
leaderboard.setMaxSize(10);

// Initialize starting values
updateFromUserMap();
leaderboard.spawn();

// Listen to vote events to know when to update map
events.register("onVote", PostVoteEvent, EventPrio.MONITOR);

// Update leaderboard values by copying values from vote map
function updateFromUserMap() {
    let voteMap = U_Manager.get().getVotes();

    voteMap.forEach(entry => {
        leaderboard.getValues().put(entry.getUniqueId(), entry.getValue())
    });
}

function onVote(/* PlayerPostVoteEvent */ event) {
    // Kill, and then respawn the leaderboard
    scheduler.run(new Runnable() {
        run: function() {
            // Clear existing values in the map and then
            // re add the values from the user map
            leaderboard.getValues().clear();
            updateFromUserMap();

            leaderboard.spawn();
        }
    });
}

function __onClose() {
    // Kill the leaderboard if the script is being closed
    leaderboard.kill();
}