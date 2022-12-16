package net.forthecrown.guilds.menu;

import net.forthecrown.core.FTC;
import net.forthecrown.events.dynamic.GuildSignPacketListener;
import net.forthecrown.guilds.GuildMessage;
import net.forthecrown.user.User;
import net.forthecrown.user.packet.PacketListeners;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.VanillaAccess;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.MenuBuilder;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.inventory.menu.Menus;
import net.forthecrown.utils.inventory.menu.context.InventoryContext;
import net.forthecrown.utils.inventory.menu.page.MenuPage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.forthecrown.guilds.menu.GuildMenus.GUILD;

public class MessageCreationMenu extends MenuPage {
    public MessageCreationMenu(MenuPage parent) {
        super(parent);
        initMenu(Menus.builder(Menus.MIN_INV_SIZE, "Pick a sign type"), true);
    }

    @Override
    protected void addBorder(MenuBuilder builder) {
    }

    @Override
    protected void createMenu(MenuBuilder builder) {
        int i = -1;
        for (var type: GuildMessage.SIGN_TYPES) {
            i++;

            builder.add(i,
                    MenuNode.builder()
                            .setItem((user, context) -> new ItemStack(type))

                            .setRunnable((user, context, click) -> {
                                var guild = context.getOrThrow(GUILD);
                                Location loc = user.getLocation();
                                loc.setY(Util.MAX_Y);

                                BlockPos pos = new BlockPos(
                                        loc.getBlockX(),
                                        loc.getBlockY(),
                                        loc.getBlockZ()
                                );

                                try {
                                    user.getPlayer().sendBlockChange(
                                            loc,
                                            type.createBlockData()
                                    );

                                    ClientboundOpenSignEditorPacket
                                            packet = new ClientboundOpenSignEditorPacket(pos);

                                    VanillaAccess.getPacketListener(user.getPlayer())
                                            .send(packet);

                                    PacketListeners.register(
                                            new GuildSignPacketListener(
                                                    user, guild, type
                                            )
                                    );
                                } catch (Throwable t) {
                                    FTC.getLogger().error("Couldn't send sign packet", t);
                                }
                            })

                            .build()
            );
        }
    }

    @Override
    public @Nullable ItemStack createItem(@NotNull User user, @NotNull InventoryContext context) {
        return ItemStacks.builder(Material.WRITABLE_BOOK)
                .setName("&eWrite a new message")
                .addLore("&7Click to write a new message")
                .build();
    }
}