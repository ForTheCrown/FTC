package net.forthecrown.commands.regions;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.chat.ComponentTagVisitor;
import net.forthecrown.grenadier.command.BrigadierCommand;
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
                });
    }
}