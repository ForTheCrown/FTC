package net.forthecrown.economy.market;

import com.destroystokyo.paper.profile.CraftPlayerProfile;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.forthecrown.core.Keys;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.math.Vector3i;
import net.forthecrown.utils.transformation.FtcBoundingBox;
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
import org.bukkit.block.data.Rotatable;
import org.bukkit.entity.Slime;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

/**
 * A shop's entrance
 * <p></p>
 * Used for setting and changing the shop's owner sign and purchase notice
 */
public class ShopEntrance implements JsonSerializable {
    public static final NamespacedKey NOTICE_KEY = Keys.key("ftc", "market_notice");
    public static final NamespacedKey DOOR_SIGN = Keys.key("ftc", "door_sign");

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

    /**
     * Sets the sign to the user's name and removes the purchase notice
     * @param user The user that claimed the shop
     * @param world The world the shop is in
     */
    public void onClaim(CrownUser user, World world) {
        boolean endsWithS = user.getNickOrName().endsWith("s");

        setSign(world, user.nickDisplayName().append(Component.text("'" + (endsWithS ? "" : "s"))));

        removeNotice(world);
    }

    /**
     * Spawms the purchase notice and sets the sign above the door to say "Available"
     * @param world The world the shop is in
     * @param shop The shop itself
     */
    public void onUnclaim(World world, MarketShop shop) {
        //Above door sign
        setSign(world, Component.text("Available player"));

        //Notice
        removeNotice(world);
        spawnNotice(world, shop);
    }

    //Spawns the notice in the given world with info for the given shop
    private void spawnNotice(World world, MarketShop shop) {
        //Set block type
        Block block = notice.getBlock(world);
        block.setType(Material.PLAYER_HEAD);

        //Place sign in correct orientation
        Rotatable directional = (Rotatable) block.getBlockData();
        directional.setRotation(direction);
        block.setBlockData(directional);

        //Floating crystal head
        Skull skull = (Skull) block.getState();
        skull.getPersistentDataContainer().set(NOTICE_KEY, PersistentDataType.STRING, shop.getName());
        skull.setPlayerProfile(NOTICE_PROFILE);
        skull.update();

        //Slime, which player's can click on
        Location l = new Location(world, notice.getX() + 0.5D, notice.getY(), notice.getZ() + 0.5D);
        world.spawn(l, Slime.class, slime -> {
            slime.getPersistentDataContainer().set(NOTICE_KEY, PersistentDataType.STRING, shop.getName());
            slime.setSize(0);

            slime.setAI(false);
            slime.setGravity(false);
            slime.setInvulnerable(true);
            slime.setRemoveWhenFarAway(false);

            //Add to no clip team
            FtcUtils.getNoClipTeam().addEntry(slime.getUniqueId().toString());

            //Price text
            slime.customName(
                    Component.text("Price: ")
                            .color(NamedTextColor.GRAY)
                            .append(FtcFormatter.rhinesNonTrans(shop.getPrice()).color(NamedTextColor.YELLOW))
            );
        });
    }

    //Removes the purchase notice
    void removeNotice(World world) {
        FtcBoundingBox area = FtcBoundingBox.of(world, notice, 1.5);

        area.getEntities().forEach(a -> {
            if(!a.getPersistentDataContainer().has(NOTICE_KEY, PersistentDataType.STRING)) return;
            a.remove();
        });

        notice.getBlock(world).setType(Material.AIR);
    }

    //Removes the door sign
    void removeSign(World world) {
        doorSign.getBlock(world).setType(Material.AIR);
    }

    //Creates a sign in the given world with the given title, aka the given text for the second line
    private void setSign(World world, Component signTitle) {
        //Make sign
        Block block = doorSign.getBlock(world);
        block.setType(Material.BIRCH_WALL_SIGN);

        //Orient correctly
        Directional signData = (Directional) block.getBlockData();
        signData.setFacing(direction);
        block.setBlockData(signData);

        //Set text
        Sign sign = (Sign) block.getState();
        sign.line(0, Component.empty());
        sign.line(1, signTitle);
        sign.line(2, Component.text("shop"));
        sign.line(3, Component.empty());
        sign.getPersistentDataContainer().set(DOOR_SIGN, PersistentDataType.BYTE, (byte) 1);

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