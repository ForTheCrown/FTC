package net.forthecrown.guilds;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.forthecrown.core.FTC;
import net.forthecrown.guilds.unlockables.*;

public class GuildUnlockables {
    private final Object2IntMap<Unlockable> progressMap = new Object2IntOpenHashMap<>();

    public int getExpProgress(Unlockable feature) {
        return this.progressMap.getOrDefault(feature, 0);
    }

    public void setExpProgress(Unlockable feature, int progress) {
        this.progressMap.put(feature, progress);
    }

    // Get GuildUnlockables from Json
    public void deserialize(JsonObject json) {
        progressMap.clear();

        for (var e: json.entrySet()) {
            Unlockables.REGISTRY.get(e.getKey())
                    .ifPresentOrElse(
                            unlockable -> setExpProgress(unlockable, e.getValue().getAsInt()),
                            () -> FTC.getLogger().warn("Unknown unlockable: '{}'", e.getKey())
                    );
        }
    }

    // Get Json from GuildUnlockables
    public JsonObject serialize() {
        JsonObject result = new JsonObject();

        // Rank slots
        progressMap.forEach((feature, progress) -> {
            if (progress > 0) {
                result.addProperty(feature.getKey(), progress);
            }
        });

        return result;
    }
}