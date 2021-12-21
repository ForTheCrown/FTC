package net.forthecrown.regions;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sk89q.worldedit.math.BlockVector2;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.ComVars;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.math.MathUtil;
import net.forthecrown.utils.math.WorldVec3i;
import net.forthecrown.utils.transformation.FtcBoundingBox;
import net.kyori.adventure.text.Component;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.bukkit.HeightMap;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.persistence.PersistentDataType;

import static net.forthecrown.regions.RegionConstants.DISTANCE_TO_POLE;

public final class RegionUtil {
    private RegionUtil() {}

    /**
     * Does sum fuckery to find a region pole's bounding box
     * @param region The region to find the bounding box for
     * @return The bounding box
     */
    public static BoundingBox poleBoundingBox(PopulationRegion region) {
        WorldVec3i p = bottomOfPole(region.getWorld(), region.getPolePosition());

        return new BoundingBox(p.x - 2, p.y, p.z-2, p.x + 2, p.y + 5, p.z + 2);
    }

    private static WorldVec3i findBottom(WorldVec3i p) {
        if(p.getY() < FtcUtils.MIN_Y) return null;

        Block b = p.getBlock();
        if(b.getType() != Material.OAK_SIGN) return findBottom(p.below());
        Sign sign = (Sign) b.getState();

        if(sign.getPersistentDataContainer().has(RegionPoleGenerator.TOP_SIGN_KEY, PersistentDataType.BYTE)) {
            return p.below(4);
        }

        return findBottom(p.below());
    }

    public static WorldVec3i findBottomLazy(WorldVec3i vec3i) {
        Material mat = vec3i.getMaterial();

        if(mat.isAir()) return findBottomLazy(vec3i.below());

        return switch (mat) {
            case OAK_SIGN -> vec3i.subtract(0, 4, 0);
            case SEA_LANTERN -> vec3i.subtract(0, 3, 0);
            case GLOWSTONE -> findBottomLazy(vec3i.below());
            default -> vec3i;
        };
    }

    public static WorldVec3i bottomOfPole(World world, BlockVector2 vec2) {
        int y = world.getHighestBlockYAt(vec2.getX(), vec2.getZ(), HeightMap.WORLD_SURFACE);
        WorldVec3i vec3i = new WorldVec3i(world, vec2.getX(), y, vec2.getZ());
        WorldVec3i result = findBottom(vec3i.clone());

        return result == null ? findBottomLazy(vec3i.clone()) : result;
    }

    public static boolean isValidPolePosition(PopulationRegion region, BlockVector2 vec) {
        FtcBoundingBox valid = region.getBB().shrink(5);

        return MathUtil.isInRange(vec.getX(), valid.getMinX(), valid.getMaxX()) &&
                MathUtil.isInRange(vec.getZ(), valid.getMinZ(), valid.getMaxZ());
    }

    public static void validateWorld(World world) throws CommandSyntaxException {
        if(!world.equals(ComVars.getRegionWorld())) throw FtcExceptionProvider.regionsWrongWorld();
    }

    public static boolean isCloseToPoleOrOp(BlockVector2 pole, CrownUser user) {
        if(user.hasPermission(Permissions.REGIONS_ADMIN)) return true;

        return isCloseToPole(pole, user);
    }

    public static boolean isCloseToPole(BlockVector2 pole, CrownUser user) {
        BlockVector2 vec2 = user.get2DLocation();
        return vec2.distance(pole) <= DISTANCE_TO_POLE;
    }

    public static void validateDistance(BlockVector2 pole, CrownUser user) throws RoyalCommandException {
        if(!isCloseToPoleOrOp(pole, user)) {
            throw FtcExceptionProvider.translatable("regions.tooFar",
                    Component.text("x = " + pole.getX() + " z = " + pole.getZ())
            );
        }
    }
}