package net.forthecrown.emperor.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.manager.FtcCommand;
import net.forthecrown.emperor.commands.manager.FtcExceptionProvider;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.EnchantArgument;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class CommandEnchant extends FtcCommand {
    public CommandEnchant(){
        super("enchant", CrownCore.inst());

        setPermission(Permissions.CORE_ADMIN);
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("enchant", EnchantArgument.enchantment())
                        .then(argument("level", IntegerArgumentType.integer(1))
                                .executes(c -> {
                                    CrownUser user = getUserSender(c);
                                    Enchantment ench = c.getArgument("enchant", Enchantment.class);
                                    int level = c.getArgument("level", Integer.class);

                                    ItemStack inHand = user.getPlayer().getInventory().getItemInMainHand();
                                    if(inHand == null || inHand.getType() == Material.AIR) throw FtcExceptionProvider.mustHoldItem();

                                    inHand.addUnsafeEnchantment(ench, level);
                                    c.getSource().sendAdmin(Component.text("Enchanted item in main hand"));
                                    return 0;
                                })
                        )
                );
    }
}
