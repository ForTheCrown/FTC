package net.forthecrown.user.property;

import com.google.gson.JsonElement;
import lombok.Getter;
import net.forthecrown.utils.io.JsonUtils;
import org.jetbrains.annotations.NotNull;

public class EnumProperty<E extends Enum<E>> extends UserProperty<E> {
    @Getter
    private final Class<E> type;

    public EnumProperty(@NotNull String name, @NotNull E defaultValue) {
        super(name, defaultValue);
        this.type = defaultValue.getDeclaringClass();
    }

    @Override
    public E deserialize(JsonElement element) {
        return JsonUtils.readEnum(type, element);
    }

    @Override
    public JsonElement serialize(E e) {
        return JsonUtils.writeEnum(e);
    }
}