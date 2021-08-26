/**
 * Contains items relating to serialization of things.
 * <p></p>
 * Abstract serializers are classes one class can extend to easily serialize items.
 * Granted, the 3 separate classes could easily extend a generic AbstractSerializer class, as they all
 * have a "load()" method to create the file and load it, so a generic super class could mean less
 * rewriting of that.
 */
package net.forthecrown.serializer;