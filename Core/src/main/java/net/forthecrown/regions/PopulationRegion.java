package net.forthecrown.regions;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.sk89q.worldedit.math.BlockVector2;
import net.forthecrown.core.Crown;
import net.forthecrown.royalgrenadier.GrenadierUtils;
import net.forthecrown.serializer.NbtSerializable;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.Nameable;
import net.forthecrown.utils.math.FtcBoundingBox;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.UnaryOperator;

public class PopulationRegion implements Nameable, HoverEventSource<Component>, NbtSerializable {
    private static final GsonComponentSerializer SERIALIZER = GsonComponentSerializer.gson();

    private final RegionPos pos;
    private final FtcBoundingBox region;

    private Component description;
    private TextColor nameColor;
    private String name;
    private BlockVector2 polePosition;
    private BoundingBox poleBoundingBox;

    public PopulationRegion(RegionPos pos, World world) {
        this.pos = pos;
        this.region = makeRegion(world);

        updatePoleBounds();
    }

    public PopulationRegion(RegionPos pos, World world, Tag tag) {
        this(pos, world);

        //If the only thing serialized is, is a name
        if(tag.getId() == Tag.TAG_STRING) {
            setName(tag.getAsString());
            return;
        }

        //If the only thing serialized is, is a custom pole position
        if(tag.getId() == Tag.TAG_INT_ARRAY) {
            IntArrayTag intArr = (IntArrayTag) tag;
            int[] arr = intArr.getAsIntArray();

            setPolePosition0(BlockVector2.at(arr[0], arr[1]));
            return;
        }

        //Both name and pole position have been serialized, maybe description too
        CompoundTag tags = (CompoundTag) tag;

        this.name = tags.getString("name");

        //Set pole position
        int[] arr = tags.getIntArray("polePosition");
        setPolePosition0(BlockVector2.at(arr[0], arr[1]));

        //If has description, set it
        if(tags.contains("description")) this.description = SERIALIZER.deserialize(tags.getString("description"));
        if(tags.contains("color")) this.nameColor = TextColor.fromHexString(tags.getString("color"));
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
     * Gets the position of this region
     * @return The region's position
     */
    public RegionPos getPos() {
        return pos;
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
     * Gets the absolute position of this region's pole
     * @return This region's pole position
     */
    public @NotNull BlockVector2 getPolePosition() {
        return polePosition == null ? pos.toCenter() : polePosition;
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
    private void updatePoleBounds() {
        poleBoundingBox = RegionUtil.poleBoundingBox(this);
    }

    @Override
    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    public TextColor getNameColor() {
        return nameColor;
    }

    public void setNameColor(TextColor nameColor) {
        this.nameColor = nameColor;
    }

    /**
     * Gets either the name or a string representation of the position
     * @return The region's name, or it's position.
     */
    public String nameOrPos() {
        return hasName() ? getName() : getPos().toString();
    }

    /**
     * Gets the region's description
     * @return The region's description, or null, if there's no description
     */
    public Component getDescription() {
        return description;
    }

    /**
     * Set's the region's description
     * @param description The region's new description
     */
    public void setDescription(Component description) {
        this.description = description;
    }

    /**
     * Gets the region's display name
     * @return The region's display name with a hover event and a click event, if the region has a name.
     */
    public Component displayName() {
        return Component.text('[' + nameOrPos() + ']')
                .color(nameColor)
                .hoverEvent(this)
                .clickEvent(hasName() ? ClickEvent.suggestCommand("/visit " + getName()) : null);
    }

    @Override
    public @NotNull HoverEvent<Component> asHoverEvent(@NotNull UnaryOperator<Component> op) {
        return HoverEvent.showText(
                op.apply(
                        Component.text()
                                .append(Component.text("x: " + getPos().getCenterX()))
                                .append(Component.newline())
                                .append(Component.text("z: " + getPos().getCenterZ()))
                                .build()
                )
        );
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
     * Saves the region as an NBT tag
     * @see PopulationRegion#shouldSerialize()
     * @return The region's NBT representation, or null, if the region doesn't have enough data to serialize.
     */
    @Override
    public Tag saveAsTag() {
        boolean noPos = polePosition == null;
        boolean noName = FtcUtils.isNullOrBlank(name);

        //Shouldn't serialize
        if(noPos && noName) return null;

        //If it only has a name
        if(noPos && description == null && nameColor == null) {
            return StringTag.valueOf(name);
        }

        //If it only has a position
        if(!noPos && noName) {
            return writeColumn(polePosition);
        }

        //It has a both a name and a custom position.
        CompoundTag tag = new CompoundTag();
        tag.putString("name", name);
        tag.put("polePosition", writeColumn(polePosition));

        //If it also has a description
        if(description != null) tag.putString("description", SERIALIZER.serialize(description));
        if(nameColor != null) tag.putString("color", nameColor.asHexString());

        return tag;
    }

    //Write a block vec 2 as an int array for NBT
    private static IntArrayTag writeColumn(BlockVector2 pos) {
        return new IntArrayTag(new int[] {pos.getX(), pos.getZ()});
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
