package net.forthecrown.commands.click;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.FtcUtils;

public final class ClickableTexts {
    private ClickableTexts() {}

    public static final int RADIX = Character.MAX_RADIX;

    private static final ClickableTextNode ROOT_NODE = new ClickableTextNode("root");

    public static ClickableTextNode register(ClickableTextNode node) {
        ROOT_NODE.addNode(node);

        return node;
    }

    public static void unregister(ClickableTextNode node) {
        ROOT_NODE.removeNode(node);
    }

    public static void unregister(String name) {
        ROOT_NODE.removeNode(name);
    }

    public static Int2ObjectMap<ClickableTextNode> getNodes() {
        return ROOT_NODE.getNodes();
    }

    public static void execute(CrownUser user, String args) throws CommandSyntaxException {
        StringReader reader = new StringReader(FtcUtils.isNullOrBlank(args) ? "" : args);
        int initialID = Integer.valueOf(reader.readString(), RADIX);
        reader.skipWhitespace();

        ClickableTextNode node = ROOT_NODE.getNodes().get(initialID);
        if(node == null) {
            return;
        }

        node.execute(user, reader);
    }
}
