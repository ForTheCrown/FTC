/**
 * Gets a score in an objective
 *
 * The 'entry' parameter can be a string (Treated as entry name), a player, 
 * user, or entity
 *
 * @param objective Scoreboard objective name
 * @param entry Entry's name or entry object
 * 
 * @returns Entry's score, or undefined, if the objective doesn't exist
 */
declare function getScore(objective: string, entry: any): number | undefined

/**
 * Adds to an entry's score.
 * 
 * The 'entry' parameter can be a string (Treated as entry name), a player, 
 * user, or entity
 * 
 * @param objective Scoreboard objective name
 * @param entry Entry's name
 * @param amount Amount to add
 * 
 * @returns 'true' if the objective existed, 'false' otherwise.
 */
declare function addScore(objective: string, entry: any, amount: number): boolean

/**
 * Removes from an entry's score.
 * 
 * The 'entry' parameter can be a string (Treated as entry name), a player, 
 * user, or entity
 * 
 * @param objective Scoreboard objective name
 * @param entry Entry's name
 * @param amount Amount to remove
 * 
 * @returns 'true' if the objective existed, 'false' otherwise.
 */
declare function removeScore(objective: string, entry: any, amount: number): boolean

/**
 * Sets an entry's score.
 * 
 * The 'entry' parameter can be a string (Treated as entry name), a player, 
 * user, or entity
 * 
 * @param objective Scoreboard objective name
 * @param entry Entry's name
 * @param amount New score
 * 
 * @returns 'true' if the objective existed, 'false' otherwise.
 */
declare function setScore(objective: string, entry: any, amount: number): boolean

/**
 * Deletes an entry's score.
 * 
 * The 'entry' parameter can be a string (Treated as entry name), a player, 
 * user, or entity
 * 
 * @param objective Scoreboard objective name
 * @param entry Entry's name
 * 
 * @returns 'true' if the objective existed, 'false' otherwise.
 */
declare function deleteScore(objective: string, entry: any): boolean

/**
 * Tests if an objective exists
 * @param objective Scoreboard objective name
 * @returns 'true' if the objective exists, 'false' otherwise
 */
declare function objectiveExists(objective: string): boolean

/**
 * Defines an objective
 * 
 * @param name 
 * @param criteria 
 * 
 * @returns 'true' if the objective was defined, 'false' if it already existed
 */
declare function defineObjective(name: string, criteria: string): boolean

/**
 * Deletes an objective
 * @param objective Scoreboard objective name
 * @returns 'true' if the objective existed and was deleted, 'false' if the 
 *          objective didn't exist
 */
declare function deleteObjective(objective: string): boolean

/**
 * Lists the names of all scoreboard objectives
 */
declare function listObjectives(): string[]

/**
 * Sets the display name of an objective
 * 
 * The 'displayName' parameter can be a string, number, boolean (Treated as 
 * literals) or an object/array (Converted to JSON and parsed back into a chat 
 * component), or a text component directly.
 * 
 * @param objective Scoreboard objective name
 * @param displayName Display name
 * @returns 'true' if the objective exists, 'false' otherwise
 */
declare function setDisplayName(objective: string, displayName: any): boolean

/**
 * Sets the render type of an objective.
 * @param objective Scoreboard objective name
 * @param renderType Render type (One of: 'numbers', 'hearts')
 * @returns 'true' if the objective exists, 'false' otherwise
 */
declare function setRenderType(objective: string, renderType: string): boolean

/**
 * Sets an objective's display slot
 * @param objective Scoreboard objective name
 * @param slot Display slot value, or 'null' to remove
 * @returns 'true' if the objective was found and changed, 'false' otherwise
 */
declare function setDisplaySlot(objective: string, slot: string | null): boolean

/**
 * Gets an objective's display name
 * @param objective Scoreboard objective name
 * @returns 'null' if the objective wasn't found, otherwise returns 
 *          the display name as a JS object
 */
declare function getDisplayName(objective: string): null | object

/**
 * Gets an objective's render type
 * @param objective Scoreboard objective name
 * @returns 'null' if the objective wasn't found, or the render type
 */
declare function getRenderType(objective: string): null | string

/**
 * Gets an objective's display slot
 * @param objective Scoreboard objective name
 * @returns 'null', if the objective has no set display slot, 'undefined' if the 
 *          objective doesn't exist, or the display slot name
 */
declare function getDisplaySlot(objective: string): null | undefined | string
