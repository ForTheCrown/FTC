package net.forthecrown.valhalla.data.triggers.functions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.forthecrown.core.ForTheCrown;
import net.forthecrown.core.animation.BlockAnimation;
import net.forthecrown.registry.Registries;
import net.forthecrown.serializer.JsonBuf;
import net.forthecrown.serializer.SerializerType;
import net.forthecrown.utils.math.Vector3i;
import net.forthecrown.valhalla.Valhalla;
import net.forthecrown.valhalla.active.ActiveRaid;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public class PlayAnimationFunction implements TriggerFunction {
    public static final Key KEY = Valhalla.vikingKey("play_animation");

    public static final SerializerType<PlayAnimationFunction> TYPE = new SerializerType<>() {
        @Override
        public PlayAnimationFunction deserialize(JsonElement element) {
            JsonBuf json = JsonBuf.of(element.getAsJsonObject());

            Key key = json.getKey("animation");
            Vector3i pos = json.get("position", Vector3i::of);

            return new PlayAnimationFunction(key, pos);
        }

        @Override
        public JsonObject serialize(PlayAnimationFunction value) {
            JsonBuf json = JsonBuf.empty();

            json.addKey("animation", value.getAnimation());
            json.add("position", value.getPosition());

            return json.getSource();
        }

        @Override
        public @NotNull Key key() {
            return KEY;
        }
    };

    private final Key animation;
    private final Vector3i position;

    public PlayAnimationFunction(Key animation, Vector3i position) {
        this.animation = animation;
        this.position = position;
    }

    public Vector3i getPosition() {
        return position;
    }

    public Key getAnimation() {
        return animation;
    }

    @Override
    public void execute(ActiveRaid raid) {
        BlockAnimation anim = Registries.ANIMATIONS.get(animation);

        if(anim == null) {
            ForTheCrown.logger().warning("Unknown animation from key: " + animation.asString());
            return;
        }

        anim.play(raid.getRegion().getWorld(), position);
    }

    @Override
    public SerializerType<PlayAnimationFunction> serializerType() {
        return TYPE;
    }
}
