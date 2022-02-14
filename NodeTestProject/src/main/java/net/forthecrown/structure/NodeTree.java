package net.forthecrown.structure;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class NodeTree<T extends StructureNode> {
    public static final Logger LOGGER = LogManager.getLogger();

    private Entry<T> start;

    public Entry<T> getStart() {
        return start;
    }

    public void setStart(Entry<T> start) {
        this.start = start;
    }

    public boolean gen(PlaceContext context) {
        return getStart().generate(context);
    }

    public static class Entry<T extends StructureNode> {
        private final T parent;
        private final List<Entry<T>> children = new ArrayList<>();

        public Entry(T parent) {
            this.parent = parent;
        }

        public T getParent() {
            return parent;
        }

        public List<Entry<T>> getChildren() {
            return children;
        }

        public void addChild(Entry<T> entry) {
            children.add(entry);
        }

        public void addChild(T node) {
            addChild(new Entry<>(node));
        }

        public boolean generate(PlaceContext context) {
            if (!parent.place(context, false)) {
                LOGGER.warn("Couldn't place parent node");
                return false;
            }

            for (Entry<T> e: getChildren()) {
                e.generate(context);
            }

            return true;
        }
    }
}
