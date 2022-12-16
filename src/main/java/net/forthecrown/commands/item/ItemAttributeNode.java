package net.forthecrown.commands.item;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.Messages;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.grenadier.types.RegistryArgument;
import net.forthecrown.grenadier.types.UUIDArgument;
import net.forthecrown.grenadier.types.args.ArgsArgument;
import net.forthecrown.grenadier.types.args.Argument;
import net.forthecrown.grenadier.types.args.ParsedArgs;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;
import java.util.function.Predicate;

public class ItemAttributeNode extends ItemModifierNode {
    private static final RegistryArgument<Attribute> ARGUMENT_ATTRIBUTE =
            RegistryArgument.registry(Registry.ATTRIBUTE, "Attribute");

    private static final EnumArgument<EquipmentSlot> SLOT_ARGUMENT = EnumArgument.of(EquipmentSlot.class);

    private static final Argument<Attribute> ATTR_ARG = Argument.builder("attribute", ARGUMENT_ATTRIBUTE)
            .setAliases("attr")
            .build();

    private static final Argument<UUID> UUID_ARG = Argument.builder("uuid", UUIDArgument.uuid())
            .setAliases("id")
            .build();

    private static final Argument<String> NAME_ARG = Argument.builder("name", StringArgumentType.string())
            .build();

    private static final Argument<AttributeModifier.Operation>
            OPERATION_ARG = Argument.builder("operation", EnumArgument.of(AttributeModifier.Operation.class))
            .build();

    private static final Argument<Double> VALUE_ARG = Argument.builder("value", DoubleArgumentType.doubleArg())
            .setAliases("amount")
            .build();

    private static final Argument<EquipmentSlot> SLOT_ARG = Argument.builder("slot", SLOT_ARGUMENT)
            .build();

    private static final ArgsArgument ATTR_ARGS = ArgsArgument.builder()
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
    public void create(LiteralArgumentBuilder<CommandSource> command) {
        command
                .then(literal("clear")
                        .executes(c -> {
                            var held = getHeld(c.getSource());
                            var meta = held.getItemMeta();

                            var mods = meta.getAttributeModifiers();

                            if (mods == null) {
                                throw Exceptions.NO_ATTR_MODS;
                            }

                            for (var e : mods.keySet()) {
                                meta.removeAttributeModifier(e);
                            }

                            held.setItemMeta(meta);

                            c.getSource().sendAdmin(Messages.CLEARED_ATTRIBUTE_MODS);
                            return 0;
                        })
                )

                .then(literal("add")
                        .then(argument("args", ATTR_ARGS)
                                .executes(c -> {
                                    var held = getHeld(c.getSource());
                                    var args = c.getArgument("args", ParsedArgs.class);

                                    var value = args.get(VALUE_ARG);
                                    var operation = args.get(OPERATION_ARG);
                                    var attribute = args.get(ATTR_ARG);

                                    var slot = args.get(SLOT_ARG);
                                    UUID id = args.getOrDefault(UUID_ARG, UUID.randomUUID());
                                    String name = args.getOrDefault(NAME_ARG, attribute.name().toLowerCase());

                                    AttributeModifier modifier = new AttributeModifier(
                                            id, name, value, operation, slot
                                    );

                                    var meta = held.getItemMeta();
                                    meta.addAttributeModifier(attribute, modifier);

                                    held.setItemMeta(meta);

                                    c.getSource().sendAdmin(
                                            Messages.addedAttributeModifier(attribute, modifier)
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
                                .then(argument("slot", EnumArgument.of(EquipmentSlot.class))
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

    private int removeAttr(CommandContext<CommandSource> c, Predicate<ItemMeta> remover) throws CommandSyntaxException {
        var held = getHeld(c.getSource());
        var meta = held.getItemMeta();

        if (!remover.test(meta)) {
            throw Exceptions.NO_ATTR_MODS;
        }

        held.setItemMeta(meta);

        c.getSource().sendAdmin(Messages.REMOVED_ATTRIBUTE_MOD);
        return 0;
    }
}