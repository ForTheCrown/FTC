package net.forthecrown.core.commands.item;

import static net.forthecrown.core.commands.CommandNickname.CLEAR;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.Locale;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.arguments.chat.MessageSuggestions;
import net.forthecrown.command.help.Usage;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.core.CoreMessages;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.text.Text;
import net.kyori.adventure.translation.GlobalTranslator;

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
        factory.usage("<text>")
            .addInfo("Sets the name of the item you're holding")
    );

    factory.usage("-clear")
        .addInfo("Clears the name of the item you're holding");
  }

  static void namingNote(Usage usage) {
    usage.addInfo("Note:")
        .addInfo("If the <text> is a JSON component (eg: {\"text\":\"Item Name\"})")
        .addInfo("The name won't automatically become non-italic")
        .addInfo("and white, you'll be required to manually set them to")
        .addInfo("that configuration");
  }

  @Override
  public void create(LiteralArgumentBuilder<CommandSource> command) {
    command
        .then(argument("name", Arguments.CHAT)
            .suggests((context, builder) -> {
              return MessageSuggestions.get(context, builder, true, (builder1, source) -> {
                Completions.suggest(builder1, CLEAR);

                // Suggest existing item name
                getItemSuggestions(source, itemStack -> {
                  var name = Text.itemDisplayName(itemStack);

                  String legacy = Text.LEGACY.serialize(
                      GlobalTranslator.render(name, Locale.ENGLISH)
                  );

                  Completions.suggest(builder1, legacy);
                });
              });
            })

            .executes(c -> {
              var held = getHeld(c.getSource());
              var meta = held.getItemMeta();
              var name = Arguments.getMessage(c, "name").asComponent();

              if (Text.isDashClear(name)) {
                meta.displayName(null);
                c.getSource().sendSuccess(CoreMessages.CLEARED_ITEM_NAME);
              } else {
                meta.displayName(optionallyWrap(name, c, "name"));
                c.getSource().sendSuccess(CoreMessages.setItemName(name));
              }

              held.setItemMeta(meta);
              return 0;
            })
        );
  }
}