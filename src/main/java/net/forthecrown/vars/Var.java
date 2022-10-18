package net.forthecrown.vars;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for marking a field as a global variable
 * field. This annotation is also applicable to classes, see
 * below for functionality
 * <p>
 * First off, a global variable is a variable that is accessible
 * easily in both code and in-game. This is done by mapping each
 * variable to a data wrapper with a {@link net.forthecrown.vars.types.VarType}
 * which allows for serialization and parsing through commands,
 * meaning code behaviour can be altered through staff input in
 * commands.
 * As well, this system provides a way to make these changes last
 * by serializing them into JSON and applying them to the var's
 * field.
 * <p>
 * Also of note, a {@link VarData} object acts only as a wrapper
 * and handle for the var's field, it contains no actual data of
 * the var's value, that is held by the field itself.
 * <h2>Usability</h2>
 * This annotation can be applied in 1 of two places, either onto
 * a field or a class. If this is applied onto a class, then the
 * annotation is not allowed to specify a {@link #type()} value nor
 * a {@link #callback()} value.
 * <p>
 * If this annotation is applied to a field, then all values this
 * annotation holds are allowed to be specified.
 * <p>
 * If you wish to make a var transient, aka, making it so it won't
 * be saved to the comvar JSON file, then all you must do is give
 * the field the transient keyword
 *
 * @see #callback()
 * @see #type()
 * @see #namePrefix()
 * @see net.forthecrown.vars.types.VarTypes
 * @see net.forthecrown.vars.types.VarType
 * @see VarRegistry
 * @see VarFactory
 */
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Var {

    /**
     * The manually specified type of the variable.
     * <p>
     * If this value is not set then the var system will
     * attempt to find the type based off of the field's
     * type, if that fails, it throws an error.
     * <p>
     * To find what strings are valid for this type value,
     * see {@link net.forthecrown.vars.types.VarTypes} to
     * see the names of all the types
     *
     * @return The variable's manually specified type, if empty,
     *         the vars type will be automatically inferred.
     * @see net.forthecrown.vars.types.VarTypes
     */
    String type() default "";

    /**
     * Specifies the method name of the value update
     * callback of this var.
     * <p>
     * If left empty, no callback will be assigned.
     * <p>
     * There are two valid method signatures for this
     * callback value, one is a void callback with no
     * parameters and the others is a void callback
     * with the value of this var being the only parameter.
     * In both cases, the callback must be a static void
     * method in the same class as the var itself
     *
     * @return The callback function's name, if empty, no
     *         callback function is assigned
     */
    String callback() default "";

    /**
     * The prefix to prepend onto the var's name
     * <p>
     * Note: If a class is annotated with this annotation
     * and the prefix is given there, all var's within that
     * class are applied the given prefix, if a method is
     * then given a prefix as well, it will prepend the class'
     * prefix and then the field's prefix onto it, in that
     * order.
     * <p>
     * Also be aware, the prefix is prepended literally, no
     * connecting "_" character or anything is added with it
     *
     * @return The vars name prefix, blank if no prefix is issued
     */
    String namePrefix() default "";
}