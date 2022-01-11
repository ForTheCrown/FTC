package net.forthecrown.core.chat;

import com.google.gson.JsonElement;
import net.forthecrown.core.FtcConfig;
import net.forthecrown.serializer.JsonWrapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

public class JoinInfo extends FtcConfig.ConfigSection {
    private Component info;
    private Component endInfo;
    private boolean shouldShow, shouldShowEnd;

    public JoinInfo(){
        super("join_info");

        info = Component.empty();
    }

    public Component display() {
        TextComponent.Builder builder = Component.text();

        boolean normalShowed = false;
        if(shouldShow) {
            normalShowed = true;
            builder.append(info);
        }

        if(shouldShowEnd) {
            if(normalShowed) builder.append(Component.newline());
            builder.append(endInfo);
        }

        return builder.build();
    }

    public Component getEndInfo() {
        return endInfo;
    }

    public Component getInfo() {
        return info;
    }

    public void setEndInfo(Component endInfo) {
        this.endInfo = endInfo;
    }

    public void setInfo(Component info) {
        this.info = info;
    }

    public boolean shouldShow() {
        return shouldShow;
    }

    public void setShouldShow(boolean shouldShow) {
        this.shouldShow = shouldShow;
    }

    public void setShouldShowEnd(boolean shouldShowEnd) {
        this.shouldShowEnd = shouldShowEnd;
    }

    public boolean shouldShowEnd() {
        return shouldShowEnd;
    }

    @Override
    public void deserialize(JsonElement element) {
        JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());

        this.info = json.getComponent("info");
        this.endInfo = json.getComponent("end_info");
        this.shouldShow = json.getBool("should_show");
        this.shouldShowEnd = json.getBool("should_show_end");
    }

    @Override
    public JsonElement serialize() {
        JsonWrapper json = JsonWrapper.empty();

        json.addComponent("info", info);
        json.addComponent("end_info", endInfo);
        json.add("should_show", shouldShow);
        json.add("should_show_end", shouldShowEnd);

        return json.getSource();
    }
}
