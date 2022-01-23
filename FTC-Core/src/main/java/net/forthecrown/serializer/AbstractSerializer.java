package net.forthecrown.serializer;

import net.forthecrown.core.Crown;
import org.apache.logging.log4j.Logger;

public abstract class AbstractSerializer implements CrownSerializer {
    protected static final Logger LOGGER = Crown.logger();
}
