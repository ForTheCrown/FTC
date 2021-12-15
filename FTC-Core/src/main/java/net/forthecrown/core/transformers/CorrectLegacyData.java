package net.forthecrown.core.transformers;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.commands.CommandArkBox;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.user.data.RankTier;
import net.forthecrown.utils.JsonUtils;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CorrectLegacyData {
    public static void runAsync() {
        CompletableFuture.runAsync(() -> {
            CorrectLegacyData data = new CorrectLegacyData();

            try {
                data.run();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void run() throws IOException {
        Map<UUID, CommandArkBox.ArkBoxInfo> arkData = CommandArkBox.ID_2_DATA;

        File userDataDir = new File("old_user_data");
        Validate.isTrue(userDataDir.exists() && userDataDir.isDirectory());

        File[] files = userDataDir.listFiles();
        int minimumOrdinal = RankTier.FREE.ordinal();

        for (File f: files) {
            LegacyUser user = new LegacyUser(f);
            CommandArkBox.ArkBoxInfo info = arkData.computeIfAbsent(user.uniqueId, uuid -> new CommandArkBox.ArkBoxInfo(null, user.tier));

            if(user.tier.ordinal() < minimumOrdinal) continue;
            info.tier = user.tier;
        }

        CommandArkBox.save();
    }

    private static class LegacyUser {
        private final UUID uniqueId;
        private final RankTier tier;
        private final List<LegacyRank> ranks = new ObjectArrayList<>();

        private LegacyUser(File file) throws IOException {
            this.uniqueId = UUID.fromString(file.getName().substring(0, file.getName().indexOf('.')));

            JsonObject jsonObject = JsonUtils.readFileObject(file);
            JsonWrapper json = JsonWrapper.of(jsonObject);

            ranks.addAll(json.getList("ranks", e -> JsonUtils.readEnum(LegacyRank.class, e), new ObjectArrayList<>()));

            RankTier highest = RankTier.NONE;
            for (LegacyRank r: ranks) {
                if(r.tier.ordinal() <= highest.ordinal()) continue;
                highest = r.tier;
            }

            this.tier = highest;
        }
    }

    private enum LegacyRank {
        KNIGHT (        RankTier.FREE),
        BARON (         RankTier.FREE),
        BARONESS(       RankTier.FREE),
        LORD (          RankTier.TIER_1),
        LADY(           RankTier.TIER_1),
        DUKE (          RankTier.TIER_2),
        DUCHESS(        RankTier.TIER_2),
        PRINCE (        RankTier.TIER_3),
        PRINCESS (      RankTier.TIER_3),

        SAILOR (        RankTier.FREE),
        PIRATE (        RankTier.TIER_1),
        CAPTAIN (       RankTier.TIER_2),
        ADMIRAL (       RankTier.TIER_3),

        VIKING (        RankTier.FREE),
        BERSERKER (     RankTier.FREE),
        WARRIOR (       RankTier.TIER_1),
        SHIELD_MAIDEN ( RankTier.TIER_1),
        HERSIR (        RankTier.TIER_2),
        JARL (          RankTier.TIER_3),

        DEFAULT(        RankTier.NONE),
        LEGEND(         RankTier.TIER_3);

        private final RankTier tier;

        LegacyRank(RankTier tier) {
            this.tier = tier;
        }
    }
}
