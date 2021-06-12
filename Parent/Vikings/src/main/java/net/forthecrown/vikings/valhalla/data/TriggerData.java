package net.forthecrown.vikings.valhalla.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.forthecrown.emperor.serializer.JsonSerializable;

import java.util.ArrayList;
import java.util.List;

public class TriggerData implements JsonSerializable {

    public final List<UnbuiltTrigger<?>> triggers = new ArrayList<>();

    public TriggerData(){

    }

    public TriggerData(JsonElement element){

    }

    @Override
    public JsonArray serialize() {
        JsonArray array = new JsonArray();
        triggers.forEach(a -> array.add(a.serialize()));
        return array;
    }
}
