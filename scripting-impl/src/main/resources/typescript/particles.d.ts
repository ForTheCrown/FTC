
/**
 * Color vector, ranges from 0.0 to 1.0
 */
declare type ColorVector = Vector3d;

/** Name of a particle */
declare type ParticleName = string;

/**
 * A function which takes a 0.0 to 1.0 number as input and provides a color as 
 * a result.
 */
declare type ColorFunction = (progress: number) => ColorVector;

/** An array of colors that are linearly interpolated between */
declare type ColorLerp = ColorVector[];

/** Either a color array, a color function or a single color vector */
declare type ColorValue = ColorLerp | ColorFunction | ColorVector;

declare type PointTransformer = (point: Vector3d, index: number) => Vector3d;

/** Particle spawning options */
declare interface ParticleOptions {
  particleName: ParticleName;

  offset?: Vector3d;

  count?: number;
  force?: boolean;

  color?: ColorValue;
  colorTransition?: ColorValue;
  item?: any;
  blockData?: any;
  size?: number;

  transformer?: PointTransformer
}

/** Either a particle's name or the options required to spawn it */
declare type ParticleSpawn = ParticleOptions | ParticleName;

/**
 * Creates a java-wrapped options object from a JavaScript particle spawn.
 * @param particle Particle name
 */
declare function compileOptions(particle?: ParticleSpawn): ParticleOptions;

/**
 * Spawns a particle
 * 
 * @param particle Particle to spawn
 * @param world Name of the world to spawn the particle in
 * @param position Position to spawn the particle at
 */
declare function spawnParticle(particle: ParticleSpawn, world: string, position: Vector3d): void;

declare function spawnParticleLine(
  particle: ParticleSpawn, 
  world: string, 
  origin: Vector3d, 
  destination: Vector3d,
  particleInterval?: number
): void;

declare function spawnParticleSphere(
  particle: ParticleSpawn,
  world: string,
  centerPosition: Vector3d,
  radius: number | Vector3d,
  particleInterval?: number
): void;

declare function spawnParticleCircle(
  particle: ParticleSpawn,
  world: string,
  centerPosition: Vector3d,
  radius: number | Vector2d,
  particleInterval?: number,
): void;