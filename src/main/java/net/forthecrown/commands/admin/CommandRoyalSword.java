package net.forthecrown.commands.admin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.Pair;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.inventory.ExtendedItems;
import net.forthecrown.inventory.weapon.RoyalSword;
import net.forthecrown.utils.text.Text;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.inventory.ItemStack;

public class CommandRoyalSword extends FtcCommand {

    public CommandRoyalSword() {
        super("RoyalSword");

        setPermission(Permissions.ROYAL_SWORD);
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
                        .then(argument("owner", Arguments.USER)
                                .executes(c -> {
                                    User sender = getUserSender(c);
                                    User user = Arguments.getUser(c, "owner");

                                    ItemStack item = ExtendedItems.ROYAL_SWORD.createItem(user.getUniqueId());

                                    sender.getInventory().addItem(item);

                                    c.getSource().sendAdmin(
                                            Component.text("Created royal sword for ")
                                                    .append(user.displayName())
                                    );
                                    return 0;
                                })
                        )
                )

                .then(literal("update")
                        .executes(c -> {
                            User user = getUserSender(c);
                            var swordPair = getSword(user);
                            var item = swordPair.first();
                            var sword = swordPair.second();

                            sword.update(item);

                            c.getSource().sendAdmin("Updated held sword");
                            return 0;
                        })
                )

                .then(literal("data")
                        .executes(c -> {
                            User user = getUserSender(c);
                            var swordPair = getSword(user);
                            var sword = swordPair.second();

                            CompoundTag tag = new CompoundTag();
                            sword.save(tag);

                            user.sendMessage(Text.displayTag(tag, true));
                            return 0;
                        })
                )

                .then(literal("upgrade")
                        .requires(source -> source.hasPermission(Permissions.ADMIN))

                        .executes(c -> {
                            User user = getUserSender(c);

                            var swordPair = getSword(user);
                            var item = swordPair.first();
                            var sword = swordPair.second();

                            sword.incrementRank(item);
                            sword.update(item);

                            c.getSource().sendAdmin("Upgraded held sword");
                            return 0;
                        })
                )

                .then(literal("get_owner")
                        .executes(c -> {
                            User user = getUserSender(c);
                            var swordPair = getSword(user);
                            var sword = swordPair.second();

                            User owner = Users.get(sword.getOwner());

                            c.getSource().sendMessage(
                                    Component.text("Sword owner: ")
                                            .append(owner.displayName())
                            );
                            return 0;
                        })
                );
    }

    private static Pair<ItemStack, RoyalSword> getSword(User user) throws CommandSyntaxException {
        ItemStack held = user.getInventory().getItemInMainHand();

        if (ItemStacks.isEmpty(held)) {
            throw Exceptions.MUST_HOLD_ITEM;
        }

        RoyalSword sword = ExtendedItems.ROYAL_SWORD.get(held);

        if (sword == null) {
            throw Exceptions.NOT_HOLDING_ROYAL_SWORD;
        }

        return Pair.of(held, sword);
    }
}