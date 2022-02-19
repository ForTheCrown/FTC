package net.forthecrown.user.packets.listeners;

import net.forthecrown.core.Crown;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.economy.shops.ShopManager;
import net.forthecrown.economy.shops.SignShop;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.packets.PacketContext;
import net.forthecrown.user.packets.PacketListener;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.math.WorldVec3i;
import net.kyori.adventure.text.Component;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntityType;

import javax.annotation.Nullable;
import java.time.Month;
import java.util.Calendar;

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

        SignShop shop = Crown.getShopManager().getShop(pos2);
        if(shop == null) return;

        Component priceLine = shop.getPriceLineFor(user);

        if(APRIL_FOOLS) {
            int price = shop.getPrice(user);
            int halfPrice = price >> 1;

            priceLine = Crown.getShopManager().getPriceLine(FtcUtils.RANDOM.intInRange(halfPrice, price + halfPrice));
        }

        String priceLineJson = ChatUtils.GSON.serialize(priceLine);

        tag.putString("Text4", priceLineJson);
        if(tag.contains("FilteredText4")) tag.putString("FilteredText4", priceLineJson);
    }

    private static final boolean APRIL_FOOLS = Util.make(() -> {
        Calendar calendar = Calendar.getInstance();
        if(calendar.get(Calendar.MONTH) != Month.APRIL.getValue()) return false;

        return calendar.get(Calendar.DATE) == 1;
    });

    static boolean isShopTag(@Nullable CompoundTag tag) {
        if(tag == null || !tag.contains("PublicBukkitValues", Tag.TAG_COMPOUND) || !tag.contains("GlowingText")) return false;
        CompoundTag publicBukkit = tag.getCompound("PublicBukkitValues");

        return publicBukkit.contains(ShopManager.SHOP_KEY.asString());
    }
}
