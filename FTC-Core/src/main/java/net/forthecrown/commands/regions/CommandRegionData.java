package net.forthecrown.commands.regions;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.chat.ComponentTagVisitor;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.regions.RegionData;
import net.forthecrown.regions.RegionManager;
import net.forthecrown.regions.RegionPos;
import net.forthecrown.user.CrownUser;
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

                .then(literal("-current")
                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            RegionPos pos = user.getRegionPos();

                            RegionManager manager = Crown.getRegionManager();
                            RegionData data = manager.getData(pos);

                            if(data instanceof RegionData.Empty) {
                                throw FtcExceptionProvider.create("Region has no data");
                            }

                            CompoundTag tag = new CompoundTag();
                            data.save(tag);

                            ComponentTagVisitor visitor = new ComponentTagVisitor(true);

                            user.sendMessage(
                                    visitor.visit(tag, Component.text("Data for ").append(data.displayName()))
                            );

                            return 0;
                        })
                );
    }
}