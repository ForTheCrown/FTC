package net.forthecrown.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import java.util.function.Consumer;
import net.forthecrown.commands.manager.Commands;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand.Usage;
import net.forthecrown.commands.manager.FtcCommand.UsageFactory;
import net.forthecrown.grenadier.CmdUtil;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.TextJoiner;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.NbtPathArgument.NbtPath;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class DataCommands extends CmdUtil {
  public static final DataAccessor HELD_ITEM_ACCESSOR = new DataAccessor() {
    @Override
    public CompoundTag getTag(CommandContext<CommandSource> context)
        throws CommandSyntaxException
    {
      var player = context.getSource().asPlayer();
      ItemStack item = Commands.getHeldItem(player);
      return ItemStacks.save(item);
    }

    @Override
    public void setTag(CommandContext<CommandSource> context, CompoundTag tag)
        throws CommandSyntaxException
    {
      var player = context.getSource().asPlayer();
      var loaded = ItemStacks.load(tag);
      player.getInventory().setItemInMainHand(loaded);
    }
  };

  public static DataAccessor offsetAccessor(DataAccessor accessor, String path)
      throws RuntimeException
  {
    NbtPath tagPath;

    try {
      tagPath = NbtPathArgument.nbtPath().parse(new StringReader(path));
    } catch (CommandSyntaxException exc) {
      throw new RuntimeException(exc);
    }

    return new DataAccessor() {
      @Override
      public CompoundTag getTag(CommandContext<CommandSource> context)
          throws CommandSyntaxException
      {
        CompoundTag tag = accessor.getTag(context);
        List<Tag> tags = tagPath.get(tag);

        if (tags.size() > 1) {
          throw Exceptions.format(
              "Too many elements at path '{0}' (more than 1)",
              path
          );
        }

        Tag t = tags.get(0);

        if (!(t instanceof CompoundTag compound)) {
          throw Exceptions.format("Element at {0} is not a CompoundTag", path);
        }

        return compound;
      }

      @Override
      public void setTag(CommandContext<CommandSource> context, CompoundTag tag)
          throws CommandSyntaxException
      {
        CompoundTag original = accessor.getTag(context);
        tagPath.set(original, tag);
      }
    };
  }

  public static void addUsages(UsageFactory factory,
                               String name,
                               @Nullable Consumer<Usage> usageConsumer
  ) {
    var implicitView = factory.usage("")
        .addInfo("Displays the %s data", name);

    var view = factory.usage("view [<path: nbt path>]")
        .addInfo("Displays %s data, if [path] is set, shows only", name)
        .addInfo("data at that path");

    var insert = factory.usage("insert <path: nbt path> <tag>")
        .addInfo("Inserts a <tag> into %s data at a <path>", name);

    var merge = factory.usage("merge <tag>")
        .addInfo("Merges a <tag> into %s data", name);

    var set = factory.usage("set <tag>")
        .addInfo("Completely overwrites the existing %s data", name)
        .addInfo("and sets it to <tag>");

    if (usageConsumer != null) {
      usageConsumer.accept(implicitView);
      usageConsumer.accept(view);
      usageConsumer.accept(insert);
      usageConsumer.accept(merge);
      usageConsumer.accept(set);
    }
  }

  public static LiteralArgumentBuilder<CommandSource> dataAccess(
      String name,
      DataAccessor accessor
  ) {
    var literal = literal("data");
    addArguments(literal, name, accessor);
    return literal;
  }

  public static void addArguments(
      LiteralArgumentBuilder<CommandSource> builder,
      String name,
      DataAccessor accessor
  ) {
    builder
        .executes(c -> viewData(c, accessor, name, null))

        .then(literal("view")
            .executes(c -> viewData(c, accessor, name, null))

            .then(argument("path", NbtPathArgument.nbtPath())
                .executes(c -> {
                  NbtPath path = c.getArgument("path", NbtPath.class);
                  return viewData(c, accessor, name, path);
                })
            )
        )

        .then(literal("set")
            .then(argument("nbt_tag", CompoundTagArgument.compoundTag())
                .executes(c -> {
                  CompoundTag tag = c.getArgument("nbt_tag", CompoundTag.class);
                  accessor.setTag(c, tag);

                  c.getSource().sendAdmin(
                      Text.format("Set {0} data",
                          NamedTextColor.GRAY,
                          name
                      )
                  );
                  return 0;
                })
            )
        )

        .then(literal("insert")
            .then(argument("path", NbtPathArgument.nbtPath())
                .then(argument("nbt", NbtTagArgument.nbtTag())
                    .executes(c -> {
                      CompoundTag tag = accessor.getTag(c);
                      NbtPath path = c.getArgument("path", NbtPath.class);
                      Tag insertTag = c.getArgument("nbt", Tag.class);

                      int changed = path.set(tag, insertTag);

                      if (changed < 1) {
                        throw Exceptions.format("Nothing changed");
                      }

                      accessor.setTag(c, tag);

                      c.getSource().sendAdmin(
                          Text.format(
                              "Placed data into &e{0}&r tag at &e{1}&r",
                              NamedTextColor.GRAY,
                              name, path
                          )
                      );
                      return 0;
                    })
                )
            )
        )

        .then(literal("merge")
            .then(argument("nbt", CompoundTagArgument.compoundTag())
                .executes(c -> {
                  CompoundTag tag = accessor.getTag(c);
                  CompoundTag mergeSource
                      = c.getArgument("nbt", CompoundTag.class);

                  tag.merge(mergeSource);
                  accessor.setTag(c, tag);

                  c.getSource().sendAdmin("Merged " + name + " data");
                  return 0;
                })
            )
        )

        .then(literal("remove")
            .then(argument("path", NbtPathArgument.nbtPath())
                .executes(c -> {
                  NbtPath path = c.getArgument("path", NbtPath.class);
                  CompoundTag tag = accessor.getTag(c);

                  int removed = path.remove(tag);
                  if (removed <= 0) {
                    throw Exceptions.format(
                        "Nothing to remove at path '{0}'",
                        path
                    );
                  }

                  accessor.setTag(c, tag);

                  c.getSource().sendAdmin(
                      Text.format("Removed {0, number} tags at '{1}'",
                          removed, path
                      )
                  );
                  return 0;
                })
            )
        );
  }

  private static int viewData(CommandContext<CommandSource> context,
                              DataAccessor accessor,
                              String name,
                              @Nullable NbtPath path
  ) throws CommandSyntaxException {
    Tag tag = accessor.getTag(context);

    Component component;

    if (path != null) {
      List<Tag> tags = path.get(tag);

      if (tags.size() == 1) {
        component = Text.displayTag(tags.get(0), true);
      } else {
        component = TextJoiner.on(",\n")
            .setPrefix(Component.text("["))
            .setPrefix(Component.text("]"))
            .add(tags.stream().map(tag1 -> Text.displayTag(tag1, true)))
            .asComponent();
      }

    } else {
      component = Text.displayTag(tag, true);
    }

    context.getSource().sendMessage(
        Component.text(name + " data: ")
            .append(component)
    );
    return 0;
  }

  public interface DataAccessor {
    CompoundTag getTag(CommandContext<CommandSource> context)
        throws CommandSyntaxException;

    void setTag(CommandContext<CommandSource> context, CompoundTag tag)
        throws CommandSyntaxException;
  }
}