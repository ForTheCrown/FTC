package net.forthecrown.regions;

import com.sk89q.worldedit.math.BlockVector2;
import net.forthecrown.serializer.NbtSerializable;
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
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class RegionData implements Nameable, HoverEventSource<Component>, NbtSerializable {
    private static final GsonComponentSerializer SERIALIZER = GsonComponentSerializer.gson();

    protected final RegionPos pos;

    protected String name;
    protected BlockVector2 polePosition;
    protected TextColor nameColor;
    protected Component description;

    public RegionData(String name, RegionPos pos, BlockVector2 polePosition, TextColor nameColor, Component description) {
        this.name = name;
        this.pos = pos;
        this.polePosition = polePosition;
        this.nameColor = nameColor;
        this.description = description;
    }

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

        if(polePosition != null) tag.put("polePosition", writeColumn(polePosition));
        if(description != null) tag.putString("description", SERIALIZER.serialize(description));
        if(nameColor != null) tag.putString("color", nameColor.asHexString());

        return tag;
    }

    protected void readTag(Tag tag, Consumer<BlockVector2> polePositionConsumer) {
        //If the only thing serialized is, is a name
        if(tag.getId() == Tag.TAG_STRING) {
            name = tag.getAsString();
            return;
        }

        //If the only thing serialized is, is a custom pole position
        if(tag.getId() == Tag.TAG_INT_ARRAY) {
            IntArrayTag intArr = (IntArrayTag) tag;
            int[] arr = intArr.getAsIntArray();

            polePositionConsumer.accept((BlockVector2.at(arr[0], arr[1])));
            return;
        }

        //Both name and pole position have been serialized, maybe description too
        CompoundTag tags = (CompoundTag) tag;

        this.name = tags.getString("name");

        //Set pole position
        int[] arr = tags.getIntArray("polePosition");
        polePositionConsumer.accept(BlockVector2.at(arr[0], arr[1]));

        //If has description, set it
        if(tags.contains("description")) this.description = SERIALIZER.deserialize(tags.getString("description"));
        if(tags.contains("color")) this.nameColor = TextColor.fromHexString(tags.getString("color"));
    }

    private static IntArrayTag writeColumn(BlockVector2 pos) {
        return new IntArrayTag(new int[] {pos.getX(), pos.getZ()});
    }

    @Override
    public String getName() {
        return name;
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
        return HoverEvent.showText(
                op.apply(
                        Component.text()
                                .append(descOrEmpty())
                                .append(Component.text("x: " + getPos().getCenterX()))
                                .append(Component.newline())
                                .append(Component.text("z: " + getPos().getCenterZ()))
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
        return hasName() ? getName() : getPos().toString();
    }

    /**
     * Gets the absolute position of this region's pole
     * @return This region's pole position
     */
    public @NotNull BlockVector2 getPolePosition() {
        return polePosition == null ? pos.toCenter() : polePosition;
    }
}
