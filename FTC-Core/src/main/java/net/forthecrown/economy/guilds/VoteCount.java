package net.forthecrown.economy.guilds;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.translation.Translatable;
import org.jetbrains.annotations.NotNull;

public class VoteCount {
    private final int pro, against, abstentions;
    private final Result result;

    public VoteCount(int pro, int against, int abstentions) {
        this.pro = pro;
        this.against = against;
        this.abstentions = abstentions;

        // Find result
        // If no one voted, or there were more absentions
        // than actual votes, result is no_votes
        if((pro == 0 && against == 0) || (abstentions > (pro + against))) {
            this.result = Result.NO_VOTES;
            return;
        }

        if(pro == against) this.result = Result.TIE;
        else if(pro > against) this.result = Result.WIN;
        else this.result = Result.LOSE;
    }

    public boolean hasVotes() {
        return getResult() != Result.NO_VOTES;
    }

    public boolean isWin() {
        return getResult() == Result.WIN;
    }

    public Result getResult() {
        return result;
    }

    public int getAbstentions() {
        return abstentions;
    }

    public int getAgainst() {
        return against;
    }

    public int getPro() {
        return pro;
    }

    public Component shortDisplay() {
        return BallotBox.voteLine(pro, against, abstentions);
    }

    public enum Result implements Translatable, ComponentLike {
        NO_VOTES,
        WIN,
        LOSE,
        TIE;

        private final String translationKey;

        Result() {
            this.translationKey = "guilds.voteResult." + name().toLowerCase();
        }

        @Override
        public @NotNull String translationKey() {
            return translationKey;
        }

        @Override
        public @NotNull Component asComponent() {
            return Component.translatable(this.translationKey());
        }
    }
}
