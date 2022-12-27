const MAX_RADIUS = 18;
const MAX_RADIUS_SQ = MAX_RADIUS * MAX_RADIUS;

const Worlds = Java.type("net.forthecrown.core.Worlds");

const center = {
  x: -1356,
  y: 75,
  z: -8808
};

const start = {
  x: center.x - MAX_RADIUS,
  z: center.z - MAX_RADIUS
};

const end = {
  x: center.x + MAX_RADIUS,
  z: center.z + MAX_RADIUS
};

for (let x = start.x; x < end.x; x++) {
    for (let z = start.z; z < end.z; z++) {
        const block = Worlds.overworld().getBlockAt(x, center.y, z);
        const distSq = centerDistSq(x, z);

        if (distSq > MAX_RADIUS_SQ || !block.isEmpty()) {
            continue;
        }

      block.setType(Material.SNOW_BLOCK);
    }
}

function centerDistSq(x, z) {
    const xDif = center.x - x;
    const zDif = center.z - z;

    return xDif * xDif + zDif * zDif;
}