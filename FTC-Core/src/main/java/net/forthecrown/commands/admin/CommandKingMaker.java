package net.forthecrown.commands.admin;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.Kingship;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.inventory.crown.Crowns;
import net.forthecrown.inventory.crown.RoyalCrown;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CommandKingMaker extends FtcCommand {

    public CommandKingMaker(){
        super("kingmaker", Crown.inst());

        setDescription("This command is used to assign and unassign a king or queen");
        setPermission(Permissions.KING_MAKER);

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Makes a player a king or queen, or removes
     * the current king or queen.
     *
     * Valid usages of command:
     * - /kingmaker remove
     * - /kingmaker <player> [king | queen]
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    c.getSource().sendMessage("The Current king is " + Crown.getKingship().getName());
                    return 0;
                })
                .then(literal("remove")
                        .executes(c ->{
                            if(Crown.getKingship().getUniqueId() == null) throw FtcExceptionProvider.create("There is already no king");

                            String removeGroupCmd = "lp user " + Crown.getKingship().getUser().getName() + " parent remove king";
                            Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), removeGroupCmd);

                            Crown.getKingship().set(null);
                            c.getSource().sendMessage("King has been removed");
                            return 0;
                        })
                )
                .then(argument("player", UserArgument.user())
                        .then(literal("queen").executes(c -> makeKing(c, true)))
                        .then(literal("king").executes(c -> makeKing(c, false)))
                )

                .then(literal("crown")
                        .then(literal("make")
                                .then(argument("owner", UserArgument.user())
                                        .executes(makeCommand(false))

                                        .then(literal("queen")
                                                .executes(makeCommand(true))
                                        )
                                )
                        )

                        .then(literal("force_update")
                                .executes(c -> {
                                    Player player = c.getSource().asPlayer();
                                    ItemStack held = player.getInventory().getItemInMainHand();

                                    if(!Crowns.isCrown(held)) {
                                        throw FtcExceptionProvider.create("Held item is not crown");
                                    }

                                    RoyalCrown crown = new RoyalCrown(held);
                                    crown.update();

                                    c.getSource().sendAdmin("Updated crown");
                                    return 0;
                                })
                        )

                        .then(literal("gender_flip")
                                .executes(c -> {
                                    Player player = c.getSource().asPlayer();
                                    ItemStack held = player.getInventory().getItemInMainHand();

                                    if(!Crowns.isCrown(held)) {
                                        throw FtcExceptionProvider.create("Held item is not crown");
                                    }

                                    RoyalCrown crown = new RoyalCrown(held);
                                    crown.setQueen(!crown.isQueen());
                                    crown.update();

                                    c.getSource().sendAdmin("Set crown to have " + (crown.isQueen() ? "Queen" : "King") + " owner");
                                    return 0;
                                })
                        )

                        .then(literal("upgrade")
                                .executes(c -> {
                                    Player player = c.getSource().asPlayer();
                                    ItemStack held = player.getInventory().getItemInMainHand();

                                    if(!Crowns.isCrown(held)) {
                                        throw FtcExceptionProvider.create("Held item is not crown");
                                    }

                                    RoyalCrown crown = new RoyalCrown(held);
                                    crown.upgrade();

                                    c.getSource().sendAdmin("Upgraded crown");
                                    return 0;
                                })
                        )
                );
    }

    private Command<CommandSource> makeCommand(boolean queen) {
        return c -> {
            Player player = c.getSource().asPlayer();
            CrownUser user = UserArgument.getUser(c, "owner");

            if(player.getInventory().firstEmpty() == -1) {
                throw FtcExceptionProvider.inventoryFull();
            }

            ItemStack crown = Crowns.make(user.getUniqueId(), queen);
            player.getInventory().addItem(crown);

            c.getSource().sendAdmin("Made crown for " + user.getNickOrName());
            return 0;
        };
    }

    private int makeKing(CommandContext<CommandSource> c, boolean isQueen) throws CommandSyntaxException {
        Kingship kingship = Crown.getKingship();
        if(kingship.getUniqueId() != null) throw FtcExceptionProvider.create("There already is a king");

        CrownUser king = UserArgument.getUser(c, "player");

        CrownUser previous = kingship.getUser();
        if(previous != null) {
            String addGroupCmd = "lp user " + previous.getName() + " parent remove " + ( kingship.isFemale() ? "queen" : "king" );
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), addGroupCmd);
        }

        kingship.set(king.getUniqueId());
        kingship.setFemale(isQueen);

        String addGroupCmd = "lp user " + king.getName() + " parent add " + ( isQueen ? "queen" : "king" );
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), addGroupCmd);

        c.getSource().sendAdmin(
                king.displayName()
                        .append(Component.text(" is the new "))
                        .append(kingship.getPrefix())
                        .append(Component.text(":D"))
        );
        return 0;
    }
}
