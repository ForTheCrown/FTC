package net.forthecrown.commands.click;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.forthecrown.user.User;
import net.forthecrown.utils.Util;

public final class ClickableTexts {
    private ClickableTexts() {}

    public static final int RADIX = Character.MAX_RADIX;
    public static final int RANDOM_KEY = Util.RANDOM.nextInt(1000000);

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

    public static Long2ObjectMap<ClickableTextNode> getNodes() {
        return ROOT_NODE.getNodes();
    }

    public static void execute(User user, String args) throws CommandSyntaxException {
        StringReader reader = new StringReader(Util.isNullOrBlank(args) ? "" : args);
        long initialID = Long.valueOf(reader.readString(), RADIX);
        reader.skipWhitespace();

        ClickableTextNode node = ROOT_NODE.getNodes().get(initialID);
        if(node == null) {
            return;
        }

        node.execute(user, reader);
    }

    // Takes the given hash and multiplies it with
    // the given RANDOM_KEY of the current session
    static long toCodedHash(int hash) {
        return (long) hash * RANDOM_KEY;
    }
}