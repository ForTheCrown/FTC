package net.forthecrown.core.crownevents;
/*
 * Some basic API stuff for hosting CrownEvents
 *
 * InEventListener
 * An InEventListener is just a concept, it's a listener class that gets created and registered when a player enters an
 * event and gets unregistered and destroyed when they leave. Aka it's a listener that's only active when it needs to be
 *
 * Entries:
 * Entries are essentially classes that store data for players or teams of players in the event
 * They all use the same abstract EventEntry class as a parent class, and it's easy to create your own.
 * The EasterEvent or AprilEvent event had it's own entry, an EasterEntry
 *
 * CrownEvent
 * A CrownEvent is an interface with 3 methods, start(Player player), end(EventEntry entry) and complete(EventEntry entry)
 * start starts it, and should create an entry for the player.
 * End and Complete are different in the sense that, if you do the event successfully, you complete it, but if you fail it
 * or just don't succeed, then you end the event
 * I usually make the complete method call the end method, since they can have overlapping functions
 *
 */