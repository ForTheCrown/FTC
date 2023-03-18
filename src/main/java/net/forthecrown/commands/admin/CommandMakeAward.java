package net.forthecrown.commands.admin;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.time.ZonedDateTime;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.user.User;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class CommandMakeAward extends FtcCommand {

  public CommandMakeAward() {
    super("makeaward");

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
  public void createCommand(GrenadierCommand command) {
    command
        .then(argument("award", StringArgumentType.word())
            .then(argument("winner", Arguments.USER)
                .executes(c -> doAward(
                    c,
                    c.getArgument("award", String.class),
                    Arguments.getUser(c, "winner"),
                    Material.RED_TULIP
                ))

                .then(argument("mat", ArgumentTypes.enumType(Material.class))
                    .executes(c -> doAward(
                        c,
                        c.getArgument("award", String.class),
                        Arguments.getUser(c, "winner"),
                        c.getArgument("mat", Material.class)
                    ))
                )
            )
        );
  }

  private int doAward(CommandContext<CommandSource> c, String award, User target, Material material)
      throws CommandSyntaxException {
    User user = getUserSender(c);

    ItemStack awardItem = makeAward(award, target, material);
    user.getPlayer().getInventory().addItem(awardItem.clone());

    user.sendMessage(Component.text("Got award!").color(NamedTextColor.GRAY));
    return 0;
  }

  private ItemStack makeAward(String award, User winner, Material material) {
    return ItemStacks.builder(material, 1)
        .setNameRaw(Component.text("Award for " + award).color(NamedTextColor.YELLOW)
            .decoration(TextDecoration.ITALIC, false))
        .addLoreRaw(
            Component.text("Winner: ")
                .color(NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false)
                .append(winner.displayName())
        )
        .addLoreRaw(
            Text.format("Won at the {0} FTC awards.",
                Text.nonItalic(NamedTextColor.GRAY),
                ZonedDateTime.now().getYear()
            )
        )
        .addEnchant(Enchantment.LOYALTY, 1)
        .build();
  }
}