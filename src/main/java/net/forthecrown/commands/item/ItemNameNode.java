package net.forthecrown.commands.item;

import static net.forthecrown.commands.CommandNickname.CLEAR;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.core.Messages;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;

public class ItemNameNode extends ItemModifierNode {

  public ItemNameNode() {
    super("itemname", "nameitem", "renameitem", "itemrename");
  }

  @Override
  String getArgumentName() {
    return "name";
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    namingNote(
        factory.usage("<name>")
            .addInfo("Sets the name of the item you're holding")
    );

    factory.usage("-clear")
        .addInfo("Clears the name of the item you're holding");
  }

  static void namingNote(Usage usage) {
    usage.addInfo("Note:")
        .addInfo("If the <name> is a JSON component (eg: {\"text\":\"Item Name\"})")
        .addInfo("The name won't automatically become non-italic")
        .addInfo("and white, you'll be required to manually set them to")
        .addInfo("that configuration");
  }

  @Override
  public void create(LiteralArgumentBuilder<CommandSource> command) {
    command
        .then(argument("name", Arguments.CHAT)
            .suggests((context, builder) -> {
              var token = builder.getRemainingLowerCase();

              if (token.isBlank() || CLEAR.startsWith(token)) {
                builder.suggest(CLEAR);
                return builder.buildFuture();
              }

              return Arguments.CHAT.listSuggestions(context, builder);
            })

            .executes(c -> {
              var held = getHeld(c.getSource());
              var meta = held.getItemMeta();
              var name = c.getArgument("name", Component.class);

              if (Text.isDashClear(name)) {
                meta.displayName(null);
                c.getSource().sendAdmin(Messages.CLEARED_ITEM_NAME);
              } else {
                meta.displayName(optionallyWrap(name, c, "name"));
                c.getSource().sendAdmin(Messages.setItemName(name));
              }

              held.setItemMeta(meta);
              return 0;
            })
        );
  }
}