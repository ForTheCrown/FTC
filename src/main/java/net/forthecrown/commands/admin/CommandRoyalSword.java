package net.forthecrown.commands.admin;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.Pair;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.RegistryArgument;
import net.forthecrown.grenadier.types.args.ArgsArgument;
import net.forthecrown.grenadier.types.args.Argument;
import net.forthecrown.grenadier.types.args.ParsedArgs;
import net.forthecrown.inventory.ExtendedItems;
import net.forthecrown.inventory.weapon.RoyalSword;
import net.forthecrown.inventory.weapon.WeaponAbility;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

public class CommandRoyalSword extends FtcCommand {

  public static final Argument<PotionEffectType> POT_TYPE
      = Argument.builder("type", RegistryArgument.registry(Registry.POTION_EFFECT_TYPE))
          .build();

  public static final Argument<Integer> LEVEL
      = Argument.builder("level", IntegerArgumentType.integer(1)).build();

  public static final Argument<Integer> DURATION
      = Argument.builder("duration", IntegerArgumentType.integer(1)).build();

  public static final ArgsArgument ARGS = ArgsArgument.builder()
      .addRequired(LEVEL)
      .addRequired(POT_TYPE)
      .addRequired(DURATION)
      .build();

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

        .then(literal("ability")
            .then(argument("args", ARGS)
                .executes(c -> {
                  User user = getUserSender(c);
                  var pair = getSword(user);

                  ItemStack item = pair.first();
                  RoyalSword sword = pair.second();

                  var args = c.getArgument("args", ParsedArgs.class);
                  int level = args.get(LEVEL);
                  int duration = args.get(DURATION);
                  PotionEffectType type = args.get(POT_TYPE);

                  WeaponAbility ability = new WeaponAbility(
                      type, level, duration
                  );

                  sword.setAbility(ability);
                  sword.update(item);

                  c.getSource().sendAdmin("Applied ability to weapon");
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