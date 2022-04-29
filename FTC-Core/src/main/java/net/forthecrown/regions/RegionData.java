package net.forthecrown.regions;

import com.sk89q.worldedit.math.BlockVector2;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.Nameable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.UnaryOperator;

public abstract class RegionData implements Nameable, HoverEventSource<Component> {
    private static final GsonComponentSerializer SERIALIZER = GsonComponentSerializer.gson();

    protected final RegionPos pos;
    protected final Set<RegionProperty> properties = new ObjectOpenHashSet<>();

    protected String name;
    protected BlockVector2 polePosition;
    protected TextColor nameColor;
    protected Component description;

    public RegionData(RegionPos pos) {
        this.pos = pos;
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

        saveExtraData(tag);
    }

    protected void readTag(Tag tag) {
        //If the only thing serialized is, is a name
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
        CompoundTag tags = (CompoundTag) tag;

        //Set pole position
        if(tags.contains("polePosition")) {
            int[] arr = tags.getIntArray("polePosition");
            setPolePosition0(BlockVector2.at(arr[0], arr[1]));
        }

        // Properties must be deserialized before name
        if(tags.contains("properties")) {
            properties.clear();
            properties.addAll(RegionProperty.unpack(tags.getInt("properties")));
        }

        this.name = tags.getString("name");

        //If has description, set it
        if(tags.contains("description")) this.description = SERIALIZER.deserialize(tags.getString("description"));
        if(tags.contains("color")) this.nameColor = TextColor.color(tags.getInt("color"));

        loadExtraData(tags);
    }

    protected void saveExtraData(CompoundTag tag) {}
    protected void loadExtraData(CompoundTag tag) {}

    // sets the pole position without generating a new pole or removing the old one
    protected void setPolePosition0(@Nullable BlockVector2 polePosition) {
        this.polePosition = polePosition;
        updatePoleBounds();
    }

    protected abstract void updatePoleBounds();

    @Override
    public String getName() {
        return name;
    }

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

    public static class Empty extends RegionData {
        public Empty(RegionPos pos) {
            super(pos);
        }

        @Override
        protected void updatePoleBounds() {

        }
    }
}