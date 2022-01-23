package net.forthecrown.economy.houses;

import com.google.gson.JsonElement;
import net.forthecrown.economy.guilds.VoteState;
import net.forthecrown.registry.Registries;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.JsonUtils;
import net.forthecrown.utils.TimeUtil;

public final class HouseUtil {
    private HouseUtil() {}

    public static void randomizeVoteTimes(VoteState state) {
        // Difference should be measured from an hour after the
        // vote started and to an hour before the vote ends.
        // Just in case the random number generator decides to
        // make the house vote 2 milliseconds before the vote
        // ends
        long start = state.getStarted() + TimeUtil.HOUR_IN_MILLIS;
        long end = state.getEnds() - TimeUtil.HOUR_IN_MILLIS;
        long diff = end - start;

        // Randomize vote times for all houses
        for (House h: Registries.HOUSES) {
            // HOUSES.size() here cuz idk lol, + 1 tho to ensure no
            // division by zero accidents
            h.voteTime = start + (diff / (FtcUtils.RANDOM.nextInt(Registries.HOUSES.size()) + 1));

            h.scheduleVoteTask();
        }
    }

    public static JsonElement write(House h) {
        return JsonUtils.writeKey(h.key());
    }

    public static House read(JsonElement element) {
        return Registries.HOUSES.get(JsonUtils.readKey(element));
    }
}