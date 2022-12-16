package net.forthecrown.core.script2;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import jdk.dynalink.beans.StaticClass;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.FTC;
import net.forthecrown.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.event.*;
import org.bukkit.plugin.EventExecutor;
import org.jetbrains.annotations.NotNull;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;
import org.openjdk.nashorn.internal.runtime.Context;

import java.util.List;
import java.util.Objects;

@Getter
@RequiredArgsConstructor
public class ScriptEvents {
    private final Script script;
    private final List<ExecutorWrapper> wrappers = new ObjectArrayList<>();

    /* ---------------------------- REGISTRATION ---------------------------- */

    public void register(String function,
                         Object eventClass
    ) {
        register(function, eventClass, EventPriority.NORMAL);
    }

    public void register(String function,
                         Object eventClass,
                         EventPriority priority
    ) {
        register(function, eventClass, priority, false);
    }

    public void register(String function,
                         Object eventClass,
                         EventPriority priority,
                         boolean ignoreCancelled
    ) {
        register(null, function, eventClass, priority, ignoreCancelled);
    }

    public void register(Object listener,
                         String function,
                         Object eventClass
    ) {
        register(listener, function, eventClass, EventPriority.NORMAL);
    }

    public void register(Object listener,
                         String function,
                         Object eventClass,
                         EventPriority priority
    ) {
        register(listener, function, eventClass, priority, false);
    }

    public void register(Object listener,
                         String function,
                         Object eventClass,
                         EventPriority priority,
                         boolean ignoreCancelled
    ) {
        if (listener == null) {
            listener = script.getMirror();
        }

        var rawMirror = ScriptObjectMirror.wrap(
                listener, Context.getGlobal()
        );

        if (!(rawMirror instanceof ScriptObjectMirror m)) {
            throw Util.newException("Invalid listener: %s", listener);
        }

        var eventType = getEventClass(eventClass);

        ScriptListenerHandle handle = new ScriptListenerHandle(m, function);
        ExecutorWrapper wrapper = new ExecutorWrapper(handle, eventType, this);
        wrappers.add(wrapper);

        Bukkit.getPluginManager()
                .registerEvent(
                        eventType,
                        wrapper,
                        priority,
                        wrapper,
                        FTC.getPlugin()
                );
    }

    /* --------------------------- UNREGISTRATION --------------------------- */

    public void unregister(String functionName) {
        Objects.requireNonNull(functionName);

        wrappers.removeIf(wrapper -> {
            if (wrapper.handle.member.equalsIgnoreCase(functionName)) {
                HandlerList.unregisterAll(wrapper);
                return true;
            }

            return false;
        });
    }

    public void unregisterFrom(Object eventClass) {
        var clazz = getEventClass(eventClass);

        wrappers.removeIf(wrapper -> {
            if (wrapper.type == clazz) {
                HandlerList.unregisterAll(wrapper);
                return true;
            }

            return false;
        });
    }

    private Class<? extends Event> getEventClass(Object input) {
        Objects.requireNonNull(input, "Event class is null");

        if (input instanceof String s) {
            try {
                input = Class.forName(
                        s, true, getClass().getClassLoader()
                );
            } catch (ReflectiveOperationException exc) {
                throw new IllegalStateException(exc);
            }
        } else if (input instanceof StaticClass staticClass) {
            input = staticClass.getRepresentedClass();
        }

        Class type = (Class) input;

        if (!Event.class.isAssignableFrom(type)) {
            throw Util.newException("Class %s is not an event class!",
                    type.getName()
            );
        }

        return type;
    }

    void close() {
        wrappers.forEach(HandlerList::unregisterAll);
        wrappers.clear();
    }

    @Getter
    @RequiredArgsConstructor
    static class ExecutorWrapper implements EventExecutor, Listener {
        private final ScriptListenerHandle handle;
        private final Class<? extends Event> type;
        private final ScriptEvents events;

        @Override
        public void execute(@NotNull Listener listener, @NotNull Event event)
                throws EventException
        {
            if (listener instanceof Cancellable c
                    && c.isCancelled()
            ) {
                return;
            }

            if (!type.isAssignableFrom(event.getClass())) {
                return;
            }

            handle.invoke(events.getScript(), event);
        }
    }

    public record ScriptListenerHandle(ScriptObjectMirror scriptObject,
                                       String member
    ) {
        public void invoke(Script script, Event event) {
            Script.invokeSafe(script, scriptObject, member, event);
        }
    }
}