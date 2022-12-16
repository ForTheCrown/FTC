// Constants

// SNOW_RANGE determines the definite range where snow and ice is spawned,
// afterwards the ice and snow will drop off until they finally hit SNOW_MAX,
// where all snow and ice generation stops
const SNOW_RANGE         = 125;
const SNOW_MAX           = SNOW_RANGE * 2;

// Squared values so distance calculation is a bit faster
const SNOW_RANGE_SQUARED = SNOW_RANGE * SNOW_RANGE;
const SNOW_MAX_SQ        = SNOW_MAX * SNOW_MAX;

// Chance for snow to generate in the city paths
const PATH_SNOW_CHANCE   = 0.0025;

const RANDOM             = Util.RANDOM;

// Java Types
const Waypoint           = Java.type("net.forthecrown.waypoint.Waypoint");
const Waypoints          = Java.type("net.forthecrown.waypoint.Waypoints");
const W_Manager          = Java.type("net.forthecrown.waypoint.WaypointManager");
const WaypointConfig     = Java.type("net.forthecrown.waypoint.WaypointConfig");
const S_Manager          = Java.type("net.forthecrown.core.script.ScriptManager");
const Block              = Java.type("org.bukkit.block.Block");
const HeightMap          = Java.type("org.bukkit.HeightMap");
const BlockSupport       = Java.type("org.bukkit.block.BlockSupport");
const BlockFace          = Java.type("org.bukkit.block.BlockFace");
const J_Set              = Java.type("it.unimi.dsi.fastutil.objects.ObjectOpenHashSet");
const Files              = Java.type("java.nio.file.Files");

const DIRECTORY          = S_Manager.getInstance().getDirectory();
const OUTPUT_TXT         = DIRECTORY.resolve("snow_gen_output.txt");
const OUTPUT_WRITER      = Files.newBufferedWriter(OUTPUT_TXT);

// Hazel
const hazel = W_Manager.getInstance()
        .get(WaypointConfig.spawnWaypoint);

// Ensure hazel exists lol
if (hazel == null) {
    logger.warn(
            "No spawn waypoint named {} found! Cannot generate snow",
            WaypointConfig.spawnWaypoint
    );
} else {
    run();
}

function run() {
    var pos = hazel.getPosition()
        .toVector2(true);

    var minPos = pos.sub(SNOW_MAX, SNOW_MAX);
    var maxPos = pos.add(SNOW_MAX, SNOW_MAX);
    var size = maxPos.sub(minPos);

    // Create height map then place it
    var heightMap = createHeightMap(hazel.getWorld(), minPos, size);
    placeSnow(heightMap);

    OUTPUT_WRITER.close();
}

function createHeightMap(world, min, size) {
    var heightMap = Array(size.x());

    // Fill array
    for (let i = 0; i < size.x(); i++) {
        heightMap[i] = Array(size.y());
    }

    var center = hazel.getPosition()
            .toVector2(true);

    logger.info("center={}, min={}, size={}", center, min, size);
    var dropOffDifference = SNOW_MAX_SQ - SNOW_RANGE_SQUARED;

    // Initialize values
    for (let x = 0; x < size.x(); x++) {
        for (let z = 0; z < size.y(); z++) {
            let absX = x + min.x();
            let absZ = z + min.y();

            let centerDistSq = center.distanceSquared(absX, absZ);

            // If above total max distance
            if (centerDistSq > SNOW_MAX_SQ) {
                continue;
            }

            // If we're outside definite snow range, then
            // call RANDOM to figure out if a block should
            // be placed here, the chance of it allowing us to place
            // should decrease as it gets further from the center.
            if (centerDistSq > SNOW_RANGE_SQUARED) {
                let distRatio = (centerDistSq - SNOW_RANGE_SQUARED) / dropOffDifference;
                let randFloat = RANDOM.nextFloat();

                if (randFloat < distRatio) {
                    continue;
                }
            }

            let block = world.getHighestBlockAt(absX, absZ, HeightMap.MOTION_BLOCKING);
            block = findSnowBlockY(block);

            // Don't override any existing ice or place on any
            // slab/stair blocks lol
            let type = block.getType();
            if (type.name().contains("ICE")
                    || type.name().contains("SPRUCE_PLANK")
                    || type.name().contains("SAND")
            ) {
                continue;
            }

            // Ensure the above block is air
            let above = block.getRelative(0, 1, 0);
            if (!above.isEmpty()) {
                continue;
            }

            if (!block.isLiquid() && !type.name().contains("LEAVES")) {
                // Ensure snow can be placed on the block
                let bData = block.getBlockData();
                if (!bData.isFaceSturdy(BlockFace.UP, BlockSupport.FULL)) {
                    continue;
                }
            }

            heightMap[x][z] = {
                craftBlock: block,
                isPath: false,
                placeIce: false
            }
        }
    }

    fillHeightMapData(heightMap);
    return heightMap;
}

// Finds the appropriate block to place snow at, if the block is underwater,
// then it finds the surface water block, if it's on a non-solid block then
// moves downward until it finds a solid block to place snow on
function findSnowBlockY(block) {
    if (block.isLiquid()) {
        while (true) {
            var relative = block.getRelative(0, 1, 0);

            if (!relative.isLiquid()) {
                return block;
            } else {
                block = relative;
            }
        }
    }

    while (block.isPassable() || block.isEmpty()) {
        block = block.getRelative(0, -1, 0);

        if (block.getY() <= Util.MIN_Y) {
            return block;
        }
    }

    return block;
}

function fillHeightMapData(heightMap) {
    heightMap.forEach(element => {
        element.forEach(blockData => {
            detectPath(blockData);

            var cBlock = blockData.craftBlock;

            var typeName = cBlock.getType().name();
            if (cBlock.isLiquid() || typeName.contains("KELP") || typeName.contains("SEAGRASS")) {
                blockData.placeIce = true;
            }
        });
    });
}

function detectPath(blockData) {
    if (!isPath(blockData.craftBlock.getType())) {
        return;
    }

    var surroundingBlocks = new J_Set();

    // Detect if path
    for (let x = -1; x < 2; x++) {
        for (let z = -1; z < 2; z++) {
            if (x == 0 && z == 0) {
                continue;
            }

            const cBlock = blockData.craftBlock.getRelative(x, 0, z);
            const type = cBlock.getType();

            if (!isPath(type) || !isSurfaceBlock(cBlock)) {
                continue;
            }

            surroundingBlocks.add(type);
        }
    }

    blockData.isPath = surroundingBlocks.size() > 1;
}

function placeSnow(heightMap) {
    const bounds = hazel.getBounds();
    const center = hazel.getPosition().toVector2(true);
    print("placeSnow");

    for (let arrIndex = 0; arrIndex < heightMap.length; arrIndex++) {
        var blockArray = heightMap[arrIndex];

        blockArray.forEach(element => {
            const block = element.craftBlock;

            var typeName = block.getType().name();
            if (element.placeIce) {
                logBlock(block);

                block.setType(Material.ICE);
                return;
            }

            // Dumb dumb way of testing if the block can
            // have snow placed on it
            if (bounds.contains(block)) {
                return;
            }

            if (element.isPath && RANDOM.nextFloat() > PATH_SNOW_CHANCE) {
                return;
            }

            var relative = block.getRelative(0, 1, 0);
            var data = Material.SNOW.createBlockData();

            // If random decides we should randomize show height
            if (RANDOM.nextBoolean()) {
                var minLayers = data.getMinimumLayers();
                var maxLayers = data.getMaximumLayers();
                var halfLayerDif = (maxLayers - minLayers) / 2;

                // nextInt between min and max produced atrocities lol,
                // so the range is between min and max / 2
                var chosenLayers = RANDOM.nextInt(minLayers, minLayers + halfLayerDif);
                data.setLayers(chosenLayers);
            }

            logBlock(relative);
            relative.setBlockData(data);
        });
    }
}

function logBlock(relative) {
    var currentData = relative.getBlockData().getAsString(false);
    OUTPUT_WRITER.write(relative.getX() + " " + relative.getY() + " " + relative.getZ() + " " + currentData);
    OUTPUT_WRITER.newLine();
}

function isPath(blockType) {
    if (blockType == Material.STONE_BRICKS
            || blockType == Material.COBBLESTONE
            || blockType == Material.GRAVEL
            || blockType == Material.CRACKED_STONE_BRICKS
            || blockType == Material.POLISHED_ANDESITE
    )  {
        return true;
    }

    return false;
}

function isSurfaceBlock(block) {
    var above = block.getRelative(0, 1, 0);

    return above.isEmpty()
            || above.isPassable()
            || above.isLiquid();
}