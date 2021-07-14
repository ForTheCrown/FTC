package net.forthecrown.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.CrownCore;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.grenadier.types.EnchantArgument;
import net.forthecrown.squire.enchantment.RoyalEnchant;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.FtcUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

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
                        .executes(c -> enchant(
                                c.getSource(),
                                getUserSender(c),
                                1,
                                c.getArgument("enchant", Enchantment.class))
                        )

                        .then(argument("level", IntegerArgumentType.integer(1))
                                .suggests((c, b) -> {
                                    try {
                                        Enchantment ench = c.getArgument("enchant", Enchantment.class);
                                        int max = ench.getMaxLevel();
                                        if(max == 0 || max == 1) return Suggestions.empty();

                                        List<String> strings = new ArrayList<>();

                                        for (int i = 0; i < max; i++) {
                                            strings.add(i + "");
                                        }

                                        return CompletionProvider.suggestMatching(b, strings);
                                    } catch (Exception e){
                                        return Suggestions.empty();
                                    }
                                })

                                .executes(c -> {
                                    CrownUser user = getUserSender(c);
                                    Enchantment ench = c.getArgument("enchant", Enchantment.class);
                                    int level = c.getArgument("level", Integer.class);

                                    return enchant(c.getSource(), user, level, ench);
                                })
                        )
                );
    }

    private int enchant(CommandSource source, CrownUser user, int level, Enchantment ench) throws RoyalCommandException {
        ItemStack inHand = user.getPlayer().getInventory().getItemInMainHand();
        if(FtcUtils.isItemEmpty(inHand)) throw FtcExceptionProvider.mustHoldItem();

        if(ench instanceof RoyalEnchant) RoyalEnchant.addCrownEnchant(inHand, (RoyalEnchant) ench, level);
        else inHand.addUnsafeEnchantment(ench, level);

        source.sendAdmin(Component.text("Enchanted item in main hand"));
        return 0;
    }
}
