/**
 * The classes here are old and should be rewritten.
 * <p>That's it</p>
 * <p></p>
 * <p>
 * Yeah alright. This package contains stuff to make clickable text more powerful by using the /npcconverse command
 * </p>
 * <p></p>
 * <p>
 * It uses a {@link net.forthecrown.commands.clickevent.ClickEventTask} to run the clickable code. You can use it
 * by calling {@link net.forthecrown.commands.clickevent.ClickEventManager#registerClickEvent(net.forthecrown.commands.clickevent.ClickEventTask)},
 * Which will register the given task and return the String id of it. Save that ID, you're going to need it to later.
 * </p>
 * <p></p>
 * <p>
 * Once you wanna call the task, do {@link net.forthecrown.commands.clickevent.ClickEventManager#allowCommandUsage(org.bukkit.entity.Player, boolean)},
 * to actually allow them to use it. Then create a component with a click event and as the click event, put
 * {@link net.forthecrown.commands.clickevent.ClickEventManager#getClickEvent(java.lang.String, java.lang.String...)},
 * the first string is the ID, and the second is any extra arguments you might wanna pass in.
 * </p>
 */
package net.forthecrown.commands.clickevent;