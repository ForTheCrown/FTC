package net.forthecrown.commands.regions;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.regions.RegionPos;
import net.forthecrown.regions.RegionUtil;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.actions.ActionFactory;
import net.forthecrown.utils.Cooldown;
import net.forthecrown.utils.CrownRandom;
import net.forthecrown.utils.transformation.FtcBoundingBox;
import net.kyori.adventure.text.Component;

public class CommandRandomRegion extends FtcCommand {
    private static final int COOLDOWN_SECONDS = 10;

    public CommandRandomRegion() {
        super("randomregion");

        setPermission(Permissions.REGIONS_ADMIN);
        setDescription("Takes you to a random region");

        register();
    }

    private final CrownRandom random = new CrownRandom();

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /RandomRegion
     *
     * Permissions used: ftc.regions
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    CrownUser user = getUserSender(c);
                    RegionUtil.validateWorld(user.getWorld());

                    if(Cooldown.containsOrAdd(user, getName(), COOLDOWN_SECONDS * 20)) {
                        throw FtcExceptionProvider.translatable("regions.cooldown", Component.text(COOLDOWN_SECONDS));
                    }

                    FtcBoundingBox box = FtcBoundingBox.of(user.getWorld());
                    RegionPos max = RegionPos.of(box.getMaxLocation());
                    RegionPos min = RegionPos.of(box.getMinLocation());

                    RegionPos cords = new RegionPos(
                            random.intInRange(max.getX()-1, min.getX()+1),
                            random.intInRange(max.getZ()-1, min.getZ()+1)
                    );

                    PopulationRegion region = Crown.getRegionManager().get(cords);

                    ActionFactory.visitRegion(user, region);
                    return 0;
                });
    }
}