package net.forthecrown.economy.market;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.net.MalformedURLException;
import java.net.URL;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.Loggers;
import net.forthecrown.text.UnitFormat;
import net.forthecrown.user.User;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.math.Vectors;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
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
import org.bukkit.util.BoundingBox;
import org.slf4j.Logger;
import org.spongepowered.math.vector.Vector3i;

/**
 * A shop's entrance
 * <p>
 * Used for setting and changing the shop's owner sign and purchase notice
 */
@Getter
@RequiredArgsConstructor
public class ShopEntrance {
  /* ----------------------------- CONSTANTS ------------------------------ */

  public static final Logger LOGGER = Loggers.getLogger();

  public static final NamespacedKey NOTICE_KEY = new NamespacedKey("ftc", "market_notice");
  public static final NamespacedKey DOOR_SIGN = new NamespacedKey("ftc", "door_sign");

  public static final PlayerProfile NOTICE_PROFILE = createTextureProfile();

  public static final String KEY_DIRECTION = "direction";
  public static final String KEY_NOTICE_POS = "skullPosition";
  public static final String KEY_DOOR_SIGN = "doorSign";

  /* ----------------------------- INSTANCE FIELDS ------------------------------ */

  /**
   * Direction the entrance is facing, outwards relative to the shop
   */
  private final BlockFace direction;

  /**
   * Position of the pearl skull players interact with to purchase the shop
   */
  private final Vector3i purchasePearl;

  /**
   * Position of the sign above the entrance door, uses {@link #direction} to rotate the sign
   */
  private final Vector3i doorSign;

  /* ----------------------------- METHODS ------------------------------ */

  private static PlayerProfile createTextureProfile() {
    var textureLink = "http://textures.minecraft.net/texture/"
        + "7d16ae951120394f368f2250b7c3ad3fb12cea55ec1b2db5a94d1fb7fd4b6fa";

    PlayerProfile profile = Bukkit.getServer().createProfile(Identity.nil().uuid(), "Pearl");

    var textures = profile.getTextures();

    try {
      textures.setSkin(new URL(textureLink));
    } catch (MalformedURLException exc) {
      Loggers.getLogger().error("Couldn't set textures of profile", exc);
    }

    profile.setTextures(textures);
    return profile;
  }

  /**
   * Sets the sign to the user's name and removes the purchase notice
   *
   * @param user  The user that claimed the shop
   * @param world The world the shop is in
   */
  public void onClaim(User user, World world) {
    boolean endsWithS = user.getNickOrName().endsWith("s");

    setSign(world, user.displayName()
        .append(Component.text("'" + (endsWithS ? "" : "s")))
    );

    removeNotice(world);
  }

  /**
   * Spawns the purchase notice and sets the sign above the door to say "Available"
   *
   * @param world The world the shop is in
   * @param shop  The shop itself
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
    Block block = Vectors.getBlock(purchasePearl, world);
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
    Location l = new Location(
        world,
        purchasePearl.x() + 0.5D,
        purchasePearl.y(),
        purchasePearl.z() + 0.5D
    );

    world.spawn(l, Slime.class, slime -> {
      slime.getPersistentDataContainer().set(NOTICE_KEY, PersistentDataType.STRING, shop.getName());
      slime.setSize(0);

      slime.setAI(false);
      slime.setGravity(false);
      slime.setInvulnerable(true);
      slime.setRemoveWhenFarAway(false);

      //Add to no clip team
      var noClip = Bukkit.getScoreboardManager()
          .getMainScoreboard()
          .getTeam("NoClip");

      if (noClip != null) {
        noClip.addEntry(slime.getUniqueId().toString());
      } else {
        LOGGER.warn("Cannot add market shop pearl head to noClip team: Team not found");
      }

      //Price text
      slime.customName(
          Component.text("Price: ")
              .color(NamedTextColor.GRAY)
              .append(UnitFormat.rhines(shop.getPrice()).color(NamedTextColor.YELLOW))
      );
    });
  }

  //Removes the purchase notice
  void removeNotice(World world) {
    world.getNearbyEntities(BoundingBox.of(Vectors.toVec(purchasePearl), 1.5, 1.5, 1.5))
        .forEach(a -> {
          if (!a.getPersistentDataContainer().has(NOTICE_KEY, PersistentDataType.STRING)) {
            return;
          }

          a.remove();
        });

    Vectors.getBlock(purchasePearl, world).setType(Material.AIR);
  }

  //Removes the door sign
  void removeSign(World world) {
    Vectors.getBlock(doorSign, world).setType(Material.AIR);
  }

  //Creates a sign in the given world with the given title, aka the given text for the second line
  private void setSign(World world, Component signTitle) {
    //Make sign
    Block block = Vectors.getBlock(doorSign, world);
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

  /* ----------------------------- SERIALIZATION ------------------------------ */

  public static ShopEntrance deserialize(JsonElement element) {
    JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

    return new ShopEntrance(
        json.getEnum(KEY_DIRECTION, BlockFace.class),
        Vectors.read3i(json.get(KEY_NOTICE_POS)),
        Vectors.read3i(json.get(KEY_DOOR_SIGN))
    );
  }

  public JsonObject serialize() {
    JsonWrapper json = JsonWrapper.create();

    json.addEnum(KEY_DIRECTION, direction);
    json.add(KEY_NOTICE_POS, Vectors.writeJson(purchasePearl));
    json.add(KEY_DOOR_SIGN, Vectors.writeJson(doorSign));

    return json.getSource();
  }
}