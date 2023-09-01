package net.forthecrown.text.placeholder;

import net.forthecrown.utils.context.Context;
import net.kyori.adventure.audience.Audience;

/**
 * Placeholder rendering context
 * @param viewer Message viewer, may be {@code null}
 * @param list Placeholder list, can be used to render messages inside messages
 * @param context General-purpose context object
 */
public record PlaceholderContext(
    Audience viewer,
    PlaceholderRenderer renderer,
    Context context
) {

}
