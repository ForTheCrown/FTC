package net.forthecrown.core.commands.item;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.command.DataCommands;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.text.Messages;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

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
    DataCommands.addUsages(factory, "Item", usage -> {
      var label = usage.getArguments();

      if (!label.contains("<tag") && !label.contains("<path")) {
        return;
      }

      usage.addInfo("Note: the <tag> and <path> roots are the item's raw NBT")
          .addInfo("Not the 'tag' element.");
    });
  }

  @Override
  public void create(LiteralArgumentBuilder<CommandSource> command) {
    DataCommands.addArguments(
        command,
        "Item",
        DataCommands.HELD_ITEM_ACCESSOR
    );

    command
        .then(literal("give_command")
            .executes(c -> {
              var held = getHeld(c.getSource());

              String nbt = ItemStacks.save(held).getCompound("tag").toString();
              String cmd = "/give @s " + held.getType().getKey() + nbt;

              c.getSource().sendMessage(
                  Component.text("[Click to copy /give command]", NamedTextColor.AQUA)
                      .clickEvent(ClickEvent.copyToClipboard(cmd))
                      .hoverEvent(Messages.CLICK_ME)
              );
              return 0;
            })
        );

  }
}