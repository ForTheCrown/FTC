/**
 * Usables are a system of objects that a user can interact with in some way.
 * <p>
 * I'll be honest, I have no idea how to properly describe the usables system, so
 * here goes.
 * <p>
 * Usables have to kinds of UsageTypes, checks and actions. Checks are run
 * before actions to test if a user can interact with a certain usable.
 * UsageTypes create a UsageInstance which holds the data that action/check
 * needs to run.
 * <p>
 * All usage types are stored in 2 registries: {@link net.forthecrown.registry.Registries#USAGE_ACTIONS} and
 * {@link net.forthecrown.registry.Registries#USAGE_CHECKS}. If a usage type is not
 * in one of those 2 registries, it cannot be serialized or deserialized.
 */
package net.forthecrown.useables;