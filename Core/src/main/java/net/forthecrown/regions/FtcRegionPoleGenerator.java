package net.forthecrown.regions;

import com.sk89q.worldedit.math.BlockVector2;
import net.forthecrown.utils.math.FtcBoundingBox;
import net.forthecrown.utils.math.WorldVec3i;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;

public class FtcRegionPoleGenerator implements RegionPoleGenerator {

    private final FtcRegionManager manager;

    public FtcRegionPoleGenerator(FtcRegionManager manager) {
        this.manager = manager;
    }

    //I hate hardcoding this in, but I don't see a different solution rn.
    //Vanilla structures weren't working :(
    @Override
    public void generate(PopulationRegion region) {
        World world = region.getWorld();
        BlockVector2 place = region.getPolePosition();
        int highestY = world.getHighestBlockYAt(place.getX(), place.getZ());

        //Get the center bottom the pole should be placed at
        WorldVec3i p = RegionUtil.findBottomOfPole(new WorldVec3i(world, place.getX(), highestY, place.getZ()));

        //bounding box for the region pole
        FtcBoundingBox box = FtcBoundingBox.of(region.getWorld(), region.getPoleBoundingBox());

        //Clear area
        box.forEach(b -> b.setType(Material.AIR, false));
        box.getEntitiesByType(Item.class).forEach(Entity::remove);

        //Go to the top and place the sea lantern
        p.above(3);
        p.getBlock().setType(Material.SEA_LANTERN);

        //Place the help signs on the side
        generateSideText(p.clone().inDirection(BlockFace.WEST), BlockFace.WEST);
        generateSideText(p.clone().inDirection(BlockFace.EAST), BlockFace.EAST);

        //Place region name
        p.above();
        generateRegionName(p.clone(), region);

        //Place first glowstone
        p.below(2);
        p.getBlock().setType(Material.GLOWSTONE);

        //Next region signs
        generateNextRegion(p.clone().inDirection(BlockFace.NORTH), BlockFace.NORTH, BlockFace.EAST, region.getPos());
        generateNextRegion(p.clone().inDirection(BlockFace.SOUTH), BlockFace.SOUTH, BlockFace.WEST, region.getPos());
        generateNextRegion(p.clone().inDirection(BlockFace.WEST), BlockFace.WEST, BlockFace.NORTH, region.getPos());
        generateNextRegion(p.clone().inDirection(BlockFace.EAST), BlockFace.EAST, BlockFace.SOUTH, region.getPos());

        //Place final glowstone
        p.below();
        p.getBlock().setType(Material.GLOWSTONE);
        p.below();

        //Get min bottom of pole
        p.subtract(2, 0, 2);

        //Place the base
        for (int index = 0; index < 25; index++) {
            //If it's an edge, place cool block, if not, place based
            Material blockMat = index == 0 || index == 4 || index == 21 || index == 25 ?
                    Material.CHISELED_STONE_BRICKS : Material.STONE_BRICKS;

            WorldVec3i pos = p.clone().add(index / 5, 0, index % 5);
            Block block = pos.getBlock();

            block.setType(blockMat);
        }
    }

    @Override
    public void generateRegionName(WorldVec3i pos, PopulationRegion region) {
        Block block = pos.getBlock();

        //Set type
        block.setType(Material.OAK_SIGN);
        Sign sign = (Sign) block.getState();

        //Set lines
        sign.line(0, Component.empty());
        sign.line(1, Component.text(region.hasName() ? region.getName() : "Wilderness"));
        sign.line(2, Component.text("Region"));
        sign.line(3, Component.empty());

        sign.update();
    }

    @Override
    public void generateNextRegion(WorldVec3i pos, BlockFace signFace, BlockFace direction, RegionPos origin) {
        PopulationRegion region = manager.get(origin.add(direction.getModX(), direction.getModZ()));

        Block block = pos.getBlock();
        block.setType(Material.OAK_WALL_SIGN);

        //set signFace, aka, direction
        Directional directional = (Directional) block.getBlockData();
        directional.setFacing(signFace);
        block.setBlockData(directional);

        Sign sign = (Sign) block.getState();
        Component pointer = Component.text("<---");

        //set lines
        sign.line(0, pointer);
        sign.line(1, Component.text(region.hasName() ? region.getName() : "Wilderness"));
        sign.line(2, Component.text("Region"));
        sign.line(3, pointer);

        sign.update();
    }

    @Override
    public void generateSideText(WorldVec3i pos, BlockFace direction) {
        Block block = pos.getBlock();
        block.setType(Material.OAK_WALL_SIGN);

        //set direction
        Directional directional = (Directional) block.getBlockData();
        directional.setFacing(direction);
        block.setBlockData(directional);

        Sign sign = (Sign) block.getState();

        //set lines and update
        sign.line(1, Component.text("/visit <region>"));
        sign.line(2, Component.text("to teleport."));
        sign.update();
    }

    @Override
    public RegionManager getManager() {
        return manager;
    }
}
