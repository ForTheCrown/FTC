/**
 * A ComVar or CommandVariable is a global variable which can be easily modified and accessed ingame
 * Currently, all serialization of ComVars has to be done by the plugins that create the ComVars
 * Maybe this should change :shrug: idk lol
 *
 * You can create them with the ComVars class's method set(String name, ComVarType<T> type, T value){
 * Where T can be any type that you've specified a ComVarType for
 */
package net.forthecrown.comvars;