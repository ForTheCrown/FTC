package net.forthecrown.economy.market;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.forthecrown.serializer.JsonBuf;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.squire.Squire;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.math.FtcBoundingBox;
import net.forthecrown.utils.math.Vector3i;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.persistence.PersistentDataType;

public class ShopEntrance implements JsonSerializable {
    public static final NamespacedKey NOTICE_KEY = Squire.createFtcKey("market_notice");

    public final BlockFace direction;
    public final Vector3i notice;
    public final Vector3i doorSign;

    public ShopEntrance(BlockFace direction, Vector3i notice, Vector3i doorSign) {
        this.direction = direction;
        this.notice = notice;
        this.doorSign = doorSign;
    }

    public void onClaim(CrownUser user, FtcMarketShop shop) {
        //Above door sign
        /*boolean endsInS = user.getNickOrName().endsWith("s");

        setSign(shop.getWorld(), user.nickDisplayName()
                .append(Component.text("'" + (endsInS ? "" : "s")))
        );

        removeEntity(shop.getWorld());*/
    }

    public void onUnclaim(World world) {
        //Above door sign
        setSign(world, Component.text("Available player"));

        //Notice
        removeEntity(world);
    }

    private void removeEntity(World world) {
        FtcBoundingBox area = FtcBoundingBox.of(world, notice, 5);

        area.getEntities().forEach(a -> {
            if(!a.getPersistentDataContainer().has(NOTICE_KEY, PersistentDataType.BYTE)) return;
            a.remove();
        });
    }

    private void setSign(World world, Component signTitle) {
        Block block = doorSign.getBlock(world);
        block.setType(Material.BIRCH_WALL_SIGN);

        Directional signData = (Directional) block.getBlockData();
        signData.setFacing(direction);
        block.setBlockData(signData);

        Sign sign = (Sign) block.getState();
        sign.line(0, Component.empty());
        sign.line(1, signTitle);
        sign.line(2, Component.text("shop"));
        sign.line(3, Component.empty());

        sign.update();
    }

    public static ShopEntrance fromJson(JsonElement element) {
        JsonBuf json = JsonBuf.of(element.getAsJsonObject());

        return new ShopEntrance(
                json.getEnum("direction", BlockFace.class),
                Vector3i.of(json.get("notice")),
                Vector3i.of(json.get("doorSign"))
        );
    }

    @Override
    public JsonObject serialize() {
        JsonBuf json = JsonBuf.empty();

        json.addEnum("direction", direction);
        json.add("notice", notice.serialize());
        json.add("doorSign", doorSign.serialize());

        return json.getSource();
    }
}
