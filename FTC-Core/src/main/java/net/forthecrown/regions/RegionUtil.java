package net.forthecrown.regions;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sk89q.worldedit.math.BlockVector2;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.FtcVars;
import net.forthecrown.core.Permissions;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.utils.math.MathUtil;
import net.forthecrown.utils.math.WorldBounds3i;
import net.forthecrown.utils.math.WorldVec3i;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.IntArrayTag;
import org.bukkit.HeightMap;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import static net.forthecrown.regions.RegionConstants.DISTANCE_TO_POLE;
import static net.forthecrown.regions.RegionPoleGenerator.TOP_SIGN_KEY;

public final class RegionUtil {
    private RegionUtil() {}

    /**
     * Does sum fuckery to find a region pole's bounding box
     * @param region The region to find the bounding box for
     * @return The bounding box
     */
    public static Bounds3i poleBoundingBox(PopulationRegion region) {
        WorldVec3i p = bottomOfPole(region.getWorld(), region.getPolePosition());

        return new Bounds3i(p.getX() - 2, p.getY(), p.getZ()-2, p.getX() + 2, p.getY() + 5, p.getZ() + 2);
    }

    private static WorldVec3i findBottom(WorldVec3i p) {
        p = p.mutable();
        if(p.getY() < FtcUtils.MIN_Y) return null;

        Block b = p.getBlock();
        if(b.getType() != Material.OAK_SIGN) return findBottom(p.below());
        Sign sign = (Sign) b.getState();

        PersistentDataContainer container = sign.getPersistentDataContainer();

        if(container.has(TOP_SIGN_KEY, PersistentDataType.BYTE)
                || container.has(TOP_SIGN_KEY, RegionPos.DATA_TYPE)
        ) {
            return p.below(4);
        }

        return findBottom(p.below());
    }

    private static WorldVec3i findBottomLazy(WorldVec3i vec3i) {
        Material mat = vec3i.getMaterial();

        if(mat.isAir()) return findBottomLazy(vec3i.below());

        return switch (mat) {
            case OAK_SIGN -> vec3i.subtract(0, 4, 0);
            case SEA_LANTERN -> vec3i.subtract(0, 3, 0);
            case GLOWSTONE -> findBottomLazy(vec3i.below());
            default -> vec3i;
        };
    }

    /**
     * Gets the bottom position of a pole
     * @param world The world the pole is in
     * @param vec2 The position of the pole
     * @return The pole's position
     */
    public static WorldVec3i bottomOfPole(World world, BlockVector2 vec2) {
        int y = world.getHighestBlockYAt(vec2.getX(), vec2.getZ(), HeightMap.WORLD_SURFACE);
        WorldVec3i vec3i = new WorldVec3i(world, vec2.getX(), y, vec2.getZ());
        WorldVec3i result = findBottom(vec3i);

        return result == null ? findBottomLazy(vec3i) : result;
    }

    /**
     * Checks if the given 2D position is a valid place for a region post
     * @param region The region the pole is for
     * @param vec The place to put the pole
     * @return True, if the pole is not outside the region and is more than 5 blocks away from the edge
     */
    public static boolean isValidPolePosition(PopulationRegion region, BlockVector2 vec) {
        WorldBounds3i valid = region.getBB().contract(5);

        return MathUtil.inRange(vec.getX(), valid.minX(), valid.maxX()) &&
                MathUtil.inRange(vec.getZ(), valid.minZ(), valid.maxZ());
    }

    /**
     * Validates that the given world is the region world
     * @param world The world to check
     * @throws CommandSyntaxException If the world is not the region world
     */
    public static void validateWorld(World world) throws CommandSyntaxException {
        if(!world.equals(FtcVars.getRegionWorld())) throw FtcExceptionProvider.regionsWrongWorld();
    }

    /**
     * Checks if the given user is in a valid distance to the pole
     * @param pole The pole's position
     * @param user The user
     * @return True, if the user has the {@link Permissions#REGIONS_ADMIN} permission or is close to the pole
     */
    public static boolean isCloseToPoleOrOp(BlockVector2 pole, CrownUser user) {
        return isCloseToPole(pole, user) || user.hasPermission(Permissions.REGIONS_ADMIN);
    }

    /**
     * Checks if the given user is within a valid distance to the
     * given pole position
     *
     * @param pole The pole's position
     * @param user The user
     * @return True, if the user is closer than {@link RegionConstants#DISTANCE_TO_POLE} to the pole.
     */
    public static boolean isCloseToPole(BlockVector2 pole, CrownUser user) {
        BlockVector2 vec2 = user.get2DLocation();
        return vec2.distance(pole) <= DISTANCE_TO_POLE;
    }

    /**
     * Checks if the user has the required permission or is close enough
     * to the given pole
     *
     * @param pole The pole's position
     * @param user The user
     * @throws CommandSyntaxException If user either doesn't have the permission or isn't close enough
     */
    public static void validateDistance(BlockVector2 pole, CrownUser user) throws CommandSyntaxException {
        if(!isCloseToPoleOrOp(pole, user)) {
            throw FtcExceptionProvider.translatable("regions.tooFar",
                    Component.text("x = " + pole.getX() + " z = " + pole.getZ())
            );
        }
    }

    public static IntArrayTag writeColumn(BlockVector2 pos) {
        return new IntArrayTag(new int[] {pos.getX(), pos.getZ()});
    }

    public static BlockVector2 readColumn(int[] arr) {
        return BlockVector2.at(arr[0], arr[1]);
    }

    public static int getPoleTop(PopulationRegion data) {
        return data != null ? data.getPoleBoundingBox().maxY() : FtcUtils.MAX_Y;
    }
}