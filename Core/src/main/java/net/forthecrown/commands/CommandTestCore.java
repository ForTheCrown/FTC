package net.forthecrown.commands;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.inventory.weapon.RoyalWeapons;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.animation.AnimationBuilder;
import net.forthecrown.utils.animation.BlockAnimation;
import net.forthecrown.utils.math.BoundingBoxes;
import net.forthecrown.utils.math.Vector3i;
import net.forthecrown.utils.math.Vector3iOffset;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.core.Direction;

public class CommandTestCore extends FtcCommand {

    static final BlockAnimation TEST_ANIM = new AnimationBuilder("ftccore:test_animation")
            .setTicksPerFrame(10)
            .addFrames(BoundingBoxes.createArray(
                    new Vector3i(273, 4, 219),
                    new Vector3iOffset(5, 5, 5),
                    Direction.EAST,
                    1, 12
            ))
            .buildAndRegister();

    public CommandTestCore(){
        super("coretest", Crown.inst());

        setAliases("testcore");
        setPermission(Permissions.FTC_ADMIN);
        register();
    }

    @Override
    public boolean test(CommandSource sender) { //test method used by Brigadier to determine who can use the command, from Predicate interface
        return sender.asBukkit().isOp() && testPermissionSilent(sender.asBukkit());
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(c -> {
            CrownUser user = getUserSender(c);
            Component gradient = FtcFormatter.gradientText("Giving RoyalSword", NamedTextColor.RED, NamedTextColor.BLUE);

            user.getInventory().addItem(RoyalWeapons.make(user.getUniqueId()));

            user.sendMessage(gradient);
            return 0;
        });
    }
}
