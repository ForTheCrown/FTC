package net.forthecrown.core;

import com.google.gson.JsonElement;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.text.writer.TextWriter;
import net.forthecrown.text.writer.TextWriters;
import net.forthecrown.utils.io.JsonWrapper;
import net.kyori.adventure.text.Component;

/**
 * Config section that shows info to users when they
 * join the server.
 */
@Getter @Setter
public class JoinInfo extends FtcConfig.Section {

    /**
     * The standard info to show to players whenever
     * they join the server.
     */
    private Component info;

    /**
     * The info to show to players when they join
     * the server and the end is open.
     */
    private Component endInfo;

    /**
     * Determines whether the {@link #getInfo()} is
     * shown to players when they join.
     */
    private boolean visible;

    /**
     * Determines whether the {@link #getEndInfo()}
     * is shown to players when they join.
     */
    private boolean endVisible;

    public JoinInfo() {
        super("join_info");

        info = Component.empty();
        endInfo = Component.empty();
    }

    /**
     * Creates a display message with the current
     * join info.
     * <p>
     * Will contain both {@link #getInfo()} and
     * {@link #getEndInfo()} if their respective
     * visible toggles are set to true.
     *
     * @return The formatted join info.
     */
    public Component display() {
        TextWriter writer = TextWriters.newWriter();

        if (visible) {
            writer.line(info);
        }

        if (endVisible) {
            writer.line(endInfo);
        }

        return writer.asComponent();
    }

    @Override
    public void deserialize(JsonElement element) {
        JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

        this.info = json.getComponent("info");
        this.endInfo = json.getComponent("end_info");
        this.visible = json.getBool("should_show");
        this.endVisible = json.getBool("should_show_end");
    }

    @Override
    public JsonElement serialize() {
        JsonWrapper json = JsonWrapper.create();

        json.addComponent("info", info);
        json.addComponent("end_info", endInfo);
        json.add("should_show", visible);
        json.add("should_show_end", endVisible);

        return json.getSource();
    }
}