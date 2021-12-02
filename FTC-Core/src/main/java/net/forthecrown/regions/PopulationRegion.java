package net.forthecrown.regions;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.sk89q.worldedit.math.BlockVector2;
import net.forthecrown.core.Crown;
import net.forthecrown.royalgrenadier.GrenadierUtils;
import net.forthecrown.serializer.NbtSerializable;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.math.FtcBoundingBox;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.Nullable;

public class PopulationRegion extends RegionData implements NbtSerializable {
    private final FtcBoundingBox region;

    private BoundingBox poleBoundingBox;

    PopulationRegion(RegionPos pos, World world) {
        super(null, pos, null, null, null);
        this.region = makeRegion(world);
    }

    public PopulationRegion(RegionPos pos, World world, Tag tag) {
        this(pos, world);

        readTag(tag, this::setPolePosition0);

        if(poleBoundingBox == null) updatePoleBounds();
    }

    //Makes a bounding box for the region, from -65 to 312
    private FtcBoundingBox makeRegion(World world) {
        int minX = pos.getCenterX() - RegionConstants.HALF_REGION_SIZE;
        int minZ = pos.getCenterZ() - RegionConstants.HALF_REGION_SIZE;

        int maxX = pos.getCenterX() + RegionConstants.HALF_REGION_SIZE;
        int maxZ = pos.getCenterZ() + RegionConstants.HALF_REGION_SIZE;

        return new FtcBoundingBox(world, minX, -65, minZ, maxX, 312, maxZ);
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
        FtcBoundingBox prev = FtcBoundingBox.of(getWorld(), poleBoundingBox);
        prev.expand(BlockFace.SOUTH_EAST, 1);
        prev.forEach(b -> b.setType(Material.AIR));

        setPolePosition0(polePosition);

        Crown.getRegionManager().getGenerator().generate(this);
    }

    //sets the pole position without generating a new pole or removing the old one
    private void setPolePosition0(@Nullable BlockVector2 polePosition) {
        this.polePosition = polePosition;
        updatePoleBounds();
    }

    //Updates the pole's bounding box
    void updatePoleBounds() {
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
}
