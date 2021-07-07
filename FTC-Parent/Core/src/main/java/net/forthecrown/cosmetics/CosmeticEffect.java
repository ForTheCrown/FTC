package net.forthecrown.cosmetics;

import com.google.gson.JsonPrimitive;
import net.forthecrown.inventory.builder.options.InventoryOption;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.utils.JsonUtils;
import net.forthecrown.utils.Nameable;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;

public interface CosmeticEffect extends JsonSerializable, Nameable, Keyed, InventoryOption {
    Component[] getDescription();

    @Override
    default JsonPrimitive serialize(){
        return JsonUtils.writeKey(key());
    }
}
