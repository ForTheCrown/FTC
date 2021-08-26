package net.forthecrown.core.chat;

import com.google.gson.JsonPrimitive;
import net.forthecrown.serializer.AbstractJsonSerializer;
import net.forthecrown.serializer.JsonBuf;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

public class JoinInfo extends AbstractJsonSerializer {

    private Component info;
    private boolean shouldShow = false;

    private final GsonComponentSerializer serializer = GsonComponentSerializer.gson();

    public JoinInfo(){
        super("join_info");

        info = Component.empty();
        reload();
    }

    public Component display(){
        return info;
    }

    public boolean shouldShow() {
        return shouldShow;
    }

    public void setShouldShow(boolean shouldShow) {
        this.shouldShow = shouldShow;
    }

    public void setDisplay(Component news) {
        this.info = news;
    }

    @Override
    protected void save(JsonBuf json) {
        json.add("shouldShow", new JsonPrimitive(shouldShow));
        json.add("info", serializer.serializeToTree(info));
    }

    @Override
    protected void reload(JsonBuf json) {
        this.shouldShow = json.get("shouldShow").getAsBoolean();
        this.info = serializer.deserializeFromTree(json.get("info"));
    }

    @Override
    protected void createDefaults(JsonBuf json) {
        save(json);
    }
}
