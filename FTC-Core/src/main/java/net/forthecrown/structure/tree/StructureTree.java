package net.forthecrown.structure.tree;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.core.Crown;
import net.forthecrown.registry.Registries;
import net.forthecrown.structure.PlaceRotation;
import net.forthecrown.structure.StructureTransform;
import net.forthecrown.utils.math.Vector3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.apache.logging.log4j.Logger;
import org.bukkit.World;

import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;

public class StructureTree<T extends StructureNode> {
    private static final Logger LOGGER = Crown.logger();

    private StructureType<T> type;
    private Entry<T> start;

    public StructureTree() {
    }

    public StructureTree(CompoundTag tag) {
        type = Registries.STRUCTURE_TYPES.read(tag.get("type"));

        setStart(new Entry<>(tag.getCompound("tree")));
    }

    public Entry<T> getStart() {
        return start;
    }

    public void setStart(Entry<T> start) {
        this.start = start;
        this.type = (StructureType<T>) start.parent.getType().getStructureType();
    }

    public void place(World world, Vector3i start, StructureTransform transform, PlaceRotation rotation) {
        place(new NodePlaceContext(world, transform, start, rotation));
    }

    public void place(NodePlaceContext context) {
        start.generate(context, true);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", type.key().asString());

        tag.put("tree", start.save());

        return tag;
    }

    public int size() {
        return start.countSize();
    }

    public void forEachEntry(Consumer<Entry<T>> consumer) {
        start.accept(consumer);
    }

    public static class Entry<T extends StructureNode> {
        private final T parent;
        private final List<Entry<T>> children = new ObjectArrayList<>();

        public Entry(T parent) {
            this.parent = parent;
        }

        public Entry(CompoundTag tag) {
            StructureNodeType<T> nodeType = Registries.STRUCTURE_NODE_TYPES.read(tag.get("parent_type"));
            parent = nodeType.load(tag.getCompound("parent"));

            if(tag.contains("children")) {
                ListTag list = tag.getList("children", Tag.TAG_COMPOUND);

                for (Tag t: list) {
                    addChild(new Entry<>((CompoundTag) t));
                }
            }
        }

        public T getParent() {
            return parent;
        }

        public List<Entry<T>> getChildren() {
            return children;
        }

        public void addChild(T child) {
            addChild(new Entry<>(child));
        }

        public void addChild(Entry<T> entry) {
            children.add(entry);
        }

        public boolean generate(NodePlaceContext context, boolean genChildren) {
            if(!parent.place(context, false)) {
                // If we can't place
                /*parent.getType()
                        .getStructureType()
                        .getEndNodeType()
                        .createEmpty()
                        .place(context, true);*/

                LOGGER.info("Couldn't place parent {}", parent.getType().key().asString());
                return false;
            }

            if(!genChildren) return true;

            ListIterator<Entry<T>> iterator = children.listIterator();

            while (iterator.hasNext()) {
                Entry<T> e = iterator.next();

                if (e.generate(context.copy(), true)) continue;

                LOGGER.info("Couldn't place child {}", e.parent.getType().key().asString());
                iterator.remove();
                T node = (T) parent.getType().getStructureType().getEndType().createEmpty();
                node.place(context.copy(), false);

                iterator.add(new Entry<>(node));
            }

            return true;
        }

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();

            CompoundTag parentTag = new CompoundTag();
            parent.save(parentTag);

            tag.put("parent", parentTag);
            tag.putString("parent_type", parent.getType().key().asString());

            if(!children.isEmpty()) {
                ListTag childTag = new ListTag();

                for (Entry<T> child: children) {
                    childTag.add(child.save());
                }

                tag.put("children", childTag);
            }

            return tag;
        }

        public void accept(Consumer<Entry<T>> consumer) {
            consumer.accept(this);

            for (Entry<T> e: getChildren()) {
                e.accept(consumer);
            }
        }

        public int countSize() {
            int result = 1;

            for (Entry e: getChildren()) {
                result += e.countSize();
            }

            return result;
        }
    }
}
