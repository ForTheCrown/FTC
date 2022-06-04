package net.forthecrown.core.chat;

import net.forthecrown.utils.ItemLoreBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public interface ComponentWriter extends Supplier<Component>, ComponentLike {
    void write(Component text);
    void clear();

    default void newLine() {
        write(Component.newline());
    }

    default void space() {
        write(Component.space());
    }

    default void empty() {
        write(Component.empty());
    }

    default void write(String s) {
        write(Component.text(s));
    }

    @Override
    Component get();

    default String getString() {
        return ChatUtils.getString(get());
    }

    @Override
    @NotNull
    default Component asComponent() {
        return get();
    }

    default ComponentWriter prefixedWriter(Component prefix) {
        return new ComponentWriter() {
            @Override
            public void write(Component text) {
                ComponentWriter.this.write(text);
            }

            @Override
            public void newLine() {
                ComponentWriter.this.newLine();
                ComponentWriter.this.write(prefix);
            }

            @Override
            public void clear() {
                ComponentWriter.this.clear();
            }

            @Override
            public Component get() {
                return ComponentWriter.this.get();
            }
        };
    }

    static ComponentWriter normal() {
        return new ComponentWriter() {
            private TextComponent.Builder builder = Component.text();

            @Override
            public void write(Component text) {
                builder.append(text);
            }

            @Override
            public void clear() {
                builder = Component.text();
            }

            @Override
            public Component get() {
                return builder.build();
            }
        };
    }

    static LoreWriter loreWriter(ItemLoreBuilder builder) {
        return new LoreWriter(builder);
    }

    class LoreWriter implements ComponentWriter {
        private ItemLoreBuilder builder;
        private TextComponent.Builder allText = Component.text();
        private TextComponent.Builder currentLine = Component.text();
        private boolean lineAdded;

        public LoreWriter(ItemLoreBuilder builder) {
            this.builder = builder;
        }

        public LoreWriter() {
            this(new ItemLoreBuilder());
        }

        @Override
        public void write(Component text) {
            TextDecoration.State state = text.decoration(TextDecoration.ITALIC);

            if(state == TextDecoration.State.NOT_SET) {
                text = text.decoration(TextDecoration.ITALIC, false);
            }

            text = text.colorIfAbsent(NamedTextColor.WHITE);

            currentLine.append(text);
            allText.append(text);
            lineAdded = false;
        }

        @Override
        public void newLine() {
            builder.add(currentLine.build());
            allText.append(Component.newline());

            currentLine = Component.text();
            lineAdded = true;
        }

        @Override
        public void clear() {
            builder = new ItemLoreBuilder();
            allText = Component.text();
            currentLine = Component.text();
            lineAdded = true;
        }

        @Override
        public Component get() {
            if(!lineAdded) {
                newLine();
            }

            return allText.build();
        }

        public ItemLoreBuilder getBuilder() {
            return builder;
        }
    }
}