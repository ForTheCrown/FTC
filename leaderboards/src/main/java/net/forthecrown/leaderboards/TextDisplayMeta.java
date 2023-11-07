package net.forthecrown.leaderboards;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.forthecrown.utils.io.FtcCodecs;
import net.forthecrown.utils.io.Results;
import net.forthecrown.utils.math.Vectors;
import org.bukkit.Color;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.Display.Brightness;
import org.bukkit.entity.TextDisplay;
import org.bukkit.entity.TextDisplay.TextAlignment;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.spongepowered.math.vector.Vector3f;

@Getter @Setter
@NoArgsConstructor
public class TextDisplayMeta {

  public static final int DEFAULT_LINE_WIDTH = 200;
  public static final Color DEFAULT_COLOR = Color.fromARGB(0x40000000);

  private static final Codec<Brightness> BRIGHTNESS_CODEC = RecordCodecBuilder.create(instance -> {
    return instance
        .group(
            Codec.INT.fieldOf("block_light").forGetter(Brightness::getBlockLight),
            Codec.INT.fieldOf("sky_light").forGetter(Brightness::getSkyLight)
        )
        .apply(instance, Brightness::new);
  });

  private static Codec<Color> COLOR_CODEC = Codec.STRING.comapFlatMap(
      s -> {
        String parseStr;
        if (s.startsWith("0x")) {
          parseStr = s.substring(2);
        } else if (s.startsWith("#")) {
          parseStr = s.substring(1);
        } else {
          parseStr = s;
        }
        try {
          int v = Integer.parseUnsignedInt(parseStr, 16);
          return Results.success(Color.fromARGB(v));
        } catch (NumberFormatException exc) {
          return Results.error(exc.getMessage());
        }
      },
      color -> "0x" + Integer.toHexString(color.asARGB())
  );

  public static final Codec<TextDisplayMeta> CODEC = RecordCodecBuilder.create(instance -> {
    return instance
        .group(
            Vectors.V3F_CODEC.optionalFieldOf("scale", Vector3f.ONE)
                .forGetter(TextDisplayMeta::getScale),

            Vectors.V3F_CODEC.optionalFieldOf("translation", Vector3f.ZERO)
                .forGetter(TextDisplayMeta::getTranslation),

            FtcCodecs.enumCodec(Billboard.class)
                .optionalFieldOf("billboard", Billboard.FIXED)
                .forGetter(TextDisplayMeta::getBillboard),

            FtcCodecs.enumCodec(TextAlignment.class)
                .optionalFieldOf("text_align", TextAlignment.CENTER)
                .forGetter(TextDisplayMeta::getAlign),

            BRIGHTNESS_CODEC.optionalFieldOf("brightness")
                .forGetter(m -> Optional.ofNullable(m.brightness)),

            COLOR_CODEC.optionalFieldOf("background_color")
                .forGetter(m -> Optional.ofNullable(m.backgroundColor)),

            Codec.FLOAT.optionalFieldOf("yaw", 0f).forGetter(TextDisplayMeta::getYaw),
            Codec.FLOAT.optionalFieldOf("pitch", 0f).forGetter(TextDisplayMeta::getPitch),
            Codec.BOOL.optionalFieldOf("shadowed", false).forGetter(TextDisplayMeta::isShadowed),
            Codec.BOOL.optionalFieldOf("see_through", false).forGetter(TextDisplayMeta::isSeeThrough),
            Codec.INT.optionalFieldOf("line_width", -1).forGetter(TextDisplayMeta::getLineWidth),
            Codec.BYTE.optionalFieldOf("opacity", (byte) -1).forGetter(TextDisplayMeta::getOpacity)
        )
        .apply(instance, TextDisplayMeta::new);
  });

  private Vector3f scale = Vector3f.ONE;
  private Vector3f translation = Vector3f.ZERO;
  private Billboard billboard = Billboard.FIXED;
  private TextAlignment align = TextAlignment.CENTER;
  private Brightness brightness;
  private Color backgroundColor;

  private float yaw;
  private float pitch;

  private boolean shadowed;
  private boolean seeThrough;
  private int lineWidth = -1;
  private byte opacity  = -1;

  private TextDisplayMeta(
      Vector3f scale,
      Vector3f translation,
      Billboard billboard,
      TextAlignment align,
      Optional<Brightness> brightness,
      Optional<Color> backgroundColor,
      float yaw,
      float pitch,
      boolean shadowed,
      boolean seeThrough,
      int lineWidth,
      byte opacity
  ) {
    this.scale = scale;
    this.translation = translation;
    this.billboard = billboard;
    this.align = align;
    this.brightness = brightness.orElse(null);
    this.backgroundColor = backgroundColor.orElse(null);
    this.yaw = yaw;
    this.pitch = pitch;
    this.shadowed = shadowed;
    this.seeThrough = seeThrough;
    this.lineWidth = lineWidth;
    this.opacity = opacity;
  }

  public void setScale(Vector3f scale) {
    Objects.requireNonNull(scale, "Null scale");
    this.scale = scale;
  }

  public void setTranslation(Vector3f translation) {
    Objects.requireNonNull(translation, "Null translation");
    this.translation = translation;
  }

  public void apply(TextDisplay display) {
    if (lineWidth != -1) {
      display.setLineWidth(lineWidth);
    } else {
      display.setLineWidth(DEFAULT_LINE_WIDTH);
    }

    if (backgroundColor == null) {
      display.setBackgroundColor(DEFAULT_COLOR);
    } else {
      display.setBackgroundColor(backgroundColor);
    }

    display.setRotation(yaw, pitch);
    display.setShadowed(shadowed);
    display.setSeeThrough(seeThrough);
    display.setTextOpacity(opacity);
    display.setAlignment(align);
    display.setBrightness(brightness);
    display.setBillboard(Objects.requireNonNullElse(billboard, Billboard.FIXED));

    Transformation transformation = new Transformation(
        new org.joml.Vector3f(translation.x(), translation.y(), translation.z()),
        new AxisAngle4f(),
        new org.joml.Vector3f(scale.x(), scale.y(), scale.z()),
        new AxisAngle4f()
    );
    display.setTransformation(transformation);
  }
}
