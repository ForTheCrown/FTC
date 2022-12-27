const BoundingBox = Java.type("org.bukkit.util.BoundingBox");

const exit = {
  x: -1356.5,
  y: 78,
  z: -8791.5,
  yaw: 0,
  pitch: 180
}

const RADIUS = 100;

function onUse(user) {
  let world = user.getWorld();

  world.getNearbyEntities(makeBounds()).forEach(entity => {
      entity.sendMessage(
            Text.format(
                    "&e{0, user}&r has lost",
                    NamedTextColor.GRAY,
                    user
            )
      )
  });

  let loc = new Location(world, exit.x, exit.y, exit.z, exit.pitch, exit.yaw);
  user.getPlayer().teleport(loc);
}

function makeBounds() {
  return new BoundingBox(
      exit.x - RADIUS,
      exit.y - RADIUS,
      exit.z - RADIUS,
      exit.x + RADIUS,
      exit.y + RADIUS,
      exit.z + RADIUS
      );
}