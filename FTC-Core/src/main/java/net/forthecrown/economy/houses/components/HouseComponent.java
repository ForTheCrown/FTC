package net.forthecrown.economy.houses.components;

import net.forthecrown.core.DayChangeListener;
import net.forthecrown.serializer.JsonDeserializable;
import net.forthecrown.serializer.JsonSerializable;
import net.kyori.adventure.key.Keyed;

public interface HouseComponent extends JsonSerializable, JsonDeserializable, Keyed, DayChangeListener {
}
