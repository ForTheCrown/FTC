package net.forthecrown.dungeons.boss.evoker;

import lombok.experimental.UtilityClass;
import net.forthecrown.core.config.ConfigData;

@ConfigData(filePath = "evoker.json")
public @UtilityClass class EvokerConfig {

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

    public static double
            shulker_particleDistance        = 0.2D,

            potion_minDist                  = 4.0D,
            potion_maxDist                  = 15.0D,
            potion_spawnY                   = 40.5D,

            shulker_firingSpeed             = 0.75D;

    public static int
            normal_length                   = 400,

            zombies_skeletonChance          = 5,
            illager_ravagerChance           = 1,
            vulnerable_length               = phaseTransition * 2,

            shulker_drawInterval            = 15,
            shulker_aimingTime              = 15,
            shulker_aimInterval             = 60,

            potion_throwInterval            = 20,
            potion_length                   = 140,
            potion_spawnDelay               = 5,

            ghast_length                    = 400,
            ghast_health                    = 2000;

    public static short
            impact_stepParticles = 7;

    public static int
            pushAway_radius = 3,
            impact_stepCount = 5;

    public static double
            impact_radiusStart = 1.25D,
            impact_radiusStep = 0.25D;
}