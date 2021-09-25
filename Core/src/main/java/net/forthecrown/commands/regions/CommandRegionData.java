package net.forthecrown.commands.regions;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.chat.ComponentTagVisitor;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.regions.RegionPos;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.math.FtcBoundingBox;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.nbt.CompoundTag;

public class CommandRegionData extends FtcCommand {

    public CommandRegionData() {
        super("regiondata");

        setPermission(Permissions.REGIONS_ADMIN);
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /RegionData
     *
     * Permissions used:
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    CompoundTag tag = new CompoundTag();
                    Crown.getRegionManager().save(tag);

                    ComponentTagVisitor visitor = new ComponentTagVisitor(true);
                    TextComponent component = visitor.visit(tag, Component.text("Data for all saved regions: "));

                    c.getSource().sendMessage(component);
                    return 0;
                })

                .then(literal("current")
                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            RegionPos pos = user.getRegionCords();
                            PopulationRegion region = Crown.getRegionManager().get(pos);
                            FtcBoundingBox box = region.getBB();

                            Component boxText = Component.text()
                                    .content("{")
                                    .append(Component.text("\n  minX: " + box.getMinX()))
                                    .append(Component.text("\n  minY: " + box.getMinY()))
                                    .append(Component.text("\n  minZ: " + box.getMinZ()))
                                    .append(Component.text("\n  maxX: " + box.getMaxX()))
                                    .append(Component.text("\n  maxY: " + box.getMaxY()))
                                    .append(Component.text("\n  maxZ: " + box.getMaxZ()))
                                    .append(Component.text("\n}"))
                                    .build();

                            user.sendMessage(
                                    Component.text("Your region cords " + pos)
                                            .append(Component.newline())
                                            .append(Component.text("The region you're in " + region.nameOrPos()))
                                            .append(Component.newline())
                                            .append(Component.text("Region bounds: ").append(boxText))
                                            .append(Component.newline())
                                            .append(Component.text("polePosition: " + region.getPolePosition()))
                            );
                            return 0;
                        })
                );
    }
}