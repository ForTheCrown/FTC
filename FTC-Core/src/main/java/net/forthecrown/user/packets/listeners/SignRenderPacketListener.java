package net.forthecrown.user.packets.listeners;

import net.forthecrown.economy.shops.ShopManager;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.packets.PacketContext;
import net.forthecrown.user.packets.PacketListener;
import net.forthecrown.utils.math.WorldVec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntityType;

import javax.annotation.Nullable;

public class SignRenderPacketListener implements PacketListener<ClientboundBlockEntityDataPacket> {
    @Override
    public void onPacketSend(PacketContext<ClientboundBlockEntityDataPacket> context) {
        ClientboundBlockEntityDataPacket packet = context.packet;

        if(packet.getType() != BlockEntityType.SIGN) return;
        CompoundTag tag = packet.getTag();

        if(!isShopTag(tag)) return;
        transformTag(context.target, tag);
    }

    static void transformTag(CrownUser user, CompoundTag tag) {
        WorldVec3i pos2 = new WorldVec3i(user.getWorld(),
                tag.getInt("x"),
                tag.getInt("y"),
                tag.getInt("z")
        );

        /*SignShop shop = Crown.getShopManager().getShop(pos2);
        if(shop == null) return;

        Component priceLine = shop.getPriceLineFor(user);
        String priceLineJson = ChatUtils.GSON.serialize(priceLine);

        tag.putString("Text4", priceLineJson);
        if(tag.contains("FilteredText4")) tag.putString("FilteredText4", priceLineJson);*/
    }

    static boolean isShopTag(@Nullable CompoundTag tag) {
        if(tag == null || !tag.contains("PublicBukkitValues") || !tag.contains("GlowingText")) return false;
        CompoundTag publicBukkit = tag.getCompound("PublicBukkitValues");

        return publicBukkit.contains(ShopManager.SHOP_KEY.asString());
    }
}
