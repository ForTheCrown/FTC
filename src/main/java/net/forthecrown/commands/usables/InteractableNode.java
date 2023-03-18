package net.forthecrown.commands.usables;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.DataCommands;
import net.forthecrown.commands.DataCommands.DataAccessor;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.useables.Usable;
import net.forthecrown.utils.text.writer.TextWriters;
import org.jetbrains.annotations.NotNull;

abstract class InteractableNode<H extends Usable> extends FtcCommand {

  protected String argumentName;

  protected InteractableNode(@NotNull String name) {
    super(name);
    this.argumentName = name;

    setPermission(Permissions.USABLES);
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    addCreateUsage(factory.withPrefix("-create"));

    factory = prefixWithType(factory);
    addUsages(factory);
  }

  protected void addCreateUsage(UsageFactory factory) {
    prefixWithType(factory)
        .usage("")
        .addInfo("Creates a new usable");
  }

  protected void addUsages(UsageFactory factory) {
    factory.usage("remove")
        .addInfo("Removes the %s", argumentName);

    factory.usage("info")
        .addInfo("Displays admin information about the %s", argumentName);

    DataCommands.addUsages(factory.withPrefix("data"), argumentName, null);

    UsableCommands.ACTION_NODE.populateUsages(factory, argumentName);
    UsableCommands.CHECK_NODE.populateUsages(factory, argumentName);
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    create(command);
  }

  protected void create(LiteralArgumentBuilder<CommandSource> command) {
    var createLiteral = literal("-create");
    createNewUsableArguments(createLiteral);

    command
        .then(createLiteral)
        .then(createEditArguments());
  }

  protected abstract UsageFactory prefixWithType(UsageFactory factory);

  protected DataAccessor createAccessor(UsageHolderProvider<H> provider,
                                        UsableSaveCallback<H> saveCallback
  ) {
    return new DataAccessor() {
      @Override
      public CompoundTag getTag(CommandContext<CommandSource> context)
          throws CommandSyntaxException {
        var holder = provider.get(context);

        CompoundTag tag = BinaryTags.compoundTag();
        holder.save(tag);
        return tag;
      }

      @Override
      public void setTag(CommandContext<CommandSource> context, CompoundTag tag)
          throws CommandSyntaxException {
        var holder = provider.get(context);
        holder.load(tag);

        saveCallback.save(holder);
      }
    };
  }

  protected abstract UsableSaveCallback<H> saveCallback();

  protected RequiredArgumentBuilder<CommandSource, ?> createEditArguments() {
    var argument = argument("holder_value", getArgumentType());
    UsageHolderProvider<H> provider = context -> get("holder_value", context);

    var removeLiteral = literal("remove");
    createRemoveArguments(removeLiteral, provider);
    addEditArguments(argument, provider);

    var saveCallback = saveCallback();

    return argument
        .then(literal("info")
            .executes(c -> {
              var holder = provider.get(c);
              var writer = TextWriters.newWriter();

              holder.adminInfo(writer);

              c.getSource().sendMessage(writer);
              return 0;
            })
        )

        .then(
            DataCommands.dataAccess(
                "Usable",
                createAccessor(provider, saveCallback)
            )
        )

        .then(removeLiteral)

        .then(
            UsableCommands.CHECK_NODE
                .createArguments(provider, saveCallback)
        )
        .then(
            UsableCommands.ACTION_NODE
                .createArguments(provider, saveCallback)
        );
  }

  protected abstract void createNewUsableArguments(LiteralArgumentBuilder<CommandSource> command);

  protected abstract void createRemoveArguments(LiteralArgumentBuilder<CommandSource> command,
                                                UsageHolderProvider<H> provider
  );

  protected abstract ArgumentType<?> getArgumentType();

  protected abstract H get(String argumentName, CommandContext<CommandSource> context)
      throws CommandSyntaxException;

  protected void addEditArguments(RequiredArgumentBuilder<CommandSource, ?> command,
                                  UsageHolderProvider<H> provider
  ) {
  }
}