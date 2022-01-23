package net.forthecrown.commands.admin;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.Crown;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.user.CrownUser;
import net.forthecrown.inventory.ItemStackBuilder;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.EnumArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class CommandMakeAward extends FtcCommand {

    public CommandMakeAward() {
        super("makeaward", Crown.inst());

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /makeaward
     *
     * Permissions used:
     * ftc.commands.makeaward
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("award", StringArgumentType.word())
                        .then(argument("winner", UserArgument.user())
                                .executes(c -> doAward(
                                        c,
                                        c.getArgument("award", String.class),
                                        UserArgument.getUser(c, "winner"),
                                        Material.RED_TULIP
                                ))

                                .then(argument("mat", EnumArgument.of(Material.class))
                                        .executes(c -> doAward(
                                                c,
                                                c.getArgument("award", String.class),
                                                UserArgument.getUser(c, "winner"),
                                                c.getArgument("mat", Material.class)
                                        ))
                                )
                        )
                );
    }

    private int doAward(CommandContext<CommandSource> c, String award, CrownUser target, Material material) throws CommandSyntaxException {
        CrownUser user = getUserSender(c);

        ItemStack awardItem = makeAward(award, target, material);
        user.getPlayer().getInventory().addItem(awardItem.clone());

        user.sendMessage(Component.text("Got award!").color(NamedTextColor.GRAY));
        return 0;
    }

    private ItemStack makeAward(String award, CrownUser winner, Material material){
        return new ItemStackBuilder(material, 1)
                .setName(Component.text("Award for " + award).color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
                .addLore(
                        Component.text("Winner: ")
                                .color(NamedTextColor.GOLD)
                                .decoration(TextDecoration.ITALIC, false)
                                .append(winner.displayName())
                )
                .addLore(
                        Component.text("Won at the 2021 FTC awards.")
                                .color(NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false)
                )
                .addEnchant(Enchantment.LOYALTY, 1)
                .build();
    }
}