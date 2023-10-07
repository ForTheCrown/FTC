package net.forthecrown.usables.actions;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import lombok.Getter;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.options.ArgumentOption;
import net.forthecrown.grenadier.types.options.Options;
import net.forthecrown.grenadier.types.options.OptionsArgument;
import net.forthecrown.grenadier.types.options.ParsedOptions;
import net.forthecrown.text.Text;
import net.forthecrown.usables.Action;
import net.forthecrown.usables.BuiltType;
import net.forthecrown.usables.Interaction;
import net.forthecrown.usables.UsableComponent;
import net.forthecrown.usables.ObjectType;
import net.forthecrown.utils.io.FtcCodecs;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Source;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

@Getter
public class SoundAction implements Action {

  static final Codec<Sound> SOUND_CODEC = RecordCodecBuilder.create(instance -> {
    return instance
        .group(
            FtcCodecs.KYORI_KEY.fieldOf("id").forGetter(Sound::name),

            FtcCodecs.enumCodec(Source.class)
                .optionalFieldOf("source", Source.MASTER)
                .forGetter(Sound::source),

            Codec.FLOAT.optionalFieldOf("volume", 1f).forGetter(Sound::volume),
            Codec.FLOAT.optionalFieldOf("pitch", 1f).forGetter(Sound::pitch)
        )

        .apply(instance, Sound::sound);
  });

  static final Codec<SoundAction> CODEC = RecordCodecBuilder.create(instance -> {
    return instance
        .group(
            SOUND_CODEC.fieldOf("sound").forGetter(o -> o.sound),
            Codec.DOUBLE.optionalFieldOf("play_radius", 0.0D).forGetter(o -> o.playRadius)
        )

        .apply(instance, SoundAction::new);
  });

  static final ArgumentOption<NamespacedKey> SOUND = Options.argument(ArgumentTypes.key())
      .setLabel("sound")
      .setSuggester((context, builder) -> Completions.suggestKeyed(builder, Registry.SOUNDS))
      .build();

  static final ArgumentOption<Float> PITCH
      = Options.argument(FloatArgumentType.floatArg(0, 2))
      .setLabel("pitch")
      .setDefaultValue(1f)
      .build();

  static final ArgumentOption<Float> VOLUME
      = Options.argument(FloatArgumentType.floatArg(0))
      .setDefaultValue(1f)
      .setLabel("volume")
      .build();

  static final ArgumentOption<Source> CHANNEL
      = Options.argument(ArgumentTypes.enumType(Source.class))
      .setLabel("channel")
      .setDefaultValue(Source.MASTER)
      .build();

  static final ArgumentOption<Double> PLAY_RADIUS = Options.argument(DoubleArgumentType.doubleArg(0d))
      .setLabel("play-radius")
      .setDefaultValue(0d)
      .build();

  static final OptionsArgument OPTIONS = OptionsArgument.builder()
      .addRequired(SOUND)
      .addOptional(PITCH)
      .addOptional(VOLUME)
      .addOptional(CHANNEL)
      .addOptional(PLAY_RADIUS)
      .build();

  static final ObjectType<SoundAction> TYPE = BuiltType.<SoundAction>builder()
      .saver((value, ops) -> CODEC.encodeStart(ops, value))
      .loader(CODEC::parse)

      .parser((reader, source) -> {
        ParsedOptions options = OPTIONS.parse(reader);
        options.checkAccess(source);

        Sound sound = Sound.sound()
            .type(options.getValue(SOUND))
            .pitch(options.getValue(PITCH))
            .volume(options.getValue(VOLUME))
            .source(options.getValue(CHANNEL))
            .build();

        double radius = options.getValue(PLAY_RADIUS);

        return new SoundAction(sound, radius);
      })

      .suggester(OPTIONS::listSuggestions)
      .build();

  private final Sound sound;
  private final double playRadius;

  public SoundAction(Sound sound, double playRadius) {
    this.sound = sound;
    this.playRadius = playRadius;
  }

  @Override
  public void onUse(Interaction interaction) {
    Optional<Location> locationOpt = interaction.getValue("location", Location.class);

    if (playRadius <= 0 || locationOpt.isEmpty()) {
      interaction.player().playSound(sound);
      return;
    }

    Location location = locationOpt.get();
    for (Player player : location.getNearbyPlayers(playRadius)) {
      player.playSound(sound, location.x(), location.y(), location.z());
    }
  }

  @Override
  public @Nullable Component displayInfo() {
    return Text.format("{0}, vol={1, number}, pitch={2, number}, radius={3, number}",
        sound.name(),
        sound.volume(),
        sound.pitch(),
        playRadius
    );
  }

  @Override
  public ObjectType<? extends UsableComponent> getType() {
    return TYPE;
  }
}


