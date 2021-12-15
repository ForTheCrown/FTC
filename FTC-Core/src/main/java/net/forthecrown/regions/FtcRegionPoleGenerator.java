package net.forthecrown.regions;

import com.sk89q.worldedit.math.BlockVector2;
import net.forthecrown.utils.transformation.FtcBoundingBox;
import net.forthecrown.utils.math.WorldVec3i;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.persistence.PersistentDataType;

public class FtcRegionPoleGenerator implements RegionPoleGenerator {
    private final FtcRegionManager manager;

    public FtcRegionPoleGenerator(FtcRegionManager manager) {
        this.manager = manager;
    }

    //I hate hardcoding this in, but I don't see a different solution rn.
    //Vanilla structures weren't working :(
    @Override
    public void generate(PopulationRegion region) {
        World world = manager.getWorld();
        BlockVector2 place = region.getPolePosition();

        //Get the center bottom the pole should be placed at
        WorldVec3i pos = RegionUtil.bottomOfPole(world, place);
        WorldVec3i bottom = pos.clone();

        //bounding box for the region pole
        FtcBoundingBox box = FtcBoundingBox.of(region.getWorld(), region.getPoleBoundingBox());
        box.expand(BlockFace.SOUTH_EAST, 1);

        //Clear area
        box.forEach(b -> b.setType(Material.AIR, false));

        //Go to the top and place the sea lantern
        pos.above(3);
        pos.getBlock().setType(Material.SEA_LANTERN, false);

        //Place the help signs on the side
        generateSideText(pos.clone().inDirection(BlockFace.WEST), BlockFace.WEST);
        generateSideText(pos.clone().inDirection(BlockFace.EAST), BlockFace.EAST);

        //Place region name
        pos.above();
        generateRegionName(pos.clone(), region);

        //Place first glowstone
        pos.below(2);
        pos.getBlock().setType(Material.GLOWSTONE, false);

        //Next region signs
        generateNextRegion(pos.clone().inDirection(BlockFace.NORTH), BlockFace.NORTH, BlockFace.EAST, region.getPos());
        generateNextRegion(pos.clone().inDirection(BlockFace.SOUTH), BlockFace.SOUTH, BlockFace.WEST, region.getPos());
        generateNextRegion(pos.clone().inDirection(BlockFace.WEST), BlockFace.WEST, BlockFace.NORTH, region.getPos());
        generateNextRegion(pos.clone().inDirection(BlockFace.EAST), BlockFace.EAST, BlockFace.SOUTH, region.getPos());

        //Place final glowstone
        pos.below();
        pos.getBlock().setType(Material.GLOWSTONE, false);

        //Get min bottom of pole
        pos.subtract(2, 1, 2);

        //Place the base
        for (int index = 0; index < 25; index++) {
            int x = index / 5;
            int z = index % 5;

            WorldVec3i pos1 = pos.clone().add(x, 0, z);
            Block block = pos1.getBlock();

            block.setType(Material.STONE_BRICKS, false);
        }

        //Set corner blocks
        pos.getBlock().setType(Material.CHISELED_STONE_BRICKS, false);
        pos.add(0, 0, 4).getBlock().setType(Material.CHISELED_STONE_BRICKS, false);
        pos.add(4, 0, 0).getBlock().setType(Material.CHISELED_STONE_BRICKS, false);
        pos.subtract(0, 0, 4).getBlock().setType(Material.CHISELED_STONE_BRICKS, false);

        pos.subtract(1, 1, 1).getBlock().getState().update(false, false);
    }


    @Override
    public void generateRegionName(WorldVec3i pos, PopulationRegion region) {
        Block block = pos.getBlock();

        //Make sure sign is facing north
        org.bukkit.block.data.type.Sign signData = (org.bukkit.block.data.type.Sign) Material.OAK_SIGN.createBlockData();
        signData.setRotation(BlockFace.NORTH);
        block.setBlockData(signData, false);

        Sign sign = (Sign) block.getState();

        //Set lines
        sign.line(0, Component.empty());
        sign.line(1, Component.text(region.hasName() ? region.getName() : "Wilderness"));
        sign.line(2, Component.text("Region"));
        sign.line(3, Component.empty());
        sign.getPersistentDataContainer().set(TOP_SIGN_KEY, PersistentDataType.BYTE, (byte) 1);

        sign.update();
    }

    @Override
    public void generateNextRegion(WorldVec3i pos, BlockFace signFace, BlockFace direction, RegionPos origin) {
        RegionData region = manager.getData(origin.add(direction.getModX(), direction.getModZ()));

        Block block = pos.getBlock();

        //set signFace, aka, direction
        Directional directional = (Directional) Material.OAK_WALL_SIGN.createBlockData();
        directional.setFacing(signFace);
        block.setBlockData(directional, false);

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

        //set direction
        WallSign signData = (WallSign) Material.OAK_WALL_SIGN.createBlockData();
        signData.setFacing(direction);
        block.setBlockData(signData, false);

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
