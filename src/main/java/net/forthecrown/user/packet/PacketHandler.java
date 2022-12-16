package net.forthecrown.user.packet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Functions that intend to listen to packets
 * being sent or received by the server must
 * be annotated with this annotation
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PacketHandler {
    /**
     * The listener's priority.
     * <p>
     * Smaller value means being
     * executed earlier
     * @return The listener's priority
     */
    int priority() default 0;

    /**
     * Whether to ignore packets that have
     * already been stopped from being
     * written/read from the network
     * @return Whether this listener is ignoring already cancelled packets
     */
    boolean ignoreCancelled() default false;
}