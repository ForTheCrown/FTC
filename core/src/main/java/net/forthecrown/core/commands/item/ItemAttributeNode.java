package net.forthecrown.core.commands.item;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.UUID;
import java.util.function.Predicate;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.core.CoreExceptions;
import net.forthecrown.core.CoreMessages;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.grenadier.types.RegistryArgument;
import net.forthecrown.grenadier.types.options.ArgumentOption;
import net.forthecrown.grenadier.types.options.Options;
import net.forthecrown.grenadier.types.options.OptionsArgument;
import net.forthecrown.grenadier.types.options.ParsedOptions;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemAttributeNode extends ItemModifierNode {

  private static final RegistryArgument<Attribute> ARGUMENT_ATTRIBUTE =
      ArgumentTypes.registry(Registry.ATTRIBUTE, "Attribute");

  private static final EnumArgument<EquipmentSlot> SLOT_ARGUMENT
      = ArgumentTypes.enumType(EquipmentSlot.class);

  private static final ArgumentOption<Attribute> ATTR_ARG
      = Options.argument(ARGUMENT_ATTRIBUTE)
      .setLabel("attribute")
      .build();

  private static final ArgumentOption<UUID> UUID_ARG
      = Options.argument(ArgumentTypes.uuid())
      .setLabel("uuid")
      .build();

  private static final ArgumentOption<String> NAME_ARG
      = Options.argument(StringArgumentType.string())
      .setLabel("name")
      .build();

  private static final ArgumentOption<AttributeModifier.Operation> OPERATION_ARG
      = Options.argument(ArgumentTypes.enumType(AttributeModifier.Operation.class))
      .setLabel("operation")
      .build();

  private static final ArgumentOption<Double> VALUE_ARG
      = Options.argument(DoubleArgumentType.doubleArg())
      .setLabel("value")
      .build();

  private static final ArgumentOption<EquipmentSlot> SLOT_ARG
      = Options.argument(SLOT_ARGUMENT)
      .setLabel("slot")
      .build();

  private static final OptionsArgument ATTR_ARGS = OptionsArgument.builder()
      .addRequired(ATTR_ARG)
      .addRequired(OPERATION_ARG)
      .addRequired(VALUE_ARG)
      .addOptional(SLOT_ARG)
      .addOptional(UUID_ARG)
      .addOptional(NAME_ARG)
      .build();

  public ItemAttributeNode() {
    super("item_attribute_modifiers");
  }

  @Override
  String getArgumentName() {
    return "attributes";
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("clear")
        .addInfo("Clears the item's Attribute Modifiers");

    factory.usage("remove <attribute>")
        .addInfo("Removes all modifiers which modify the")
        .addInfo("<attribute> value");

    factory.usage("remove attr <attribute>")
        .addInfo("Same as above");

    factory.usage("remove slot <equipment slot>")
        .addInfo("Removes all modifiers which apply to the")
        .addInfo("<equipment slot>");
  }

  @Override
  public void create(LiteralArgumentBuilder<CommandSource> command) {
    command
        .then(literal("clear")
            .executes(c -> {
              var held = getHeld(c.getSource());
              var meta = held.getItemMeta();

              var mods = meta.getAttributeModifiers();

              if (mods == null) {
                throw CoreExceptions.NO_ATTR_MODS;
              }

              for (var e : mods.keySet()) {
                meta.removeAttributeModifier(e);
              }

              held.setItemMeta(meta);

              c.getSource().sendSuccess(CoreMessages.CLEARED_ATTRIBUTE_MODS);
              return 0;
            })
        )

        .then(literal("add")
            .then(argument("args", ATTR_ARGS)
                .executes(c -> {
                  var held = getHeld(c.getSource());
                  var args = c.getArgument("args", ParsedOptions.class);

                  var value = args.getValue(VALUE_ARG);
                  var operation = args.getValue(OPERATION_ARG);
                  var attribute = args.getValue(ATTR_ARG);

                  var slot = args.getValue(SLOT_ARG);

                  UUID id;
                  if (args.has(UUID_ARG)) {
                    id = args.getValue(UUID_ARG);
                  } else {
                    id = UUID.randomUUID();
                  }

                  String name;
                  if (args.has(NAME_ARG)) {
                    name = args.getValue(NAME_ARG);
                  } else {
                    name = attribute.name().toLowerCase();
                  }

                  AttributeModifier modifier = new AttributeModifier(
                      id, name, value, operation, slot
                  );

                  var meta = held.getItemMeta();
                  meta.addAttributeModifier(attribute, modifier);

                  held.setItemMeta(meta);

                  c.getSource().sendSuccess(
                      CoreMessages.addedAttributeModifier(attribute, modifier)
                  );
                  return 0;
                })
            )
        )

        .then(literal("remove")
            .then(removeAttributeArg())

            .then(literal("attr")
                .then(removeAttributeArg())
            )

            .then(literal("slot")
                .then(argument("slot", ArgumentTypes.enumType(EquipmentSlot.class))
                    .executes(c -> {
                      var slot = c.getArgument("slot", EquipmentSlot.class);
                      return removeAttr(c, meta -> meta.removeAttributeModifier(slot));
                    })
                )
            )
        );
  }

  private RequiredArgumentBuilder<CommandSource, ?> removeAttributeArg() {
    return argument("attribute", ARGUMENT_ATTRIBUTE)
        .executes(c -> {
          var attr = c.getArgument("attribute", Attribute.class);
          return removeAttr(c, meta -> meta.removeAttributeModifier(attr));
        });
  }

  private int removeAttr(CommandContext<CommandSource> c, Predicate<ItemMeta> remover)
      throws CommandSyntaxException {
    var held = getHeld(c.getSource());
    var meta = held.getItemMeta();

    if (!remover.test(meta)) {
      throw CoreExceptions.NO_ATTR_MODS;
    }

    held.setItemMeta(meta);

    c.getSource().sendSuccess(CoreMessages.REMOVED_ATTRIBUTE_MOD);
    return 0;
  }
}