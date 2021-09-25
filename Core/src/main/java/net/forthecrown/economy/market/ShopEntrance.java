package net.forthecrown.economy.market;

import com.destroystokyo.paper.profile.CraftPlayerProfile;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.squire.Squire;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.math.FtcBoundingBox;
import net.forthecrown.utils.math.Vector3i;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Slime;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class ShopEntrance implements JsonSerializable {
    public static final NamespacedKey NOTICE_KEY = Squire.createFtcKey("market_notice");
    public static final CraftPlayerProfile NOTICE_PROFILE = FtcUtils.profileWithTextureID(
            "Pearl", UUID.randomUUID(),
            "7d16ae951120394f368f2250b7c3ad3fb12cea55ec1b2db5a94d1fb7fd4b6fa"
    );

    public final BlockFace direction;
    public final Vector3i notice;
    public final Vector3i doorSign;

    public ShopEntrance(BlockFace direction, Vector3i notice, Vector3i doorSign) {
        this.direction = direction;
        this.notice = notice;
        this.doorSign = doorSign;
    }

    public void onClaim(CrownUser user, World world) {
        boolean endsWithS = user.getNickOrName().endsWith("s");

        setSign(world, user.nickDisplayName().append(Component.text("'" + (endsWithS ? "" : "s"))));

        removeNotice(world);
    }

    public void onUnclaim(World world, MarketShop shop) {
        //Above door sign
        setSign(world, Component.text("Available player"));

        //Notice
        removeNotice(world);
        spawnNotice(world, shop);
    }

    private void spawnNotice(World world, MarketShop shop) {
        Block block = notice.getBlock(world);
        block.setType(Material.PLAYER_HEAD);

        Directional directional = (Directional) block.getBlockData();
        directional.setFacing(direction);
        block.setBlockData(directional);

        Skull skull = (Skull) block.getState();
        skull.setPlayerProfile(NOTICE_PROFILE);
        skull.update();

        Location l = new Location(world, notice.getX() + 0.5D, notice.getY(), notice.getZ() + 0.5D);
        world.spawn(l, Slime.class, slime -> {
            slime.getPersistentDataContainer().set(NOTICE_KEY, PersistentDataType.STRING, shop.getName());
            slime.setSize(0);

            slime.setAI(false);
            slime.setGravity(false);
            slime.setRemoveWhenFarAway(false);

            FtcUtils.getNoClipTeam().addEntry(slime.getUniqueId().toString());

            slime.customName(
                    Component.text("Price: ")
                            .color(NamedTextColor.GRAY)
                            .append(FtcFormatter.rhinesNonTrans(shop.getPrice()).color(NamedTextColor.YELLOW))
            );
        });
    }

    private void removeNotice(World world) {
        FtcBoundingBox area = FtcBoundingBox.of(world, notice, 1.5);

        area.getEntities().forEach(a -> {
            if(!a.getPersistentDataContainer().has(NOTICE_KEY, PersistentDataType.STRING)) return;
            a.remove();
        });

        notice.getBlock(world).setType(Material.AIR);
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
        JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());

        return new ShopEntrance(
                json.getEnum("direction", BlockFace.class),
                Vector3i.of(json.get("notice")),
                Vector3i.of(json.get("doorSign"))
        );
    }

    @Override
    public JsonObject serialize() {
        JsonWrapper json = JsonWrapper.empty();

        json.addEnum("direction", direction);
        json.add("notice", notice.serialize());
        json.add("doorSign", doorSign.serialize());

        return json.getSource();
    }
}
