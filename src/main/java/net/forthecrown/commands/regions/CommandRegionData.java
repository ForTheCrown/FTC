package net.forthecrown.commands.regions;

import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.regions.RegionAccess;
import net.forthecrown.regions.RegionManager;
import net.forthecrown.regions.RegionPos;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
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
                    RegionManager.get().save(tag);

                    var component = Text.displayTag(tag, true);

                    c.getSource().sendMessage(component);
                    return 0;
                })

                .then(literal("-current")
                        .executes(c -> {
                            User user = getUserSender(c);
                            RegionPos pos = user.getRegionPos();

                            RegionManager manager = RegionManager.get();
                            RegionAccess data = manager.getAccess(pos);

                            CompoundTag tag = new CompoundTag();
                            data.save(tag);

                            if (tag.isEmpty()) {
                                throw Exceptions.REGION_NO_DATA;
                            }

                            var component = Text.displayTag(tag, true);

                            user.sendMessage(
                                    Text.format("Data for region: {0}:{1}",
                                            data.displayName(),
                                            component
                                    )
                            );

                            return 0;
                        })
                );
    }
}