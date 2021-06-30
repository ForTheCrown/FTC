package net.forthecrown.core.chat;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.forthecrown.serializer.AbstractJsonSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

public class JoinInfo extends AbstractJsonSerializer {

    private Component news;
    private boolean shouldShow = false;

    private final GsonComponentSerializer serializer = GsonComponentSerializer.gson();

    public JoinInfo(){
        super("join_info");

        news = Component.empty();
        reload();
    }

    public Component display(){
        return news;
    }

    public boolean shouldShow() {
        return shouldShow;
    }

    public void setShouldShow(boolean shouldShow) {
        this.shouldShow = shouldShow;
    }

    public void setDisplay(Component news) {
        this.news = news;
    }

    @Override
    protected void save(JsonObject json) {
        json.add("shouldShow", new JsonPrimitive(shouldShow));
        json.add("news", serializer.serializeToTree(news));
    }

    @Override
    protected void reload(JsonObject json) {
        this.shouldShow = json.get("shouldShow").getAsBoolean();
        this.news = serializer.deserializeFromTree(json.get("news"));
    }

    @Override
    protected JsonObject createDefaults(JsonObject json) {
        save(json);
        return json;
    }
}
