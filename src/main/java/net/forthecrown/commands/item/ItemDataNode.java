package net.forthecrown.commands.item;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.Messages;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.TextJoiner;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.NbtPathArgument.NbtPath;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class ItemDataNode extends ItemModifierNode {

  public ItemDataNode() {
    super(
        "item_data",
        "item_tags", "itemnbt", "itemdata", "itemtags"
    );
  }

  @Override
  String getArgumentName() {
    return "data";
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("view [<nbt path>]")
        .addInfo("Displays the item's data")
        .addInfo("If <nbt path> is set, then it'll only display the data")
        .addInfo("at that path, otherwise the entire item's NBT is shown");

    factory.usage("give_command")
        .addInfo("Creates a /give command for the item you're holding");

    factory.usage("merge <compound tag>")
        .addInfo("Merges the <compound tag> into the item's tag")
        .addInfo("Note: the <compound tag> is put into the item's raw")
        .addInfo("NBT, not into the 'tag' element in the NBT");

    factory.usage("remove <nbt path>")
        .addInfo("Removes a tag at the given NBT path.")
        .addInfo("Note: the path's root is considered the item's raw")
        .addInfo("NBT, not the 'tag' element in the data");
  }

  @Override
  public void create(LiteralArgumentBuilder<CommandSource> command) {
    command
        .then(literal("give_command")
            .executes(c -> {
              var held = getHeld(c.getSource());

              String nbt = ItemStacks.save(held).getCompound("tag").toString();
              String cmd = "/give @s " + held.getType().getKey() + nbt;

              c.getSource().sendMessage(
                  Component.text(
                      "[Click to copy /give command]", NamedTextColor.AQUA
                  ).clickEvent(ClickEvent.copyToClipboard(cmd))
              );
              return 0;
            })
        )

        .then(literal("view")
            .executes(c -> view(c, false))

            .then(argument("path", NbtPathArgument.nbtPath())
                .executes(c -> view(c, true))
            )
        )

        .then(literal("remove")
            .then(argument("path", NbtPathArgument.nbtPath())
                .executes(c -> {
                  var player = c.getSource().asPlayer();
                  var held = getHeld(c.getSource());
                  var path = c.getArgument("path", NbtPathArgument.NbtPath.class);

                  var itemTag = ItemStacks.save(held);

                  if (path.remove(itemTag) < 1) {
                    throw Exceptions.REMOVED_NO_DATA;
                  }

                  var item = ItemStacks.load(itemTag);
                  player.getInventory().setItemInMainHand(item);

                  c.getSource().sendAdmin(Messages.REMOVED_ITEM_DATA);
                  return 0;
                })
            )
        )

        .then(literal("merge")
            .then(argument("tag", CompoundTagArgument.compoundTag())
                .executes(c -> {
                  var player = c.getSource().asPlayer();
                  var held = getHeld(c.getSource());

                  var itemTag = ItemStacks.save(held);
                  var parsedTag = c.getArgument("tag", CompoundTag.class);

                  itemTag.merge(parsedTag);

                  var item = ItemStacks.load(itemTag);
                  player.getInventory().setItemInMainHand(item);

                  c.getSource().sendAdmin(Messages.MERGED_ITEM_DATA);
                  return 0;
                })
            )
        );
  }

  private int view(CommandContext<CommandSource> c, boolean pathSet)
      throws CommandSyntaxException
  {
    var held = getHeld(c.getSource());

    var tag = ItemStacks.save(held);
    List<Tag> elements;

    if (pathSet) {
      NbtPath path = c.getArgument("path", NbtPath.class);
      elements = path.get(tag);

      if (elements.isEmpty()) {
        throw Exceptions.format("No data at {0}",
            path.toString()
        );
      }
    } else {
      elements = List.of(tag);
    }

    c.getSource().sendMessage(
        Component.text("Item data: ").append(
            TextJoiner.onNewLine()
                .add(elements.stream().map(tag1 -> Text.displayTag(tag1, true)))
                .asComponent()
        )
    );
    return 0;
  }
}