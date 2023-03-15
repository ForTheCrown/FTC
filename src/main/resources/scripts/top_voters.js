// Imports
import "@ftc.utils.stand.ArmorStandLeaderboard";
import "@ftc.utils.stand.ArmorStandLeaderboard.LineFormatter";
import "@ftc.utils.text.format.UnitFormat";
import "@ftc.core.Worlds";
import "@ftc.user.UserManager";
import "@bukkit.event.EventPriority";
import "@jlang.Runnable";
import "com.bencodez.votingplugin.events.PlayerPostVoteEvent"

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
events.register(PlayerPostVoteEvent.class, onVote, EventPriority.MONITOR);

// Update leaderboard values by copying values from vote map
function updateFromUserMap() {
    let voteMap = UserManager.get().getVotes();

    voteMap.forEach(entry => {
        leaderboard.getValues().put(entry.getUniqueId(), entry.getValue())
    });
}

function onVote(/* PlayerPlayerPostVoteEvent */ event) {
    // Kill, and then respawn the leaderboard
    scheduler.run(task => {
        // Clear existing values in the map and then
        // re add the values from the user map
        leaderboard.getValues().clear();
        updateFromUserMap();

        leaderboard.spawn();
    });
}

function __onClose() {
    // Kill the leaderboard if the script is being closed
    leaderboard.kill();
}