package net.forthecrown.commands.admin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.chat.ComponentTagVisitor;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.inventory.weapon.RoyalSword;
import net.forthecrown.inventory.weapon.RoyalWeapons;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.manager.UserManager;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.inventory.ItemStack;

public class CommandRoyalSword extends FtcCommand {

    public CommandRoyalSword() {
        super("RoyalSword");

        setPermission(Permissions.ADMIN);
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /RoyalSword
     *
     * Permissions used:
     *
     * Main Author:
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(literal("give")
                        .then(argument("owner", UserArgument.user())
                                .executes(c -> {
                                    CrownUser sender = getUserSender(c);
                                    CrownUser user = UserArgument.getUser(c, "owner");

                                    ItemStack item = RoyalWeapons.make(user.getUniqueId());

                                    sender.getInventory().addItem(item);

                                    c.getSource().sendAdmin(
                                            Component.text("Created royal sword for ")
                                                    .append(user.nickDisplayName())
                                    );
                                    return 0;
                                })
                        )
                )

                .then(literal("update")
                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            RoyalSword sword = getSword(user);

                            sword.update();

                            c.getSource().sendAdmin("Updated held sword");
                            return 0;
                        })
                )

                .then(literal("upgrade")
                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            RoyalSword sword = getSword(user);

                            sword.incrementGoal();
                            sword.update();

                            c.getSource().sendAdmin("Upgraded held sword");
                            return 0;
                        })
                )

                .then(literal("data")
                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            RoyalSword sword = getSword(user);

                            CompoundTag data = sword.getData();
                            ComponentTagVisitor visitor = new ComponentTagVisitor(true);

                            c.getSource().sendMessage(
                                    visitor.visit(data, Component.text("Sword data: "))
                            );
                            return 0;
                        })
                )

                .then(literal("get_owner")
                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            RoyalSword sword = getSword(user);

                            CrownUser owner = UserManager.getUser(sword.getOwner());

                            c.getSource().sendMessage(
                                    Component.text("Sword owner: ")
                                            .append(owner.nickDisplayName())
                            );
                            return 0;
                        })
                );
    }

    private static ItemStack getItem(CrownUser user) throws CommandSyntaxException {
        ItemStack itemStack = user.getInventory().getItemInMainHand();

        if(!RoyalWeapons.isRoyalSword(itemStack)) {
            throw FtcExceptionProvider.create("Not holding a royal sword");
        }

        return itemStack;
    }

    private static RoyalSword getSword(CrownUser user) throws CommandSyntaxException {
        return new RoyalSword(getItem(user));
    }
}