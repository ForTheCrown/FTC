package net.forthecrown.dungeons.boss.evoker;

import net.forthecrown.core.Crown;
import net.forthecrown.vars.Var;

@Var(namePrefix = "evoker_")
public final class EvokerVars {
    private EvokerVars() {}

    // --- PREFIX CONSTANTS ---

    private static final String PREFIX_EFFECT = "effect_";
    private static final String PREFIX_PHASE = "phase_";

    // --- GENERAL VARS ---

    public static double
            deathPhaseHealth                = 10.0D;

    public static int
            rhineReward                     = 25000,
            baseHealth                      = 400,
            ticksBetweenSpawns              = 7,
            deathAnimLength                 = 40,
            phaseTransition                 = 5 * 20;

    public static String
            bossTeam                        = "DungeonEvokerBoss",
            mobTeam                         = "DungeonMobTeam";

    // --- PHASES ---

    @Var(namePrefix = PREFIX_PHASE)
    public static double
            shulker_particleDistance  = 0.2D,

            potion_minDist            = 4.0D,
            potion_maxDist            = 15.0D,
            potion_spawnY             = 40.5D,

            shulker_firingSpeed       = 0.75D;

    @Var(namePrefix = PREFIX_PHASE)
    public static int
            normal_length             = 400,

            zombies_skeletonChance    = 5,
            illager_ravagerChance     = 1,
            vulnerable_length         = phaseTransition * 2,

            shulker_drawInterval      = 15,
            shulker_aimingTime        = 15,
            shulker_aimInterval       = 60,

            potion_throwInterval      = 20,
            potion_length             = 140,
            potion_spawnDelay         = 5,

            ghast_length              = 400,
            ghast_health              = 2000;


    // --- EFFECTS ---

    @Var(namePrefix = PREFIX_EFFECT)
    public static short
            impact_stepParticles = 7;

    @Var(namePrefix = PREFIX_EFFECT)
    public static int
            pushAway_radius = 3,
            impact_stepCount = 5;

    @Var(namePrefix = PREFIX_EFFECT)
    public static double
            impact_radiusStart = 1.25D,
            impact_radiusStep = 0.25D;

    // --- INITIALIZER FUNCTION ---

    static void init() {
        Crown.getVars().register();
    }
}