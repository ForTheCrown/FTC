/**
 * A simple lite system to allow us to
 * make an NPC intractable without having to write
 * another event listener for it.
 * <p>
 * Simply register your instance of {@link net.forthecrown.core.npc.InteractableNPC} into
 * {@link net.forthecrown.registry.Registries#NPCS} and then apply the key you gave it
 * to an entity with '/npc ENTITY_SELECTOR NPC_KEY'
 * <p>
 * {@link net.forthecrown.core.npc.NpcDirectory} manages the interaction part, the aforementioned
 * functional interface is where your logic gets ran.
 */
package net.forthecrown.core.npc;