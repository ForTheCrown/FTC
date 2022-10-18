package net.forthecrown.valhalla.data.triggers;

import net.forthecrown.serializer.SerializerType;
import org.bukkit.event.Event;

public interface TriggerInstanceType extends SerializerType<TriggerInstance<? extends Event>> {
}
