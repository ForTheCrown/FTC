package net.forthecrown.vikings.raids;

import javax.annotation.Nonnegative;

//Class existence reason: provide a simple difficulty modifier for raids
public enum RaidDifficulty {

    //Thought adding this to make the Raids more dynamic would be a good idea
    //Maybe, if I or we have the time, we could make the difficulty be assigned
    //automatically based off of previous performance
    EASY (0.5f),
    NORMAL (1.0f),
    HARD (1.5f),
    VERY_HARD (2f);

    public final float modifier;
    RaidDifficulty(float healthModifier){
        this.modifier = healthModifier;
    }

    //If we use the same GUI like we did for the GrapplingHooks, we might need something to get an int from the level's name
    //and translate it into the difficulty
    //Or they get to choose it, in which case this might still help lol
    public static RaidDifficulty getFromNumber(@Nonnegative int i){
        if(i >= 3) return VERY_HARD;
        if(i <= 0) return EASY;
        if(i == 1) return NORMAL;
        return HARD;
    }
}
