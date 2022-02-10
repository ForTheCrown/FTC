package net.forthecrown.commands.admin;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.inventory.ItemStacks;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CommandItemStacks extends FtcCommand {

    public CommandItemStacks() {
        super("ItemStacks");

        setAliases("items", "itemutil", "itemutils");
        setPermission(Permissions.ADMIN);

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /ItemStacks
     *
     * Permissions used:
     *
     * Main Author:
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(literal("tags")
                        .then(literal("remove")
                                .then(argument("path", StringArgumentType.word())
                                        .executes(c -> {
                                            ItemStack item = item(c);
                                            ItemMeta meta = item.getItemMeta();

                                            CompoundTag tags = ItemStacks.getTags(meta);
                                            String tag = c.getArgument("path", String.class);

                                            tags.remove(tag);

                                            ItemStacks.setTags(meta, tags);
                                            item.setItemMeta(meta);

                                            c.getSource().sendAdmin("Removed tag from item");
                                            return 0;
                                        })
                                )
                        )

                        .then(literal("merge")
                                .then(argument("nbt", NbtTagArgument.nbtTag())
                                        .executes(c -> {
                                            Tag parsed = NbtTagArgument.getNbtTag(c, "nbt");

                                            if(parsed.getId() != Tag.TAG_COMPOUND) {
                                                throw FtcExceptionProvider.create("Tag was not Compound");
                                            }

                                            CompoundTag tag = (CompoundTag) parsed;
                                            ItemStack item = item(c);

                                            ItemMeta meta = item.getItemMeta();

                                            CompoundTag tags = ItemStacks.getTags(meta);
                                            tags.merge(tag);

                                            ItemStacks.setTags(meta, tags);

                                            item.setItemMeta(meta);

                                            c.getSource().sendAdmin("Merged tags");
                                            return 0;
                                        })
                                )
                        )
                );
    }

    ItemStack item(CommandContext<CommandSource> c) throws CommandSyntaxException {
        Player player = c.getSource().asPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if(ItemStacks.isEmpty(item)) {
            throw FtcExceptionProvider.mustHoldItem();
        }

        return item;
    }
}