package net.forthecrown.commands.click;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import lombok.Getter;
import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang3.Validate;

public class ClickableTextNode {
    @Getter
    private final String name;

    private final String hashedName;
    private final long hashedNameLong;
    private final Long2ObjectMap<ClickableTextNode> nameHash2Node = new Long2ObjectOpenHashMap<>();
    private ClickableTextNode parent;

    @Getter
    private PromptCreator promptCreator;
    @Getter
    private TextExecutor executor;

    public ClickableTextNode(String name) {
        this.name = name;
        this.hashedNameLong = ClickableTexts.toCodedHash(name.hashCode());
        this.hashedName = Long.toString(hashedNameLong, ClickableTexts.RADIX);
    }

    public ClickableTextNode setPrompt(PromptCreator promptCreator) {
        this.promptCreator = promptCreator;
        return this;
    }


    public ClickableTextNode setExecutor(TextExecutor executor) {
        this.executor = executor;
        return this;
    }

    public ClickableTextNode addNode(ClickableTextNode node) {
        Validate.isTrue(node != this, "Bruh");

        nameHash2Node.put(node.hashedNameLong, node);
        node.parent = this;
        return this;
    }

    public void removeNode(ClickableTextNode node) {
        removeNode(node.getName());
    }

    public void removeNode(String name) {
        Validate.notNull(name, "Name was null");
        nameHash2Node.remove(ClickableTexts.toCodedHash(name.hashCode()));
    }

    public Long2ObjectMap<ClickableTextNode> getNodes() {
        return nameHash2Node;
    }

    public void execute(User user, StringReader reader) throws CommandSyntaxException {
        if(reader.canRead()) {
            if(reader.peek() == ' ') reader.skipWhitespace();

            long hash = Long.valueOf(reader.readString(), ClickableTexts.RADIX);

            ClickableTextNode node = getNodes().get(hash);
            if(node != null) node.execute(user, reader);

            return;
        }

        if(getExecutor() != null) {
            getExecutor().execute(user);
        }
    }

    public Component presentPrompts(User user) {
        TextComponent.Builder builder = Component.text();

        for (ClickableTextNode n: getNodes().values()) {
            Component prompt = n.prompt(user);

            if(prompt != null) {
                builder
                        .append(Component.newline())
                        .append(prompt);
            }
        }

        return builder.build();
    }

    public Component prompt(User user) {
        Component prompt = promptCreator == null ? null : promptCreator.prompt(user);
        if(prompt == null) return null;

        return prompt.clickEvent(getClickEvent());
    }

    public String getCommand() {
        boolean root = name.equals("root");
        String parentCmd = parent == null || root ? ("/" + CommandClickableText.NAME) : parent.getCommand();

        return root ? parentCmd : parentCmd + " " + hashedName;
    }

    public ClickEvent getClickEvent() {
        return ClickEvent.runCommand(getCommand());
    }

    public ClickableTextNode getNode(String id) {
        return nameHash2Node.get(ClickableTexts.toCodedHash(id.hashCode()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ClickableTextNode node = (ClickableTextNode) o;

        return new EqualsBuilder()
                .append(getName(), node.getName())
                .append(nameHash2Node, node.nameHash2Node)
                .append(parent, node.parent)
                .append(getPromptCreator(), node.getPromptCreator())
                .append(getExecutor(), node.getExecutor())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getName())
                .append(nameHash2Node)
                .append(parent)
                .append(getPromptCreator())
                .append(getExecutor())
                .toHashCode();
    }
}