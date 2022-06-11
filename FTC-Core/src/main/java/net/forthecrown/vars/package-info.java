/**
 * Vars, aka, GlobalVars, ComVars, CommandVariables or whatever are a type of global variable
 * used by FTC. They are aimed at allowing you to use values that you might need to modify
 * later in-game or dynamically in another part of the code.
 * <p>
 * They work with the following structure:
 * <p>
 * All vars are held and managed by the {@link net.forthecrown.vars.VarRegistry} class. You can
 * define a variable using either {@link net.forthecrown.vars.VarRegistry#def(java.lang.String, net.forthecrown.vars.types.VarType, java.lang.Object)}
 * or {@link net.forthecrown.vars.Var#def(java.lang.String, net.forthecrown.vars.types.VarType, java.lang.Object)},
 * they do the same thing.
 * <p>
 * Each variable must have a {@link net.forthecrown.vars.types.VarType} which must be registered
 * in the {@link net.forthecrown.registry.Registries#VAR_TYPES} so it can be later deserialized.
 * All VarTypes should be stored as constants in the {@link net.forthecrown.vars.types.VarTypes}
 * class.
 */
package net.forthecrown.vars;