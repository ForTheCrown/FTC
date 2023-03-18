package net.forthecrown.commands.user;

import static net.forthecrown.utils.text.Text.format;

import com.google.common.base.Joiner;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.commands.arguments.RegistryArguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.cosmetics.Cosmetic;
import net.forthecrown.cosmetics.CosmeticType;
import net.forthecrown.cosmetics.Cosmetics;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.user.User;
import net.forthecrown.user.data.CosmeticData;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.writer.TextWriters;

class UserCosmeticsNode extends UserCommandNode {

  public UserCosmeticsNode() {
    super("user_cosmetics", "cosmetics");

    setAliases("user_effects", "usercosmetics", "usereffects");
  }

  @Override
  void createUsages(UsageFactory factory) {
    var prefixed = factory.withPrefix("<effect type>");

    prefixed.usage("", "Shows a user's active and available <effect types>");
    prefixed.usage("set <cosmetic>", "Sets the user's active <effect type>");
    prefixed.usage("unset", "Clears the user's active <effect type> cosmetic");

    prefixed.usage("clear", "Clears the user's available")
            .addInfo("<effect type> cosmetics");

    prefixed.usage("add <cosmetic>", "Adds the <cosmetic> to the user's")
        .addInfo("cosmetic effect list");

    prefixed.usage("remove <cosmetic>")
        .addInfo("Removes the <cosmetic> from the user's")
        .addInfo("available cosmetics list");
  }

  @Override
  protected <T extends ArgumentBuilder<CommandSource, T>> void create(T command,
                                                                      UserProvider provider
  ) {
    for (var type : Registries.COSMETIC) {
      if (type.equals(Cosmetics.EMOTE)) {
        continue;
      }

      command.then(typeArguments(type, provider));
    }
  }

  private <T extends Cosmetic> LiteralArgumentBuilder<CommandSource> typeArguments(
      CosmeticType<T> type,
      UserProvider provider
  ) {
    var argument = of(type);

    return literal(type.getName().replaceAll("_effects", ""))
        .executes(c -> {
          User user = provider.get(c);
          CosmeticData data = user.getCosmeticData();

          var available = data.getAvailable(type);
          var active = data.get(type);

          if (active == null && available.isEmpty()) {
            throw Exceptions.NOTHING_TO_LIST;
          }

          var writer = TextWriters.newWriter();
          writer.formatted("{0, user}'s {1} effects:", user, type.getDisplayName());
          writer.field("Active", active == null ? "UNSET" : active.getSerialId());

          writer.field("Available", Joiner.on(", ").join(available));

          c.getSource().sendMessage(writer);
          return 0;
        })

        .then(literal("set")
            .then(argument("cosmetic", argument)
                .executes(c -> {
                  User user = provider.get(c);
                  CosmeticData data = user.getCosmeticData();
                  Holder<T> effect = c.getArgument("cosmetic", Holder.class);

                  data.set(type, effect.getValue());

                  c.getSource().sendSuccess(
                      format("Set {0, user} active {1} cosmetic to {2}",
                          user, type.getDisplayName(), effect.getValue()
                      )
                  );
                  return 0;
                })
            )
        )

        .then(literal("unset")
            .executes(c -> {
              User user = provider.get(c);
              CosmeticData data = user.getCosmeticData();

              data.set(type, null);

              c.getSource().sendSuccess(
                  format("Unset {0, user} active {1} cosmetic.",
                      user, type.getDisplayName()
                  )
              );
              return 0;
            })
        )

        .then(literal("clear")
            .executes(c -> {
              User user = provider.get(c);
              CosmeticData data = user.getCosmeticData();

              data.clear(type);

              c.getSource().sendSuccess(
                  format("Cleared {0, user}'s available {1} cosmetics",
                      user, type.getDisplayName()
                  )
              );
              return 0;
            })
        )

        .then(literal("add")
            .then(argument("cosmetic", argument)
                .executes(c -> {
                  User user = provider.get(c);
                  CosmeticData data = user.getCosmeticData();
                  Holder<T> effect = c.getArgument("cosmetic", Holder.class);

                  data.add(effect.getValue());

                  c.getSource().sendSuccess(
                      format("Added {0} to {1, user}'s available {2} cosmetics",
                          effect.getValue(), user, type.getDisplayName()
                      )
                  );
                  return 0;
                })
            )
        )

        .then(literal("remove")
            .then(argument("cosmetic", argument)
                .executes(c -> {
                  User user = provider.get(c);
                  CosmeticData data = user.getCosmeticData();
                  Holder<T> effect = c.getArgument("cosmetic", Holder.class);

                  data.remove(effect.getValue());

                  c.getSource().sendSuccess(
                      format("Removed {0} from {1, user}'s available {2} cosmetics",
                          effect.getValue(), user, type.getDisplayName()
                      )
                  );
                  return 0;

                })
            )
        );
  }

  private static <T extends Cosmetic> ArgumentType<Holder<T>> of(CosmeticType<T> type) {
    return new RegistryArguments<>(type.getEffects(), Text.plain(type.getDisplayName()));
  }
}