package net.forthecrown.core.chat;

import com.google.gson.JsonElement;
import net.forthecrown.core.Crown;
import net.forthecrown.core.FtcConfig;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

public class FtcTabList extends FtcConfig.ConfigSection implements TabList {

    private Component score;

    public FtcTabList(){
        super("tab_list");

        Crown.logger().info("Tab list loaded");
    }

    @Override
    public Component format() {
        return Component.text()
                .append(TabList.SERVER_TITLE)
                .append(Component.newline())
                .append(Component.text()
                        .color(NamedTextColor.GRAY)
                        .append(TabList.SCORE_FIELD)
                        .append(score)
                        .build()
                )
                .build();
    }

    @Override
    public void setScore(@NotNull Component score) {
        Validate.notNull(score);
        this.score = score;
    }

    @Override
    public Component currentScore() {
        return score;
    }

    @Override
    public void updateList() {
        Component formatted = format();

        for (CrownUser u: UserManager.getOnlineUsers()) {
            u.sendPlayerListHeader(formatted);
        }
    }

    @Override
    public void deserialize(JsonElement element) {
        this.score = ChatUtils.fromJson(element);
    }

    @Override
    public JsonElement serialize() {
        return ChatUtils.toJson(score);
    }
}
