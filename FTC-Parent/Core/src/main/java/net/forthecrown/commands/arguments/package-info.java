/**
 * Contains argument types for FTC types.
 * <p></p>
 * <p>
 * All of these types have a static instance variable to avoid creating more than one instance of the class
 * and a static method which returns that instance to go along with them
 * </p>
 * <p></p>
 * <p>
 * Some classes have a static get method to get the parse result as well. In the case of {@link net.forthecrown.commands.arguments.UserType#getUser(com.mojang.brigadier.context.CommandContext, java.lang.String)}
 * It is used for correctly detecting that the gotten user can be accessed by the command source,
 * aka if the user is vanished or something
 * </p>
 * <p></p>
 * <p>
 * Classes also contain exception types, like the UNKNOWN_AUCTION for the AuctionArgType
 * This is an ExceptionType which returns either a CommandSyntaxException or a RoyalCommandException, depending on
 * the exception type, basically just a way of stopping the command parsing and telling the sender
 * they screwed up somewhere.
 * </p>
 * <p></p>
 * <p>
 * A lot of classes also all return the {@link net.kyori.adventure.key.Key}, like the ActionArgType, CheckArgType,
 * ArrowEffectType and DeathEffectType. The key is then used to get them from a {@link net.forthecrown.registry.Registry}
 * where the key is the, well the key.
 * </p>
 * <p></p>
 * <p>
 * If you're ever working with a type that parses a key, I reccommend using the {@link net.forthecrown.commands.arguments.KeyType}
 * to do this. Saves a lot of hassel lol
 * </p>
 */
package net.forthecrown.commands.arguments;