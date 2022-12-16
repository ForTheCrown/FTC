package net.forthecrown.core.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.forthecrown.core.module.OnEnable;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.utils.math.WorldVec3i;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.server.players.BanListEntry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.spongepowered.math.vector.Vector2d;
import org.spongepowered.math.vector.Vector2i;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.io.IOException;
import java.nio.file.Path;

public final class Configs {
    private Configs() {}

    static final Gson GSON = createGson();
    public static final Path DIRECTORY = PathUtil.getPluginDirectory("config");

    /* ----------------------------- METHODS ------------------------------ */

    @OnEnable
    private static void init() {
        ConfigManager manager = ConfigManager.get();

        manager.registerConfig(EndConfig.class);
        manager.registerConfig(ResourceWorldConfig.class);
        manager.registerConfig(GeneralConfig.class);
        manager.registerConfig(JoinInfo.class);
        manager.registerConfig(ServerRules.class);
    }

    private static Gson createGson() {
        GsonBuilder builder = new GsonBuilder()
                .setPrettyPrinting()

                .setDateFormat(BanListEntry.DATE_FORMAT.toPattern())

                .registerTypeHierarchyAdapter(
                        Key.class,
                        JsonUtils.createAdapter(JsonUtils::writeKey, JsonUtils::readKey)
                )

                .registerTypeAdapter(
                        Location.class,
                        JsonUtils.createAdapter(JsonUtils::writeLocation, JsonUtils::readLocation)
                )

                .registerTypeHierarchyAdapter(
                        World.class,

                        new TypeAdapter<World>() {
                            @Override
                            public void write(JsonWriter out, World value) throws IOException {
                                out.value(value.getName());
                            }

                            @Override
                            public World read(JsonReader in) throws IOException {
                                return Bukkit.getWorld(in.nextString());
                            }
                        }
                )

                .registerTypeAdapter(
                        Bounds3i.class,
                        JsonUtils.createAdapter(Bounds3i::serialize, Bounds3i::of)
                )

                .registerTypeHierarchyAdapter(
                        LongList.class,
                        new TypeAdapter<LongList>() {
                            @Override
                            public void write(JsonWriter out, LongList value) throws IOException {
                                out.beginArray();

                                if (value != null && !value.isEmpty()) {
                                    for (var l: value) {
                                        out.value(l);
                                    }
                                }

                                out.endArray();
                            }

                            @Override
                            public LongList read(JsonReader in) throws IOException {
                                LongList result = new LongArrayList();
                                in.beginArray();

                                while (in.peek() != JsonToken.END_ARRAY) {
                                    result.add(in.nextLong());
                                }

                                in.endArray();
                                return result;
                            }
                        }
                )

                .registerTypeAdapter(
                        Vector3i.class, JsonUtils.createAdapter(Vectors::writeJson, Vectors::read3i)
                )
                .registerTypeAdapter(
                        Vector3d.class, JsonUtils.createAdapter(Vectors::writeJson, Vectors::read3d)
                )
                .registerTypeAdapter(
                        Vector2i.class, JsonUtils.createAdapter(Vectors::writeJson, Vectors::read2i)
                )
                .registerTypeAdapter(
                        Vector2d.class, JsonUtils.createAdapter(Vectors::writeJson, Vectors::read2d)
                )
                .registerTypeAdapter(
                        WorldVec3i.class, JsonUtils.createAdapter(WorldVec3i::serialize, WorldVec3i::of)
                );

        return GsonComponentSerializer.gson()
                .populator()
                .apply(builder)
                .create();
    }
}