package net.forthecrown.waypoint.visit;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.api.GSitAPI;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.VanillaAccess;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

@Getter
@RequiredArgsConstructor
public class RidingNode {
    private final Entity entity;
    private final RidingNode[] passengers;

    public static RidingNode create(Entity root) {
        var rootNode = createNode(root);
        rootNode.dismount();
        return rootNode;
    }

    private static RidingNode createNode(Entity entity) {
        return new RidingNode(entity, createChildren(entity.getPassengers()));
    }

    private static RidingNode[] createChildren(List<Entity> entities) {
        if (entities == null || entities.isEmpty()) {
            return null;
        }

        RidingNode[] result = new RidingNode[entities.size()];

        for (int i = 0; i < entities.size(); i++) {
            var entity = entities.get(i);

            if (isSeat(entity)) {
                entity = entity.getPassengers().get(0);
            }

            result[i] = createNode(entity);
        }

        return result;
    }

    private static boolean isSeat(Entity entity) {
        var packageName = VanillaAccess.getEntity(entity)
                .getClass()
                .getPackageName();

        return packageName.contains(GSitMain.class.getPackageName());
    }

    public void forEach(Consumer<Entity> entityConsumer) {
        entityConsumer.accept(entity);

        if (ArrayUtils.isEmpty(passengers)) {
            return;
        }

        for (var p: passengers) {
            if (p == null) {
                continue;
            }

            p.forEach(entityConsumer);
        }
    }

    public void dismount() {
        forEach(Entity::leaveVehicle);
    }

    public void remount(@Nullable Entity seat) {
        if (seat != null) {
            _remount(seat);
        }

        if (ArrayUtils.isEmpty(passengers)) {
            return;
        }

        for (var p : passengers) {
            if (p == null) {
                continue;
            }

            p.entity.teleport(entity);
            Tasks.runLater(() -> p.remount(entity), 1);
        }
    }

    void _remount(@NotNull Entity seat) {
        if (seat instanceof Player player
                && entity instanceof Player passenger
        ) {
            GSitAPI.sitOnPlayer(passenger, player);
            return;
        }

        seat.addPassenger(entity);
    }
}