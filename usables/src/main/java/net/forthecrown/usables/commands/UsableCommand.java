package net.forthecrown.usables.commands;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import java.util.function.Predicate;
import lombok.Getter;
import net.forthecrown.command.DataCommands;
import net.forthecrown.command.DataCommands.DataAccessor;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.usables.UPermissions;
import net.forthecrown.usables.objects.UsableObject;
import net.forthecrown.user.User;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

public abstract class UsableCommand<H extends UsableObject> extends FtcCommand {

  @Getter
  private final String argumentName;

  protected boolean generateUseArgument = true;

  public UsableCommand(String name, String argumentName) {
    super(name);
    this.argumentName = argumentName;
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    create(command);
  }

  @Override
  public final void populateUsages(UsageFactory factory) {
    createPrefixedUsages(factory);

    ArgumentType type = getArgumentType();
    UsageFactory prefixed;

    if (type == null) {
      prefixed = factory;
    } else {
      prefixed = factory.withPrefix(usagePrefix());
    }

    createUsages(prefixed);
  }

  protected String usagePrefix() {
    return "<value>";
  }

  protected void createUsages(UsageFactory factory) {
    factory.usage("info", "Shows general info");

    if (generateUseArgument) {
      factory.usage("use", "Makes you use a " + argumentName);
      factory.usage("use <players>", "Makes a list of players use a " + argumentName);
    }

    DataCommands.addUsages(factory.withPrefix("data"), "Usable", null);
  }

  protected void createPrefixedUsages(UsageFactory factory) {

  }

  public Permission getAdminPermission() {
    return UPermissions.USABLES;
  }

  public Predicate<CommandSource> hasAdminPermission() {
    return source -> source.hasPermission(getAdminPermission());
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public void create(LiteralArgumentBuilder<CommandSource> builder) {
    addPrefixedArguments(builder);

    var argumentType = getArgumentType();
    var argName = "usable";
    var provider = getProvider(argName);

    ArgumentBuilder<CommandSource, ?> edit;

    if (argumentType == null) {
      edit = builder;
    } else {
      edit = argument(argName, argumentType);
    }

    createEditArguments((ArgumentBuilder) edit, provider);

    if (builder != edit) {
      builder.then(edit);
    }
  }

  protected void addPrefixedArguments(LiteralArgumentBuilder<CommandSource> builder) {

  }

  protected <B extends ArgumentBuilder<CommandSource, B>> void createEditArguments(
      B argument,
      UsableProvider<H> provider
  ) {
    argument.then(literal("info")
        .requires(hasAdminPermission())
        .executes(c -> {
          H holder = provider.get(c);
          c.getSource().sendMessage(holder.displayInfo());
          return 0;
        })
    );

    if (generateUseArgument) {
      argument.then(literal("use")
          .requires(hasAdminPermission())
          .executes(c -> {
            H holder = provider.get(c);
            Player player = c.getSource().asPlayer();

            holder.interact(player);
            return 0;
          })

          .then(argument("player", Arguments.ONLINE_USERS)
              .executes(c -> {
                H holder = provider.get(c);
                List<User> userList = Arguments.getUsers(c, "player");

                for (User user : userList) {
                  var player = user.getPlayer();
                  holder.interact(player);
                }

                return 0;
              })
          )
      );
    }

    argument.then(
        DataCommands.dataAccess("Usable", new UsableDataAccessor<>(provider))
            .requires(hasAdminPermission())
    );
  }

  protected abstract ArgumentType<?> getArgumentType();

  protected abstract UsableProvider<H> getProvider(String argument);

  record UsableDataAccessor<H extends UsableObject>(UsableProvider<H> provider)
      implements DataAccessor
  {

    @Override
    public CompoundTag getTag(CommandContext<CommandSource> context)
        throws CommandSyntaxException
    {
      H holder = provider.get(context);
      var tag = BinaryTags.compoundTag();
      holder.save(tag);
      return tag;
    }

    @Override
    public void setTag(CommandContext<CommandSource> context, CompoundTag tag)
        throws CommandSyntaxException
    {
      H holder = provider.get(context);
      holder.load(tag);
      provider.postEdit(holder);
    }
  }
}
