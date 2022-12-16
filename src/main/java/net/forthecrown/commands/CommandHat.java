package net.forthecrown.commands;

import net.forthecrown.commands.manager.Commands;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CommandHat extends FtcCommand {

    public CommandHat() {
        super("Hat");

        setPermission(Permissions.HAT);
        setDescription("Places the item in your hand on your head");
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /Hat
     *
     * Permissions used:
     *
     * Main Author:
     */

    private static final Sound SOUND = Sound.sound(
            org.bukkit.Sound.ITEM_ARMOR_EQUIP_NETHERITE,
            Sound.Source.PLAYER,
            1f, 1f
    );

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    Player player = c.getSource().asPlayer();
                    ItemStack held = Commands.getHeldItem(player);

                    var inventory = player.getInventory();
                    ItemStack helmet = inventory.getHelmet();

                    inventory.setHelmet(held);
                    inventory.setItemInMainHand(helmet);

                    player.playSound(SOUND);
                    return 0;
                });
    }
}