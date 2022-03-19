/**
 * Everything there is to know about Vars.
 * A var is a type of global variable that can be easily mapped to any type with a {@link net.forthecrown.vars.types.VarType}
 * instance, It's also integrated with the /var command which means that any var can be changed on the fly by a command.
 * <p></p>
 * Vars are automatically serialized so there's no need to worry about data or changes being lost, unless the var is removed
 * from the com_vars json file itself.
 */
package net.forthecrown.vars;