package net.forthecrown.text.format;

import net.forthecrown.text.Text;
import net.forthecrown.utils.Util;
import net.kyori.adventure.text.Component;

import java.util.concurrent.TimeUnit;

/**
 * Utility class for formatting specific
 * units like Rhines, Gems and Votes into
 * messages.
 */
public interface UnitFormat {
    /* ----------------------------- CONSTANTS ------------------------------ */

    /** The unit of a single rhine, the server's currency */
    String UNIT_RHINE = "Rhine";

    /** The unit of a single gem, the server's cosmetic currency */
    String UNIT_GEM = "Gem";

    /** Single vote unit */
    String UNIT_VOTE = "Vote";

    /** Single hour unit */
    String UNIT_HOUR = "Hour";

    /** Single coin unit */
    String UNIT_COIN = "Coin";

    /* ----------------------------- UTILITY METHODS ------------------------------ */

    /**
     * Formats the given amount into a rhine message
     * @param amount The amount to format
     * @return A formatted component
     */
    static Component rhines(Number amount) {
        return unit(amount, UNIT_RHINE);
    }

    /**
     * Formats the given amount into a
     * vote message
     * @param number The amount of votes
     * @return The formatted message
     */
    static Component votes(Number number) {
        return unit(number, UNIT_VOTE);
    }

    /**
     * Formats the given amount of playtime, in seconds,
     * and returns a formatted message with playtime
     * measured in hours.
     * @param number The amount of playtime seconds
     * @return The formatted message
     */
    static Component playTime(Number number) {
        return unit(TimeUnit.SECONDS.toHours(number.longValue()), UNIT_HOUR);
    }

    /**
     * Formats a gem message.
     * @param amount The amount to format and decimalize
     * @return The formatted amount.
     */
    static Component gems(Number amount) {
        return unit(amount, UNIT_GEM);
    }

    /**
     *
     * @param amount
     * @return
     */
    static Component coins(Number amount) {
        return unit(amount, UNIT_COIN);
    }

    /**
     * Creates a unit message
     * <p>
     * If there's more than 1 of the given
     * amount, the returned message will have
     * an 's' appended to it.
     *
     * @param amount The amount of units
     * @param unit The unit to format, singular
     * @return The formatted message.
     */
    static Component unit(Number amount, String unit) {
        return Component.text(
                Text.NUMBER_FORMAT.format(amount) + " " + unit
                + Util.conditionalPlural(amount.longValue())
        );
    }
}