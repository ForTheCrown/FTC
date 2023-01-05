package net.forthecrown.commands.item;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.Messages;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;

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
    factory.usage("view")
        .addInfo("Displays the item's data");

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
        .then(literal("view")
            .executes(c -> {
              var held = getHeld(c.getSource());

              var tag = ItemStacks.save(held);
              var text = Text.displayTag(tag, true);

              c.getSource().sendMessage(
                  Component.text("Item data: ")
                      .append(text)
              );
              return 0;
            })
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
}