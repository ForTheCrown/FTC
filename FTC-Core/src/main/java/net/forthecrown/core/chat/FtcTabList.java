package net.forthecrown.core.chat;

import net.forthecrown.core.Crown;
import net.forthecrown.serializer.AbstractJsonSerializer;
import net.forthecrown.serializer.JsonWrapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FtcTabList extends AbstractJsonSerializer implements TabList {

    private Component score;

    public FtcTabList(){
        super("tab_list");

        reload();
        Crown.logger().info("Tab list loaded");
    }

    @Override
    protected void save(JsonWrapper json) {
        json.add("score", ChatUtils.toJson(score));
    }

    @Override
    protected void reload(JsonWrapper json) {
        setScore(ChatUtils.fromJson(json.get("score")));
    }

    @Override
    protected void createDefaults(JsonWrapper json) {
        this.score = Component.text("Deaths").color(NamedTextColor.GRAY);
        save(json);
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

        for (Player p: Bukkit.getOnlinePlayers()){
            p.sendPlayerListHeader(formatted);
        }
    }
}
