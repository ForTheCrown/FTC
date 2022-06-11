package net.forthecrown.regions;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.sk89q.worldedit.math.BlockVector2;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import net.forthecrown.core.Crown;
import net.forthecrown.core.FtcDynmap;
import net.forthecrown.core.FtcVars;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.royalgrenadier.GrenadierUtils;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.utils.math.WorldBounds3i;
import net.forthecrown.utils.math.WorldVec3i;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.bukkit.Material;
import org.bukkit.World;
import org.dynmap.markers.Marker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.UnaryOperator;

public class PopulationRegion implements RegionAccess {
    private final WorldBounds3i region;
    private final RegionPos pos;

    private final Set<RegionProperty> properties = new ObjectOpenHashSet<>();

    private String name;
    private BlockVector2 polePosition;
    private TextColor nameColor;
    private Component description;
    private Bounds3i poleBoundingBox;

    @Getter
    private final RegionResidency residency;

    PopulationRegion(RegionPos pos, World world) {
        this.pos = pos;
        this.region = makeRegion(world);
        this.residency = new RegionResidency(this);
    }

    public PopulationRegion(RegionPos pos, World world, CompoundTag tag) {
        this(pos, world);

        load(tag);

        if(poleBoundingBox == null) updatePoleBounds();
    }

    //Makes a bounding box for the region, from -65 to 312
    private WorldBounds3i makeRegion(World world) {
        int minX = pos.getCenterX() - RegionConstants.HALF_REGION_SIZE;
        int minZ = pos.getCenterZ() - RegionConstants.HALF_REGION_SIZE;

        int maxX = pos.getCenterX() + RegionConstants.HALF_REGION_SIZE;
        int maxZ = pos.getCenterZ() + RegionConstants.HALF_REGION_SIZE;

        return new WorldBounds3i(world, minX, FtcUtils.MIN_Y, minZ, maxX, FtcUtils.MAX_Y, maxZ);
    }

    public void save(CompoundTag tag) {
        if (polePosition != null) tag.put("polePosition", RegionUtil.writeColumn(polePosition));

        if (!FtcUtils.isNullOrBlank(name)) {
            tag.putString("name", name);

            if (nameColor != null) tag.putInt("color", nameColor.value());
            if (description != null) tag.putString("description", ChatUtils.GSON.serialize(description));
        }

        if (!properties.isEmpty()) {
            tag.putInt("properties", RegionProperty.pack(properties));
        }

        if(!residency.isEmpty()) {
            tag.put("residency", residency.save());
        }
    }

    public void load(CompoundTag tags) {
        /*//If the only thing serialized is, is a name
        if(tag.getId() == Tag.TAG_STRING) {
            updatePoleBounds();
            this.name = tag.getAsString();
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
        CompoundTag tags = (CompoundTag) tag;*/

        //Set pole position
        if (tags.contains("polePosition")) {
            int[] arr = tags.getIntArray("polePosition");
            setPolePosition0(BlockVector2.at(arr[0], arr[1]));
        }

        // Properties must be deserialized before name
        if (tags.contains("properties")) {
            properties.clear();
            properties.addAll(RegionProperty.unpack(tags.getInt("properties")));
        }

        if (tags.contains("name")) {
            this.name = tags.getString("name");

            if (tags.contains("description")) this.description = ChatUtils.fromJsonText(tags.getString("description"));
            if (tags.contains("color")) this.nameColor = TextColor.color(tags.getInt("color"));
        }

        if (tags.contains("residency")) {
            residency.load(tags.getList("residency", Tag.TAG_COMPOUND));
        }
    }

    // sets the pole position without generating a new pole or removing the old one
    protected void setPolePosition0(@Nullable BlockVector2 polePosition) {
        this.polePosition = polePosition;
        updatePoleBounds();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean hasProperty(RegionProperty property) {
        return properties.contains(property);
    }

    public void addProperty(RegionProperty property) {
        if(hasProperty(property)) return;

        properties.add(property);
        property.onAdd(this);
    }

    public void removeProperty(RegionProperty property) {
        if(!hasProperty(property)) return;

        properties.remove(property);
        property.onRemove(this);
    }

    @Override
    public boolean hasProperties() {
        return !properties.isEmpty();
    }

    public void setProperty(RegionProperty property, boolean state) {
        if(state) addProperty(property);
        else removeProperty(property);
    }

    /**
     * Gets the position of this region
     * @return The region's position
     */
    public RegionPos getPos() {
        return pos;
    }

    /**
     * Gets the region's description
     * @return The region's description, or null, if there's no description
     */
    public Component getDescription() {
        return description;
    }

    public TextColor getNameColor() {
        return nameColor;
    }

    @Override
    public @NotNull HoverEvent<Component> asHoverEvent(@NotNull UnaryOperator<Component> op) {
        BlockVector2 vec2 = getPolePosition();

        return HoverEvent.showText(
                op.apply(
                        Component.text()
                                .append(descOrEmpty())
                                .append(Component.text("x: " + vec2.getX()))
                                .append(Component.newline())
                                .append(Component.text("z: " + vec2.getZ()))
                                .build()
                )
        );
    }

    private Component descOrEmpty() {
        return description != null ? description.append(Component.newline()) : Component.empty();
    }

    /**
     * Gets either the name or a string representation of the position
     * @return The region's name, or it's position.
     */
    public String nameOrPos() {
        if (hasName()) {
            return getName();
        }

        BlockVector2 polePos = getPolePosition();
        return polePos.getX() + " " + polePos.getZ();
    }

    /**
     * Gets the absolute position of this region's pole
     * @return This region's pole position
     */
    public @NotNull BlockVector2 getPolePosition() {
        return polePosition == null ? pos.toCenter() : polePosition;
    }

    /**
     * Gets the string used for marker ID's.
     * Will always return a non null string.
     * <p></p>
     * Example: region_pole_1_4
     * @return This region's marker ID
     */
    public String getMarkerID() {
        return "region_pole_" + getPos().getX() + "_" + getPos().getZ();
    }

    /**
     * Gets the bounding box of this region
     * @return The region's bounding box
     */
    public WorldBounds3i getBB() {
        return region;
    }

    /**
     * Gets the bounding box of the pole
     * @return The region pole's bounding box
     */
    public Bounds3i getPoleBoundingBox() {
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
        WorldBounds3i prev = poleBoundingBox.toWorldBounds(getWorld());

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
            marker.setLocation(
                    FtcVars.getRegionWorld().getUID().toString(),
                    pos.getX() + 0.5D, getPoleBoundingBox().maxY(), pos.getZ() + 0.5D
            );
        }

        Crown.getRegionManager().getGenerator().generate(this);
    }

    //Updates the pole's bounding box
    protected void updatePoleBounds() {
        poleBoundingBox = RegionUtil.poleBoundingBox(this);
    }

    void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the name's color
     * @param nameColor The new name color
     */
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
    public boolean isImportant() {
        return !residency.isEmpty() || !FtcUtils.isNullOrBlank(name) || polePosition != null;
    }

    /**
     * Gets the pole's bottom position
     * @return The pole's bottom position
     */
    public WorldVec3i getPoleBottom() {
        BlockVector2 vec2 = getPolePosition();

        return new WorldVec3i(getWorld(), vec2.getX(), getPoleBoundingBox().minY(), vec2.getZ());
    }
}