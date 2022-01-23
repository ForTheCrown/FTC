package net.forthecrown.regions;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.sk89q.worldedit.math.BlockVector2;
import net.forthecrown.core.ComVars;
import net.forthecrown.core.Crown;
import net.forthecrown.core.FtcDynmap;
import net.forthecrown.royalgrenadier.GrenadierUtils;
import net.forthecrown.serializer.NbtSerializable;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.math.WorldVec3i;
import net.forthecrown.utils.transformation.FtcBoundingBox;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.bukkit.Material;
import org.bukkit.World;
import org.dynmap.markers.Marker;
import org.jetbrains.annotations.Nullable;

public class PopulationRegion extends RegionData implements NbtSerializable {
    private final FtcBoundingBox region;

    private BoundingBox poleBoundingBox;

    PopulationRegion(RegionPos pos, World world) {
        super(pos);
        this.region = makeRegion(world);
    }

    public PopulationRegion(RegionPos pos, World world, Tag tag) {
        this(pos, world);

        readTag(tag);

        if(poleBoundingBox == null) updatePoleBounds();
    }

    //Makes a bounding box for the region, from -65 to 312
    private FtcBoundingBox makeRegion(World world) {
        int minX = pos.getCenterX() - RegionConstants.HALF_REGION_SIZE;
        int minZ = pos.getCenterZ() - RegionConstants.HALF_REGION_SIZE;

        int maxX = pos.getCenterX() + RegionConstants.HALF_REGION_SIZE;
        int maxZ = pos.getCenterZ() + RegionConstants.HALF_REGION_SIZE;

        return new FtcBoundingBox(world, minX, FtcUtils.MIN_Y, minZ, maxX, FtcUtils.MAX_Y, maxZ);
    }

    /**
     * Gets the bounding box of this region
     * @return The region's bounding box
     */
    public FtcBoundingBox getBB() {
        return region;
    }

    /**
     * Gets the bounding box of the pole
     * @return The region pole's bounding box
     */
    public BoundingBox getPoleBoundingBox() {
        return poleBoundingBox;
    }

    /**
     * Gets the world this region is in
     * @return The region's world
     */
    public World getWorld() {
        return getBB().getWorld();
    }

    /**
     * Sets the position of this region's pole.
     * @param polePosition The new pole position.
     */
    public void setPolePosition(@Nullable BlockVector2 polePosition) {
        //prev must be created before polePosition0 is called
        FtcBoundingBox prev = FtcBoundingBox.of(getWorld(), poleBoundingBox);

        // Actually sets the pole's position and updates the
        // pole's bounding box
        setPolePosition0(polePosition);

        // But the pole must be destroyed after the pole position is set
        // If this is done before setPolePosition0 is called, the
        // generator will think the poleBottom position is below
        // where it actually is, since we just destroyed the pole
        prev.forEach(b -> b.setType(Material.AIR, false));

        // If the marker isn't null and we're allowed to have marker, move it
        if(hasName() && !hasProperty(RegionProperty.FORBIDS_MARKER)) {
            BlockVector2 pos = polePosition == null ? getPos().toCenter() : polePosition;

            Marker marker = FtcDynmap.getMarker(this);
            marker.setLocation(ComVars.getRegionWorld().getUID().toString(),
                    pos.getX() + 0.5D, getPoleBoundingBox().maxY(), pos.getZ() + 0.5D
            );
        }

        Crown.getRegionManager().getGenerator().generate(this);
    }

    //Updates the pole's bounding box
    @Override
    protected void updatePoleBounds() {
        poleBoundingBox = RegionUtil.poleBoundingBox(this);
    }

    void setName(String name) {
        this.name = name;
    }

    public void setNameColor(TextColor nameColor) {
        this.nameColor = nameColor;
    }

    /**
     * Set's the region's description
     * @param description The region's new description
     */
    public void setDescription(Component description) {
        this.description = description;
    }

    /**
     * Gets a suggestion tooltip for this region
     * @return This region's suggestion tooltip
     */
    public Message suggestionTooltip() {
        if(description != null) return GrenadierUtils.componentToMessage(description);
        BlockVector2 polePos = getPolePosition();

        return new LiteralMessage("x: " + polePos.getX() + ", z: " + polePos.getZ());
    }

    /**
     * Gets whether the region should be serialized
     * <p></p>
     * Whether a region has a description doesn't get counted, as without a name, a description
     * is useless for regions.
     *
     * @return True if the region either has a name or a custom pole position, false otherwise
     */
    public boolean shouldSerialize() {
        return polePosition != null || !FtcUtils.isNullOrBlank(name);
    }

    public WorldVec3i getPoleBottom() {
        BlockVector2 vec2 = getPolePosition();

        return new WorldVec3i(getWorld(), vec2.getX(), getPoleBoundingBox().minY(), vec2.getZ());
    }
}
