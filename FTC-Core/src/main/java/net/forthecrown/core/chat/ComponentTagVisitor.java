package net.forthecrown.core.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.nbt.*;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;

import static net.kyori.adventure.text.Component.text;

/**
 * Thing that visits NBT tags and makes them into readable components.
 * <p>Allows formatting with new lines and indentation, makes the text look kind of like JSON</p>
 */
public class ComponentTagVisitor implements TagVisitor {
    private final int indentLevel;
    private final boolean format;
    private final TextComponent.Builder builder;

    private int currentIndent;

    public ComponentTagVisitor(int indentLevel, boolean format) {
        this.indentLevel = indentLevel;
        this.format = format;

        this.builder = text();
    }

    public ComponentTagVisitor(boolean format) {
        this(2, format);
    }

    public TextComponent visit(Tag tag) { return visit(tag, null); }
    public TextComponent visit(Tag tag, @Nullable Component prefix) {
        if(prefix != null) builder.append(prefix);

        tag.accept(this);
        return builder.build();
    }

    private void newLine() {
        if(format) builder.append(Component.newline());
    }

    private void indent() {
        if(currentIndent < 1) return;
        if(format) builder.append(text(" ".repeat(currentIndent)));
    }

    private void addComma() {
        builder.append(text(", "));
    }

    private void newLineIndent(boolean removeIndent) {
        if(!format) return;

        if(removeIndent) removeIndent();
        builder.append(Component.text("\n" + " ".repeat(currentIndent)));
    }

    private void visitArray(CollectionTag<? extends Tag> arr, Character prefix, boolean allowOneLine) {
        builder.append(text("["));

        boolean multiLine = !allowOneLine && arr.size() > 3;

        if(!arr.isEmpty()) {
            if(prefix != null) {
                builder
                        .append(text(prefix).color(NamedTextColor.RED))
                        .append(text(": "));
            }

            if(multiLine) {
                addIndent();
                newLineIndent(false);
            }

            Iterator<? extends Tag> iterator = arr.iterator();

            while (iterator.hasNext()) {
                Tag tag = iterator.next();

                tag.accept(this);

                if(iterator.hasNext()) {
                    addComma();
                    if(multiLine) newLineIndent(false);
                }
                else if(multiLine) newLineIndent(true);
            }
        }

        builder.append(text("]"));
    }

    private void addIndent() { if(format) this.currentIndent += indentLevel; }
    private void removeIndent() { if(format) this.currentIndent -= indentLevel; }

    private void addNumber(NumericTag tag, Character suffix) {
        builder
                .append(text(tag.getAsNumber().toString()).color(NamedTextColor.GOLD))
                .append(text(suffix == null ? "" : suffix.toString()).color(NamedTextColor.RED));
    }

    @Override
    public void visitString(StringTag element) {
        Tag tag = element;

        builder
                .append(text('"'))
                .append(text(tag.getAsString()).color(NamedTextColor.GREEN))
                .append(text('"'));
    }

    @Override
    public void visitByte(ByteTag element) {
        addNumber(element, 'b');
    }

    @Override
    public void visitShort(ShortTag element) {
        addNumber(element, 's');
    }

    @Override
    public void visitInt(IntTag element) {
        addNumber(element, null);
    }

    @Override
    public void visitLong(LongTag element) {
        addNumber(element, 'L');
    }

    @Override
    public void visitFloat(FloatTag element) {
        addNumber(element, 'f');
    }

    @Override
    public void visitDouble(DoubleTag element) {
        addNumber(element, 'D');
    }

    @Override
    public void visitByteArray(ByteArrayTag element) {
        visitArray(element, 'b', true);
    }

    @Override
    public void visitIntArray(IntArrayTag element) {
        visitArray(element, 'I', true);
    }

    @Override
    public void visitLongArray(LongArrayTag element) {
        visitArray(element, 'L', true);
    }

    @Override
    public void visitList(ListTag element) {
        int id = ((Tag) element).getId();

        visitArray(element, null,
            id == Tag.TAG_DOUBLE
                        || id == Tag.TAG_FLOAT
                        || id == Tag.TAG_SHORT
        );
    }

    @Override
    public void visitCompound(CompoundTag compound) {
        builder.append(text('{'));

        if(!compound.isEmpty()) {
            addIndent();
            newLineIndent(false);

            Iterator<Map.Entry<String, Tag>> iterator = compound.tags.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<String, Tag> e = iterator.next();

                builder
                        .append(text(e.getKey()).color(NamedTextColor.AQUA))
                        .append(text(": "));

                e.getValue().accept(this);

                if(iterator.hasNext()){
                    addComma();
                    newLineIndent(false);
                } else newLineIndent(true);
            }
        }

        builder.append(text('}'));
    }

    @Override
    public void visitEnd(EndTag element) {
        builder.append(text('}'));
    }
}
