package net.forthecrown.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.forthecrown.commands.arguments.RegistryArguments;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcCommands;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.economy.shops.template.ShopTemplate;
import net.forthecrown.economy.shops.template.ShopTemplateType;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.registry.Registries;
import net.forthecrown.useables.InteractionUtils;
import org.bukkit.NamespacedKey;

public class CommandShopTemplate extends FtcCommand {

    public CommandShopTemplate() {
        super("shoptemplate");

        setPermission(Permissions.FTC_ADMIN);
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     *
     * Permissions used:
     *
     * Main Author:
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(literal("create")
                        .then(argument("temp", RegistryArguments.shopTemplateType())
                                .then(argument("key", FtcCommands.ftcKeyType())
                                        .then(argument("input", StringArgumentType.greedyString())
                                                .suggests((c, b) -> {
                                                    ShopTemplateType type = c.getArgument("temp", ShopTemplateType.class);

                                                    return type.getSuggestions(c, b);
                                                })

                                                .executes(c -> {
                                                    ShopTemplateType type = (ShopTemplateType) c.getArgument("temp", ShopTemplateType.class);
                                                    NamespacedKey key = (NamespacedKey) c.getArgument("key", NamespacedKey.class);

                                                    String input = (String) c.getArgument("input", String.class);
                                                    StringReader inputReader = new StringReader(input);

                                                    ShopTemplate template = type.parse(key, inputReader, (CommandSource) c.getSource());

                                                    InteractionUtils.ensureReaderEnd(inputReader);
                                                    Registries.SHOP_TEMPLATES.register(template.key(), template);

                                                    //c.getSource().sendAdmin("Created template: " + key.asString());
                                                    return 0;
                                                })
                                        )
                                )
                        )
                )

                .then(literal("edit")
                        .then(argument("template", RegistryArguments.shopTemplate())
                                .then(argument("input", StringArgumentType.greedyString())
                                        .suggests((c, b) -> {
                                            ShopTemplate template = c.getArgument("template", ShopTemplate.class);

                                            return template.getType().getEditSuggestions(c, b);
                                        })

                                        .executes(c -> {
                                            ShopTemplate template = c.getArgument("template", ShopTemplate.class);
                                            ShopTemplateType type = template.getType();

                                            String input = c.getArgument("input", String.class);
                                            StringReader inputReader = new StringReader(input);

                                            type.edit(template, inputReader, c.getSource());

                                            InteractionUtils.ensureReaderEnd(inputReader);

                                            c.getSource().sendAdmin("Edited template: " + template.key());
                                            return 0;
                                        })
                                )
                        )
                )

                .then(literal("remove")
                        .then(argument("template", RegistryArguments.shopTemplate())
                                .executes(c -> {
                                    ShopTemplate template = c.getArgument("template", ShopTemplate.class);
                                    Registries.SHOP_TEMPLATES.remove(template.key());

                                    Crown.getShopManager().getTemplates().onTemplateDelete(template);

                                    c.getSource().sendAdmin("Removed template: " + template.key());
                                    return 0;
                                })
                        )
                );
    }
}