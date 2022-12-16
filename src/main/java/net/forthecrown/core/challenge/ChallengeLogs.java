package net.forthecrown.core.challenge;

import com.mojang.serialization.Codec;
import lombok.experimental.UtilityClass;
import net.forthecrown.core.module.OnEnable;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.log.LogSchema;
import net.forthecrown.log.SchemaField;
import net.forthecrown.utils.io.FtcCodecs;
import net.minecraft.core.UUIDUtil;

import java.util.UUID;

public @UtilityClass class ChallengeLogs {
    /* ---------------------------- COMPLETIONS ----------------------------- */

    public final Holder<LogSchema> COMPLETED;

    public final SchemaField<UUID> C_PLAYER;
    public final SchemaField<String> C_CHALLENGE;

    static {
        var builder = LogSchema.builder("challenges/completed");

        C_PLAYER = builder.add("player", UUIDUtil.CODEC);
        C_CHALLENGE = builder.add("challenge", FtcCodecs.KEY_CODEC);

        COMPLETED = builder.register();
    }

    /* ------------------------------ ACTIVES ------------------------------- */

    public final Holder<LogSchema> ACTIVE;

    public final SchemaField<String> A_CHALLENGE;
    public final SchemaField<ResetInterval> A_TYPE;
    public final SchemaField<String> A_EXTRA;

    static {
        var builder = LogSchema.builder("challenges/active");

        A_CHALLENGE = builder.add("challenge", Codec.STRING);
        A_EXTRA     = builder.add("extra", Codec.STRING);
        A_TYPE      = builder.add("type", FtcCodecs.enumCodec(ResetInterval.class));

        ACTIVE = builder.register();
    }

    /* ------------------------------ STREAKS ------------------------------- */

    public final Holder<LogSchema> STREAK_SCHEMA;

    public final SchemaField<UUID> S_PLAYER;
    public final SchemaField<StreakCategory> S_CATEGORY;

    static {
        var builder = LogSchema.builder("challenges/streaks");

        S_PLAYER = builder.add("player", UUIDUtil.CODEC);
        S_CATEGORY = builder.add(
                "category",
                FtcCodecs.enumCodec(StreakCategory.class)
        );

        STREAK_SCHEMA = builder.register();
    }

    @OnEnable
    static void init() {
        // Empty thing to force class load
    }
}