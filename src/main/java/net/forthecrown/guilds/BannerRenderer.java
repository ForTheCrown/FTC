package net.forthecrown.guilds;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import lombok.Getter;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.utils.MinecraftAssetDownloader;
import net.forthecrown.utils.io.PathUtil;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

@Getter
public class BannerRenderer {
  private static final Logger LOGGER = Loggers.getLogger();

  /** Banner texture width */
  public static final int BANNER_WIDTH = 20;

  /** Banner texture height */
  public static final int BANNER_HEIGHT = 40;

  /**
   * The scalar applied images resulting from {@link #renderSquare(ItemStack)}.
   * <p>
   * Done to raise the normally 20x40 banner textures above Discord's required
   * 64x64 image size.
   */
  public static final int IMAGE_SCALE = 8;

  /** The X offset of the banner's front face texture */
  public static final int START_OFFSET_X = 1;

  /** The Y offset of the banner's front face texture */
  public static final int START_OFFSET_Y = 1;

  private final Path assetsPath;
  private final Path bannerTextureDirectory;

  private final Map<PatternType, String> typeFilenames
      = new Object2ObjectOpenHashMap<>();

  public BannerRenderer() throws IOException {
    fillFileNames();
    assetsPath = Path.of("mc_assets");

    // Get the banner directory
    this.bannerTextureDirectory = assetsPath.resolve("assets")
        .resolve("minecraft")
        .resolve("textures")
        .resolve("entity")
        .resolve("banner");

    // Optionally download assets if the assets directory doesn't exist or
    // is from a previous version
    if (shouldDownloadAssets()) {
      PathUtil.safeDelete(assetsPath);

      MinecraftAssetDownloader.download(assetsPath, "latest");

      Files.writeString(
          assetsPath.resolve("version.txt"),
          Bukkit.getMinecraftVersion(),
          StandardCharsets.UTF_8
      );
    }
  }

  /**
   * Fills the pattern filename lookup map with filenames from the vanilla
   * pattern registry
   */
  private void fillFileNames() {
    Registry<BannerPattern> registry = BuiltInRegistries.BANNER_PATTERN;

    registry.entrySet().forEach(entry -> {
      var fileName = entry.getKey().location().getPath();
      PatternType type = PatternType.getByIdentifier(entry.getValue().getHashname());

      if (type == null) {
        LOGGER.warn(
            "Vanilla pattern type {} doesn't have matching bukkit value",
            fileName
        );
        return;
      }

      typeFilenames.put(type, fileName);
    });
  }

  private boolean shouldDownloadAssets() throws IOException {
    final Path versionTxt = assetsPath.resolve("version.txt");

    if (!Files.exists(versionTxt)) {
      return true;
    }

    var txt = Files.readString(versionTxt, StandardCharsets.UTF_8);
    var mcVersion = Bukkit.getMinecraftVersion();

    return !txt.equalsIgnoreCase(mcVersion);
  }

  private DyeColor getBaseColor(BannerMeta meta, ItemStack item) {
    var base = meta.getBaseColor();

    if (base != null) {
      return base;
    }

    var metaName = item.getType().name()
        .replaceAll("_BANNER", "");

    return DyeColor.valueOf(metaName);
  }

  /**
   * Renders the given banner itemStack as a square PNG, adding blank pixels
   * to the sides to preserve image quality.
   *
   * @param item The banner item to render
   * @return The rendered image
   *
   * @throws IOException If the texture of one of the banner's patterns could
   *                     not be read
   */
  public BufferedImage renderSquare(ItemStack item) throws IOException {
    var meta = item.getItemMeta();
    Validate.isInstanceOf(BannerMeta.class, meta);
    BannerMeta bannerMeta = (BannerMeta) meta;

    List<Pattern> patterns = bannerMeta.getPatterns();
    Pattern base = new Pattern(
        getBaseColor(bannerMeta, item),
        PatternType.BASE
    );

    patterns.add(0, base);

    // Scale the image's bounds
    int size = BANNER_HEIGHT * IMAGE_SCALE;

    // Create image and create graphics renderer for image
    BufferedImage result = new BufferedImage(size, size, TYPE_INT_ARGB);
    Graphics2D g2d = result.createGraphics();

    int xOffset = ((BANNER_HEIGHT - BANNER_WIDTH) / 2);

    // Create transform to scale all given inputs
    AffineTransform transform = AffineTransform.getScaleInstance(
        IMAGE_SCALE, IMAGE_SCALE
    );
    transform.translate(xOffset, 0);
    g2d.setTransform(transform);

    // Draw all patterns
    for (Pattern pattern : patterns) {
      Color color = new Color(pattern.getColor().getColor().asRGB(), true);

      float r = ((float) color.getRed())   / 255.0F;
      float g = ((float) color.getGreen()) / 255.0F;
      float b = ((float) color.getBlue())  / 255.0F;

      Path path = getPatternPath(pattern.getPattern());
      BufferedImage texture = ImageIO.read(Files.newInputStream(path));

      int startX = START_OFFSET_X;
      int endX = startX + BANNER_WIDTH;

      int startY = START_OFFSET_Y;
      int endY = startY + BANNER_HEIGHT;

      for (int x = startX; x < endX; x++) {
        for (int y = startY; y < endY; y++) {
          Color pixelColor = new Color(texture.getRGB(x, y), true);

          // Map byte color values to 0-1 float values
          float pixelR = ((float) pixelColor.getRed())   / 255.0F;
          float pixelG = ((float) pixelColor.getGreen()) / 255.0F;
          float pixelB = ((float) pixelColor.getBlue())  / 255.0F;
          float pixelA = ((float) pixelColor.getAlpha()) / 255.0F;

          // No alpha, means no pixel at pos
          if (pixelA <= 0) {
            continue;
          }

          // Multiply colors
          float resultR = pixelR * r;
          float resultG = pixelG * g;
          float resultB = pixelB * b;

          Color c = new Color(resultR, resultG, resultB, pixelA);

          // Fill rectangle
          g2d.setColor(c);

          // Size of 1 and off set from 1, 1, we don't care about any
          // offsets or sizes because the g2d's affine transform takes
          // care of that
          g2d.fillRect(x - startX, y - startY, 1, 1);
        }
      }
    }

    g2d.dispose();
    return result;
  }

  public Path getPatternPath(PatternType type) {
    var fName = typeFilenames.get(type);

    if (fName == null) {
      LOGGER.warn("Pattern type {} doesn't have set filename, returning type name",
          type
      );

      return bannerTextureDirectory
          .resolve(type.name().toLowerCase() + ".png");
    }

    return bannerTextureDirectory.resolve(fName + ".png");
  }
}