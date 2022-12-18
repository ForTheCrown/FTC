package net.forthecrown.core.challenge;

import com.mojang.serialization.Codec;
import java.util.UUID;
import net.forthecrown.core.module.OnEnable;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.log.LogSchema;
import net.forthecrown.log.SchemaField;
import net.forthecrown.utils.io.FtcCodecs;
import net.minecraft.core.UUIDUtil;

public final class ChallengeLogs {
  /* ---------------------------- COMPLETIONS ----------------------------- */

  public static final Holder<LogSchema> COMPLETED;

  public static final SchemaField<UUID> C_PLAYER;
  public static final SchemaField<String> C_CHALLENGE;

  static {
    var builder = LogSchema.builder("challenges/completed");

    C_PLAYER = builder.add("player", UUIDUtil.CODEC);
    C_CHALLENGE = builder.add("challenge", FtcCodecs.KEY_CODEC);

    COMPLETED = builder.register();
  }

  /* ------------------------------ ACTIVES ------------------------------- */

  public static final Holder<LogSchema> ACTIVE;

  public static final SchemaField<String> A_CHALLENGE;
  public static final SchemaField<ResetInterval> A_TYPE;
  public static final SchemaField<String> A_EXTRA;

  static {
    var builder = LogSchema.builder("challenges/active");

    A_CHALLENGE = builder.add("challenge", Codec.STRING);
    A_EXTRA = builder.add("extra", Codec.STRING);
    A_TYPE = builder.add("type", FtcCodecs.enumCodec(ResetInterval.class));

    ACTIVE = builder.register();
  }

  /* ------------------------------ STREAKS ------------------------------- */

  public static final Holder<LogSchema> STREAK_SCHEMA;

  public static final SchemaField<UUID> S_PLAYER;
  public static final SchemaField<StreakCategory> S_CATEGORY;

  static {
    var builder = LogSchema.builder("challenges/streaks");

    S_PLAYER = builder.add("player", UUIDUtil.CODEC);
    S_CATEGORY = builder.add(
        "category",
        FtcCodecs.enumCodec(StreakCategory.class)
    );

    STREAK_SCHEMA = builder.register();
  }

  private ChallengeLogs() {
    throw new UnsupportedOperationException(
        "This is a utility class and cannot be instantiated");
  }

  @OnEnable
  static void init() {
    // Empty thing to force class load
  }
}