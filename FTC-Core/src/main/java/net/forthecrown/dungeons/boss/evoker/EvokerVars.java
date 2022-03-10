package net.forthecrown.dungeons.boss.evoker;

import net.forthecrown.core.Keys;
import net.forthecrown.vars.Var;
import net.forthecrown.vars.types.VarTypes;
import net.kyori.adventure.key.Key;

import static net.forthecrown.vars.Var.def;

public interface EvokerVars {
    Var<Key>
            LEVEL_KEY                   = def("evoker_levelKey",                        VarTypes.KEY,       Keys.forthecrown("levels/evoker_level"));

    Var<Double>
            DEATH_PHASE_HEALTH          = def("evoker_deathPhaseHealth",                VarTypes.DOUBLE,    10.0D),
            IMPACT_RADIUS_START         = def("evoker_effect_impact_radiusStart",       VarTypes.DOUBLE,    1.25D),
            IMPACT_RADIUS_STEP          = def("evoker_effect_impact_radiusStep",        VarTypes.DOUBLE,    0.25D),

            SHULKER_PARTICLE_DISTANCE   = def("evoker_phase_shulker_particleDistance",  VarTypes.DOUBLE,    0.2D),

            POT_DIST_MIN                = def("evoker_phase_potion_minDist",            VarTypes.DOUBLE,    4.0D),
            POT_DIST_MAX                = def("evoker_phase_potion_maxDist",            VarTypes.DOUBLE,    15.0D),
            POT_SPAWN_Y                 = def("evoker_phase_potion_spawnY",             VarTypes.DOUBLE,    40.5D),
            FIRING_SPEED                = def("evoker_phase_shulker_firingSpeed",       VarTypes.DOUBLE,    0.75D);

    Var<Short>
            IMPACT_STEP_PARICLES        = def("evoker_effect_impact_stepParticles",     VarTypes.SHORT,     (short) 7);

    Var<Integer>
            RHINE_REWARD                = def("evoker_rhineReward",                     VarTypes.INT,       25000),
            BASE_HEALTH                 = def("evoker_baseHealth",                      VarTypes.INT,       400),
            TICKS_BETWEEN_SPAWNS        = def("evoker_ticksBetweenSpawns",              VarTypes.INT,       7),
            DEATH_ANIM_LENGTH           = def("evoker_deathAnimLength",                 VarTypes.INT,       40),
            PHASE_TRANSITION            = def("evoker_phaseTransition",                 VarTypes.INT,       5 * 20),

            NORMAL_PHASE_LENGTH         = def("evoker_phase_normal_length",             VarTypes.INT,       400),

            SKELETON_SPAWN_CHANCE       = def("evoker_phase_zombies_skeletonChance",    VarTypes.INT,       5),
            RAVAGER_SPAWN_CHANCE        = def("evoker_phase_illager_ravagerChance",     VarTypes.INT,       1),
            SHIELD_PHASE_LENGTH         = def("evoker_phase_vulnerable_length",         VarTypes.INT,       PHASE_TRANSITION.get() * 2),

            SHULKER_DRAW_INTERVAL       = def("evoker_phase_shulker_drawInterval",      VarTypes.INT,       15),
            SHULKER_AIMING_TIME         = def("evoker_phase_shulker_aimingTime",        VarTypes.INT,       15),
            SHULKER_AIM_INTERVAL        = def("evoker_phase_shulker_aimInterval",       VarTypes.INT,       60),

            POT_THROW_INTERVAL          = def("evoker_phase_potion_throwInterval",      VarTypes.INT,       20),
            POT_PHASE_LENGTH            = def("evoker_phase_potion_length",             VarTypes.INT,       140),
            POT_SPAWN_DELAY             = def("evoker_phase_potion_spawnDelay",         VarTypes.INT,       5),

            GHAST_PHASE_LENGTH          = def("evoker_phase_ghast_length",              VarTypes.INT,       400),
            GHAST_PHASE_HEALTH          = def("evoker_phase_ghast_health",              VarTypes.INT,       2000),

            PUSH_AWAY_RADIUS            = def("evoker_effect_pushAway_radius",          VarTypes.INT,       3),
            IMPACT_STEP_COUNT           = def("evoker_effect_impact_stepCount",         VarTypes.INT,       5);

    Var<String>
            BOSS_TEAM                   = def("evoker_bossTeam",                        VarTypes.STRING,    "DungeonEvokerBoss"),
            MOB_TEAM                    = def("evoker_mobTeam",                         VarTypes.STRING,    "DungeonMobTeam");

    static void init() {}
}
