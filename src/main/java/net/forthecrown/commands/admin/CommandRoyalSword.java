package net.forthecrown.commands.admin;

import static net.forthecrown.commands.DataCommands.HELD_ITEM_ACCESSOR;
import static net.forthecrown.inventory.ExtendedItems.TAG_CONTAINER;
import static net.forthecrown.inventory.ExtendedItems.TAG_DATA;
import static net.forthecrown.inventory.weapon.ability.WeaponAbility.NO_OVERRIDE;
import static net.forthecrown.inventory.weapon.ability.WeaponAbility.START_LEVEL;
import static net.forthecrown.inventory.weapon.ability.WeaponAbility.UNLIMITED_USES;
import static net.kyori.adventure.text.Component.text;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.Pair;
import net.forthecrown.commands.DataCommands;
import net.forthecrown.commands.DataCommands.DataAccessor;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.arguments.RegistryArguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.inventory.ExtendedItems;
import net.forthecrown.inventory.weapon.RoyalSword;
import net.forthecrown.inventory.weapon.SwordRanks;
import net.forthecrown.inventory.weapon.ability.SwordAbilityManager;
import net.forthecrown.inventory.weapon.ability.WeaponAbility;
import net.forthecrown.inventory.weapon.ability.WeaponAbilityType;
import net.forthecrown.user.User;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.inventory.ItemStack;

public class CommandRoyalSword extends FtcCommand {
  public static final DataAccessor SWORD_DATA_ACCESS
      = DataCommands.offsetAccessor(
          HELD_ITEM_ACCESSOR,
          "tag." + TAG_CONTAINER + "." + TAG_DATA
      );

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
    factory.usage("create <owner: player> [<level>]")
        .addInfo("Creates a sword with an <owner>")
        .addInfo("If <level> is specified, the sword is upgraded to")
        .addInfo("that level");

    factory.usage("update",
        "Forces your held sword to update",
        "the lore and sword data, this is",
        "normally done after killing any mob",
        "with the sword, or after using a sword's",
        "ability"
    );

    factory.usage("upgrade", "Upgrades your held sword by 1 level");

    var ability = factory.withPrefix("ability");
    ability.usage("", "Shows the active ability of your held sword");

    ability.usage("<ability> [<level: number(1..)>]")
        .addInfo("Sets the active ability of your held sword")
        .addInfo("If <level> is not set, defaults to %s", START_LEVEL);

    ability.usage("cooldown <ticks>")
        .addInfo("Adds a cooldown override to the ability of the sword")
        .addInfo("you're holding");

    ability.usage("cooldown remove_override")
        .addInfo("Removes the cooldown override of the sword you're holding");

    ability.usage("remaining_uses <uses: number(0..)>")
        .addInfo("Sets the remaining uses of the ability of the")
        .addInfo("sword you're holding");

    ability.usage("remaining_uses infinite")
        .addInfo("Sets the remaining uses to 'infinite' for the")
        .addInfo("ability of the sword you're holding.")
        .addInfo("This means the ability will never run out of uses");

    var data = factory.withPrefix("data");
    DataCommands.addUsages(data, "Sword", null);
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(literal("create")
            .then(argument("owner", Arguments.USER)
                .executes(c ->  create(c, 0))

                .then(argument("level", IntegerArgumentType.integer(1, SwordRanks.MAX_RANK))
                    .executes(c -> {
                      int level = c.getArgument("level", Integer.class);
                      return create(c, level - 1);
                    })
                )
            )
        )

        .then(literal("update")
            .executes(c -> {
              User user = getUserSender(c);
              var swordPair = getSword(user);
              var item = swordPair.first();
              var sword = swordPair.second();

              sword.update(item);

              c.getSource().sendSuccess(text("Updated held sword"));
              return 0;
            })
        )

        .then(
            DataCommands.dataAccess("Sword", SWORD_DATA_ACCESS)
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

              c.getSource().sendSuccess(text("Upgraded held sword"));
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
                    text("Sword has no ability", NamedTextColor.RED)
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

            .then(literal("cooldown")
                .then(argument("ticks", ArgumentTypes.time())
                    .executes(c -> abilityCooldown(c, false))
                )

                .then(literal("remove")
                    .executes(c -> abilityCooldown(c, true))
                )
            )

            .then(literal("remaining_use")
                .then(argument("uses", IntegerArgumentType.integer(0))
                    .executes(c -> abilityUses(c, false))
                )

                .then(literal("infinite")
                    .executes(c -> abilityUses(c, true))
                )
            )
        );
  }

  private int create(CommandContext<CommandSource> c, int level)
      throws CommandSyntaxException
  {
    User sender = getUserSender(c);
    User user = Arguments.getUser(c, "owner");

    ItemStack item = ExtendedItems.ROYAL_SWORD.createItem(user.getUniqueId());

    if (level > 0) {
      RoyalSword sword = ExtendedItems.ROYAL_SWORD.get(item);
      assert sword != null;

      for (int i = 0; i < level; i++) {
        sword.incrementRank(item);
      }

      sword.update(item);
    }

    sender.getInventory().addItem(item);

    c.getSource().sendSuccess(
        Text.format(
            "Created a royal sword for {0, user}, rank: {1, number, -roman}",
            user, level + 1
        )
    );
    return 0;
  }

  private int abilityUses(CommandContext<CommandSource> c, boolean infinite)
      throws CommandSyntaxException {
    var user = getUserSender(c);
    var pair = getSword(user);
    var item = pair.first();
    var sword = pair.second();

    validateAbility(sword);

    int uses = infinite ? UNLIMITED_USES : c.getArgument("uses", Integer.class);
    sword.getAbility().setRemainingUses(uses);

    sword.update(item);

    if (uses == UNLIMITED_USES) {
      c.getSource().sendSuccess(
          text(
              "Set ability's remaining uses to infinite",
              NamedTextColor.GRAY
          )
      );

    } else {
      c.getSource().sendSuccess(
          Text.format("Set sword ability's remaining uses to &e{0, number}&r.",
              NamedTextColor.GRAY,
              uses
          )
      );
    }

    return 0;
  }

  private int abilityCooldown(CommandContext<CommandSource> c, boolean remove)
      throws CommandSyntaxException {
    var user = getUserSender(c);
    var pair = getSword(user);
    var item = pair.first();
    var sword = pair.second();

    validateAbility(sword);

    long ticks = remove ? NO_OVERRIDE : ArgumentTypes.getTicks(c, "ticks");
    sword.getAbility().setCooldownOverride(ticks);

    sword.update(item);

    if (ticks == NO_OVERRIDE) {
      c.getSource().sendSuccess(
          text(
              "Removed ability cooldown override",
              NamedTextColor.GRAY
          )
      );

    } else {
      c.getSource().sendSuccess(
          Text.format(
              "Set cooldown override to &e{0, number}&r ticks "
                  + "or &e{0, time, -ticks}&r.",
              NamedTextColor.GRAY,
              ticks
          )
      );
    }

    return 0;
  }

  private void validateAbility(RoyalSword sword) throws CommandSyntaxException {
    if (sword.getAbility() == null) {
      throw Exceptions.format("Held sword has no applied ability");
    }
  }

  private int setAbility(CommandContext<CommandSource> c, int level)
      throws CommandSyntaxException {
    var user = getUserSender(c);
    var pair = getSword(user);
    var item = pair.first();
    var sword = pair.second();

    @SuppressWarnings("unchecked")
    Holder<WeaponAbilityType> holder = c.getArgument("type", Holder.class);
    WeaponAbility ability = holder.getValue().create(user, 0);

    if (level > START_LEVEL) {
      ability.setLevel(level);
    }

    sword.setAbility(ability);
    sword.update(item);

    c.getSource().sendSuccess(
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