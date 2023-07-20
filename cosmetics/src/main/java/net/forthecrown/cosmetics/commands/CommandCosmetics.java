package net.forthecrown.cosmetics.commands;

import static net.forthecrown.text.Text.format;
import static net.kyori.adventure.text.Component.text;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Optional;
import java.util.function.Predicate;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.arguments.RegistryArguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.cosmetics.Cosmetic;
import net.forthecrown.cosmetics.CosmeticData;
import net.forthecrown.cosmetics.CosmeticPermissions;
import net.forthecrown.cosmetics.CosmeticType;
import net.forthecrown.cosmetics.Cosmetics;
import net.forthecrown.cosmetics.CosmeticsPlugin;
import net.forthecrown.cosmetics.LoginEffects;
import net.forthecrown.cosmetics.menu.CosmeticMenus;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.registry.Holder;
import net.forthecrown.registry.Registry;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextJoiner;
import net.forthecrown.text.TextWriters;
import net.forthecrown.user.User;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandCosmetics extends FtcCommand {

  private static final Predicate<CommandSource> IS_ADMIN
      = source -> source.hasPermission(CosmeticPermissions.ADMIN);

  public CommandCosmetics() {
    super("cosmetics");

    setDescription("Opens the cosmetics menu");
    setPermission(CosmeticPermissions.DEFAULT);
    simpleUsages();

    register();
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory = factory.withPermission(CosmeticPermissions.ADMIN);

    factory.usage("reload").addInfo("Reloads the cosmetics config");

    categoryUses("death", factory);
    categoryUses("arrow", factory);
    categoryUses("travel", factory);
    categoryUses("login", factory);
  }

  private void categoryUses(String name, UsageFactory factory) {
    var prefixed = factory.withPrefix(name);

    prefixed.usage("")
        .addInfo("Shows a user's active and available %s", name);

    prefixed.usage("set <cosmetic>")
        .addInfo("Sets the user's active %s", name);

    prefixed.usage("unset")
        .addInfo("Clears the user's active %s cosmetic", name);

    prefixed.usage("clear")
        .addInfo("Clears the user's available")
        .addInfo("%s cosmetics", name);

    prefixed.usage("add <cosmetic>")
        .addInfo("Adds the <cosmetic> to the user's")
        .addInfo("cosmetic effect list");

    prefixed.usage("remove <cosmetic>")
        .addInfo("Removes the <cosmetic> from the user's")
        .addInfo("available cosmetics list");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .executes(c -> {
          User u = getUserSender(c);
          CosmeticMenus.open(u);
          return 0;
        })

        .then(literal("reload")
            .requires(IS_ADMIN)

            .executes(c -> {
              JavaPlugin.getPlugin(CosmeticsPlugin.class).reloadConfig();
              CosmeticMenus.createMenus();

              c.getSource().sendSuccess(text("Reloaded cosmetics config"));
              return 0;
            })
        )

        .then(typeArguments(Cosmetics.ARROW_EFFECTS))
        .then(typeArguments(Cosmetics.TRAVEL_EFFECTS))
        .then(typeArguments(Cosmetics.DEATH_EFFECTS))
        .then(typeArguments(LoginEffects.TYPE));
  }
  
  private User getUser(CommandContext<CommandSource> context) throws CommandSyntaxException {
    return Arguments.getUser(context, "user");
  }
  
  private <T> LiteralArgumentBuilder<CommandSource> typeArguments(CosmeticType<T> type) {
    var argument = argumentType(type);

    return literal(type.getName().replaceAll("_effects", ""))
        .requires(IS_ADMIN)

        .executes(c -> {
          User user = getUser(c);
          CosmeticData data = user.getComponent(CosmeticData.class);

          var available = data.getAvailable(type);
          var active = data.get(type);

          if (active == null && available.isEmpty()) {
            throw Exceptions.NOTHING_TO_LIST;
          }

          String key;
          Registry<Cosmetic<T>> effects = type.getCosmetics();

          if (active == null) {
            key = "UNSET";
          } else {
            key = effects.getKey(active).orElseThrow();
          }

          var writer = TextWriters.newWriter();
          writer.formatted("{0, user}'s {1} effects:", user, type.getDisplayName());
          writer.field("Active", key);

          ComponentLike availableValue;

          if (available.isEmpty()) {
            availableValue = text("NONE");
          } else {
            availableValue = TextJoiner.onComma()
                .add(
                    available.stream()
                        .map(effects::getKey)
                        .filter(Optional::isPresent)
                        .map(s -> text(s.get()))
                );
          }

          writer.field("Available", availableValue);

          c.getSource().sendMessage(writer);
          return 0;
        })

        .then(literal("set")
            .then(argument("cosmetic", argument)
                .executes(c -> {
                  User user = getUser(c);
                  CosmeticData data = user.getComponent(CosmeticData.class);
                  Holder<Cosmetic<T>> effect = c.getArgument("cosmetic", Holder.class);

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
              User user = getUser(c);
              CosmeticData data = user.getComponent(CosmeticData.class);

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
              User user = getUser(c);
              CosmeticData data = user.getComponent(CosmeticData.class);

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
                  User user = getUser(c);
                  CosmeticData data = user.getComponent(CosmeticData.class);
                  Holder<Cosmetic<T>> effect = c.getArgument("cosmetic", Holder.class);

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
                  User user = getUser(c);
                  CosmeticData data = user.getComponent(CosmeticData.class);
                  Holder<Cosmetic<T>> effect = c.getArgument("cosmetic", Holder.class);

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

  private static <T> ArgumentType<Holder<Cosmetic<T>>> argumentType(CosmeticType<T> type) {
    return new RegistryArguments<>(type.getCosmetics(), Text.plain(type.getDisplayName()));
  }
}