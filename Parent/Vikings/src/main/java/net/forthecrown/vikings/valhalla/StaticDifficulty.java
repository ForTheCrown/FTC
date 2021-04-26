package net.forthecrown.vikings.valhalla;

public enum StaticDifficulty {
    EASY ( 0.75f),
    NORMAL (1f),
    HARD (1.5f),
    VERY_HARD ( 2f);

    private final float modifier;
    StaticDifficulty(float modifier){
        this.modifier = modifier;
    }

    public float getModifier() {
        return modifier;
    }
}
