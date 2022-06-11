package net.forthecrown.regions;

import net.forthecrown.core.Crown;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.forthecrown.utils.math.WorldBounds3i;
import net.forthecrown.utils.math.WorldVec3i;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.WallSign;

import java.util.UUID;

public class FtcRegionPoleGenerator implements RegionPoleGenerator {
    private final FtcRegionManager manager;

    public FtcRegionPoleGenerator(FtcRegionManager manager) {
        this.manager = manager;
    }

    //I hate hardcoding this in, but I don't see a different solution rn.
    //Vanilla structures weren't working :(
    @Override
    public void generate(PopulationRegion region) {
        //Get the center bottom the pole should be placed at
        WorldVec3i pos = region.getPoleBottom().mutable();

        //bounding box for the region pole
        WorldBounds3i box = region.getPoleBoundingBox().toWorldBounds(region.getWorld());

        //Clear area
        box.forEach(b -> b.setType(Material.AIR, false));

        //Go to the top and place the sea lantern
        pos.above(3);
        pos.getBlock().setType(Material.SEA_LANTERN, false);

        //Place the help signs on the side
        generateSideText(pos.clone().inDirection(BlockFace.WEST), BlockFace.WEST);
        generateSideText(pos.clone().inDirection(BlockFace.EAST), BlockFace.EAST);

        // Place the residency sign
        RegionResidency residency = region.getResidency();
        if(!region.hasProperty(RegionProperty.HIDE_RESIDENTS) && !residency.isEmpty()) {
            generateResidency(pos.clone().inDirection(BlockFace.NORTH), BlockFace.NORTH, residency);
        }

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

        // Update potential water block under the pole
        // It looks awkward without this, idk why,
        // we never change that block
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
        sign.line(0, Component.text(Crown.inDebugMode() ? region.getPos().toString() : ""));
        sign.line(1, Component.text(region.hasName() ? region.getName() : "Wilderness"));
        sign.line(2, Component.text("Region"));
        sign.line(3, Component.empty());

        sign.getPersistentDataContainer().set(TOP_SIGN_KEY, RegionPos.DATA_TYPE, region.getPos());

        sign.update();
    }

    @Override
    public void generateNextRegion(WorldVec3i pos, BlockFace signFace, BlockFace direction, RegionPos origin) {
        RegionAccess region = manager.getSnapshot(origin.add(direction.getModX(), direction.getModZ()));

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
    public void generateResidency(WorldVec3i pos, BlockFace direction, RegionResidency residency) {
        Block b = pos.getBlock();

        WallSign data = (WallSign) Material.OAK_WALL_SIGN.createBlockData();
        data.setFacing(direction);
        b.setBlockData(data, false);

        Sign sign = (Sign) b.getState();

        Component
                line1 = null,
                line2 = null;

        if (residency.size() == 1) {
            var v = residency.getEntries().entrySet().iterator().next();
            UUID id = v.getKey();
            RegionResidency.Resident entry = v.getValue();

            CrownUser user = UserManager.getUser(id);
            Component nick = user.nickOrName();

            if (ChatUtils.plainText(nick).length() <= 16 && entry.isDirectResident()) {
                line1 = Component.text("Resident:");
                line2 = nick;
            }
        }

        if(line1 == null || line2 == null) {
            line1 = Component.text("Residents:");
            line2 = Component.text(residency.isEmpty() ? "None" : "" + residency.size());
        }

        sign.line(1, line1);
        sign.line(2, line2);
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