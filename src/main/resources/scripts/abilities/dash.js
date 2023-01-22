import "@bukkit.Particle";

import "org.spongepowered.math.imaginary.Quaterniond";
import "org.spongepowered.math.vector.Vector3d";

import "@jlang.Float";
import "@jutil.ArrayList";

const MULTIPLIER = 1.10;
const ROUND_MIN = -0.07841;

function onRightClick(player, clicked) {
  if (clicked != null) {
    return false;
  }

  // Note: velocity == 0, if player is just walking or sprinting
  // Apperantly that's a bug that hasn't been fixed since 2017 lmao
  let velocity = player.getVelocity();

  if (player.isOnGround()) {
    velocity.setX(1);
    velocity.setY(1);
    velocity.setZ(1);
  }

  // Y axis is a lil weird, x and z axes are both 0
  // when not moving, but y axis has that weird 0.07841 value
  velocity.setY(roundValue(velocity.getY()));

  let lengthSq = velocity.lengthSquared();

  // lengthSq of 0 means absolutely no velocity, can happen
  // when the holder of the sword is flying
  if (lengthSq == 0) {
    return false;
  } else {
    velocity.normalize();
  }

  // Take the strength of their velocity, multiply it by MULTIPLIER
  // and then shift it to the direction they're looking in, no clue
  // if that's a good idea or not
  let length = velocity.length();
  let dir = player.getLocation().getDirection();
  dir.multiply(length * MULTIPLIER);
  player.setVelocity(dir);

  spawnEffects(player);
  // TODO: A sound effect

  return true;
}

function spawnEffects(player) {
  let bLoc = player.getLocation();
  bLoc.add(0, 0.75, 0);

  let builder = Particle.END_ROD.builder();
  builder.location(bLoc);
  builder.allPlayers();
  builder.offset(0.15, 0.15, 0.15);
  builder.count(10)
  builder.extra(0.05);
  builder.spawn();
}

function roundValue(value) {
  return value >= ROUND_MIN && value <= 0 ? 0 : value;
}