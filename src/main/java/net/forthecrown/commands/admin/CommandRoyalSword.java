package net.forthecrown.commands.admin;

import static net.forthecrown.inventory.weapon.ability.WeaponAbility.START_LEVEL;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.Pair;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.arguments.RegistryArguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.inventory.ExtendedItems;
import net.forthecrown.inventory.weapon.RoyalSword;
import net.forthecrown.inventory.weapon.ability.SwordAbilityManager;
import net.forthecrown.inventory.weapon.ability.WeaponAbility;
import net.forthecrown.inventory.weapon.ability.WeaponAbilityType;
import net.forthecrown.user.User;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.inventory.ItemStack;

public class CommandRoyalSword extends FtcCommand {

  private static final RegistryArguments<WeaponAbilityType> ABILITY_ARGUMENT
      = new RegistryArguments<>(
          SwordAbilityManager.getInstance().getRegistry(),
          "Sword Ability"
      );

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
  public void populateUsages(UsageFactory factory) {
    factory.usage("create <owner: player>", "Creates a sword with an <owner>");

    factory.usage("update",
        "Forces your held sword to update",
        "the lore and sword data, this is",
        "normally done after killing any mob",
        "with the sword"
    );

    factory.usage("data", "Views the data of the sword you're holding");
    factory.usage("upgrade", "Upgrades your held sword by 1 level");

    var ability = factory.withPrefix("ability");
    ability.usage("", "Shows the active ability of your held sword");

    ability.usage("<ability> [<level: number(1..)>]")
        .addInfo("Sets the active ability of your held sword")
        .addInfo("If <level> is not set, defaults to %s", START_LEVEL);
  }

  @Override
  protected void createCommand(BrigadierCommand command) {
    command
        .then(literal("create")
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

        .then(literal("ability")
            .executes(c -> {
              User user = getUserSender(c);
              var swordPair = getSword(user);
              var sword = swordPair.second();

              if (sword.getAbility() == null) {
                c.getSource().sendMessage(
                    Component.text("Sword has no ability", NamedTextColor.RED)
                );
                return 0;
              }

              c.getSource().sendMessage(
                  Text.format("&7Sword's ability: &e{0}",
                      sword.getAbility().getType().fullDisplayName(user)
                  )
              );
              return 0;
            })

            .then(argument("type", ABILITY_ARGUMENT)
                .executes(c -> {
                  return setAbility(c, START_LEVEL);
                })

                .then(argument("level", IntegerArgumentType.integer(START_LEVEL))
                    .executes(c -> {
                      int level = c.getArgument("level", Integer.class);
                      return setAbility(c, level);
                    })
                )
            )
        );
  }

  private int setAbility(CommandContext<CommandSource> c, int level)
      throws CommandSyntaxException {
    var user = getUserSender(c);
    var pair = getSword(user);
    var item = pair.first();
    var sword = pair.second();

    @SuppressWarnings("unchecked")
    Holder<WeaponAbilityType> holder = c.getArgument("type", Holder.class);
    WeaponAbility ability = holder.getValue().create();

    if (level > START_LEVEL) {
      ability.setLevel(level);
    }

    int startUses = holder.getValue().getLimit().get(user);
    ability.setRemainingUses(startUses);

    sword.setAbility(ability);
    sword.update(item);

    c.getSource().sendAdmin(
        Text.format("&7Set sword's ability to &e{0}",
            holder.getValue().fullDisplayName(user)
        )
    );
    return 0;
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