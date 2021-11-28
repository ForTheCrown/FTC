package net.forthecrown.user.packets.listeners;

import net.forthecrown.economy.shops.ShopManager;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.packets.PacketContext;
import net.forthecrown.user.packets.PacketListener;
import net.forthecrown.utils.math.WorldVec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;

public class SignRenderPacketListener implements PacketListener<ClientboundBlockEntityDataPacket> {
    @Override
    public void onPacketSend(PacketContext<ClientboundBlockEntityDataPacket> context) {
        ClientboundBlockEntityDataPacket packet = context.packet;

        if(packet.getType() != 9) return;
        CompoundTag tag = packet.getTag();

        if(!isShopTag(tag)) return;
        checkCompoundTag(context.target, tag);
    }

    static void checkCompoundTag(CrownUser user, CompoundTag tag) {
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

    static boolean isShopTag(CompoundTag tag) {
        if(!tag.contains("PublicBukkitValues") || !tag.contains("GlowingText")) return false;
        CompoundTag publicBukkit = tag.getCompound("PublicBukkitValues");

        return publicBukkit.contains(ShopManager.SHOP_KEY.asString());
    }
}
