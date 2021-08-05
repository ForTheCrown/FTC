package net.forthecrown.regions;

import net.forthecrown.utils.Nameable;
import net.forthecrown.utils.math.FtcRegion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.function.UnaryOperator;

public class PoleRegion implements Nameable, HoverEventSource<Component> {
    private final RegionCords cords;
    private final FtcRegion region;
    private final String name;

    public PoleRegion(RegionCords cords, World world, String name) {
        this.cords = cords;
        this.region = makeRegion(world);
        this.name = name;
    }

    private FtcRegion makeRegion(World world) {
        int minX = cords.getAbsoluteX() - RegionCords.HALF_REGION_SIZE;
        int minZ = cords.getAbsoluteZ() - RegionCords.HALF_REGION_SIZE;

        int maxX = cords.getAbsoluteX() + RegionCords.HALF_REGION_SIZE;
        int maxZ = cords.getAbsoluteZ() + RegionCords.HALF_REGION_SIZE;

        return new FtcRegion(world, minX, -65, minZ, maxX, 312, maxZ);
    }

    public RegionCords getCords() {
        return cords;
    }

    public FtcRegion getRegion() {
        return region;
    }

    @Override
    public String getName() {
        return name;
    }

    public Component displayName() {
        return Component.text('[' + getName() + ']')
                .hoverEvent(this)
                .clickEvent(ClickEvent.suggestCommand("/visit " + getName()));
    }

    @Override
    public @NotNull HoverEvent<Component> asHoverEvent(@NotNull UnaryOperator<Component> op) {
        return HoverEvent.showText(
                op.apply(
                        Component.text()
                                .append(Component.text("x: " + getCords().getAbsoluteX()))
                                .append(Component.newline())
                                .append(Component.text("z: " + getCords().getAbsoluteZ()))
                                .build()
                )
        );
    }
}
