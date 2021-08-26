package net.forthecrown.regions;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sk89q.worldedit.math.BlockVector2;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.ComVars;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.math.FtcBoundingBox;
import net.forthecrown.utils.math.MathUtil;
import net.forthecrown.utils.math.WorldVec3i;
import net.kyori.adventure.text.Component;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.bukkit.Material;
import org.bukkit.World;

import static net.forthecrown.regions.RegionConstants.DISTANCE_TO_POLE;

public final class RegionUtil {
    private RegionUtil() {}

    /**
     * Does sum fuckery to find a region pole's bounding box
     * @param region The region to find the bounding box for
     * @return The bounding box
     */
    public static BoundingBox poleBoundingBox(PopulationRegion region) {
        BlockVector2 cords = region.getPolePosition();
        int y = region.getWorld().getHighestBlockYAt(cords.getX(), cords.getZ());

        WorldVec3i p = findBottomOfPole(new WorldVec3i(region.getWorld(), cords.getX(), y, cords.getZ()));

        return new BoundingBox(p.x - 2, p.y, p.z-2, p.x + 2, p.y + 5, p.z + 2);
    }

    public static WorldVec3i findBottomOfPole(WorldVec3i p) {
        Material mat = p.getMaterial();

        return switch (mat) {
            case OAK_SIGN -> p.subtract(0, 4, 0);
            case SEA_LANTERN -> p.subtract(0, 3, 0);
            case GLOWSTONE -> findBottomOfPole(p.below());
            default -> p;
        };
    }

    public static boolean isValidPolePosition(PopulationRegion region, BlockVector2 vec) {
        FtcBoundingBox valid = region.getBB().shrink(5);

        return MathUtil.isInRange(vec.getX(), valid.getMinX(), valid.getMaxX()) &&
                MathUtil.isInRange(vec.getZ(), valid.getMinZ(), valid.getMaxZ());
    }

    public static void validateWorld(World world) throws CommandSyntaxException {
        if(!world.equals(ComVars.getRegionWorld())) throw FtcExceptionProvider.regionsWrongWorld();
    }

    public static boolean isCloseToPole(BlockVector2 pole, CrownUser user) {
        if(user.hasPermission(Permissions.REGIONS_ADMIN)) return true;

        BlockVector2 vec2 = user.get2DLocation();
        return vec2.distance(pole) <= DISTANCE_TO_POLE;
    }

    public static void validateDistance(BlockVector2 pole, CrownUser user) throws RoyalCommandException {
        if(!isCloseToPole(pole, user)) {
            throw FtcExceptionProvider.translatable("regions.tooFar",
                    Component.text("x = " + pole.getX() + " z = " + pole.getZ())
            );
        }
    }
}