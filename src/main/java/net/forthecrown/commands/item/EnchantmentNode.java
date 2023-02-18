package net.forthecrown.commands.item;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.Messages;
import net.forthecrown.dungeons.enchantments.FtcEnchant;
import net.forthecrown.dungeons.enchantments.FtcEnchants;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.grenadier.types.EnchantArgument;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

public class EnchantmentNode extends ItemModifierNode {

  private static final int MIN_LEVEL = 1;
  private static final String ENCHANTMENT_ARG = "enchantment";

  public EnchantmentNode() {
    super("enchant");
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("<enchantment> [<level: number(1..)>]")
        .addInfo("Applies the <enchantment> to your held item")
        .addInfo("with [level]. If [level] is not given, then")
        .addInfo("a level of 1 is used.")
        .addInfo("If the item is a book, enchanting it turns it")
        .addInfo("into an enchanted book");

    factory.usage("clear")
        .addInfo("Clears all your held item's enchantments");
  }

  @Override
  public void create(LiteralArgumentBuilder<CommandSource> command) {
    command
        .then(literal("clear")
            .executes(c -> {
              var item = getHeld(c.getSource());
              var meta = item.getItemMeta();

              for (var e : meta.getEnchants().keySet()) {
                meta.removeEnchant(e);
              }

              item.setItemMeta(meta);

              c.getSource().sendMessage(Messages.CLEARED_ENCHANTMENTS);
              return 0;
            })
        )

        .then(argument(ENCHANTMENT_ARG, EnchantArgument.enchantment())
            .executes(c -> enchant(c, MIN_LEVEL))

            .then(argument("level", IntegerArgumentType.integer(MIN_LEVEL))
                .suggests((context, builder) -> {
                  var ench = getEnchantment(context);

                  if (ench.getMaxLevel() <= MIN_LEVEL) {
                    return Suggestions.empty();
                  }

                  var token = builder.getRemainingLowerCase();
                  for (int i = MIN_LEVEL; i <= ench.getMaxLevel(); i++) {
                    var suggestion = i + "";

                    if (CompletionProvider.startsWith(token, suggestion)) {
                      builder.suggest(suggestion);
                    }
                  }

                  return builder.buildFuture();
                })

                .executes(c -> {
                  var level = c.getArgument("level", Integer.class);
                  return enchant(c, level);
                })
            )

            .then(literal("remove")
                .executes(c -> {
                  var held = getHeld(c.getSource());
                  var enchantment = getEnchantment(c);

                  if (!held.containsEnchantment(enchantment)) {
                    throw Exceptions.enchantNotFound(enchantment);
                  }

                  held.removeEnchantment(enchantment);

                  c.getSource().sendAdmin(Messages.removedEnchant(enchantment));
                  return 0;
                })
            )
        );
  }

  Enchantment getEnchantment(CommandContext<CommandSource> c) {
    return c.getArgument(ENCHANTMENT_ARG, Enchantment.class);
  }

  int enchant(CommandContext<CommandSource> c, int level) throws CommandSyntaxException {
    var item = getHeld(c.getSource());
    var enchantment = getEnchantment(c);

    if (item.getType() == Material.BOOK) {
      item.setType(Material.ENCHANTED_BOOK);
    }

    if (item.getEnchantmentLevel(enchantment) >= level) {
      throw Exceptions.ENCH_MUST_BE_BETTER;
    }

    var meta = item.getItemMeta();

    if (meta instanceof EnchantmentStorageMeta storageMeta) {

      if (enchantment instanceof FtcEnchant ftcEnchant) {
        FtcEnchants.addEnchant(meta, ftcEnchant, level);
      } else {
        storageMeta.addStoredEnchant(enchantment, level, true);
      }

    } else {
      if (enchantment instanceof FtcEnchant ftcEnchant) {
        FtcEnchants.addEnchant(meta, ftcEnchant, level);
      } else {
        meta.addEnchant(enchantment, level, true);
      }
    }

    item.setItemMeta(meta);
    c.getSource().sendAdmin(Messages.addedEnchant(enchantment, level));
    return 0;
  }
}