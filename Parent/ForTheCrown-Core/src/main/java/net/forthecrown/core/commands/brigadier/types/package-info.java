package net.forthecrown.core.commands.brigadier.types;
/*
 * Wrappers for NMS argument types
 * Except for UserType and TypeCreator lol, those are mine
 *
 * Custom ones can be created and registered, but they'll either always appear red, aka, as invalid arguments in-game
 * ... or they'll disconnect players as they join due to argument registry mismatch between server and client
 */