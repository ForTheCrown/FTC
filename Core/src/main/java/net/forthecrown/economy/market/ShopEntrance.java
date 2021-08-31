package net.forthecrown.economy.market;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.forthecrown.serializer.JsonBuf;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.math.Vector3i;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;

public class ShopEntrance implements JsonSerializable {
    public final BlockFace direction;
    public final Vector3i lecternPos;
    public final Vector3i signPos;

    public ShopEntrance(BlockFace direction, Vector3i lecternPos, Vector3i signPos) {
        this.direction = direction;
        this.lecternPos = lecternPos;
        this.signPos = signPos;
    }

    public void onClaim(CrownUser user, FtcMarketShop shop) {
        //Above door sign
        boolean endsInS = user.getNickOrName().endsWith("s");

        setSign(shop.getWorld(), user.nickDisplayName()
                .append(Component.text("'" + (endsInS ? "" : "s")))
        );

        //Remove lectern
        Block lectern = lecternPos.getBlock(shop.getWorld());
        lectern.setType(Material.AIR);
    }

    public void onUnclaim(World world) {
        //Above door sign
        setSign(world, Component.text("Available player"));

        //Notice
    }

    private void setSign(World world, Component signTitle) {
        Block block = signPos.getBlock(world);
        block.setType(Material.BIRCH_WALL_SIGN);

        Directional signData = (Directional) block.getBlockData();
        signData.setFacing(direction);

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
                Vector3i.of(json.get("lectern")),
                Vector3i.of(json.get("sign"))
        );
    }

    @Override
    public JsonObject serialize() {
        JsonBuf json = JsonBuf.empty();

        json.addEnum("direction", direction);
        json.add("lectern", lecternPos.serialize());
        json.add("sign", signPos.serialize());

        return json.getSource();
    }
}
