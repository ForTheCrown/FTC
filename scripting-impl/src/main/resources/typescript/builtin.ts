/**
 * Runs a command as console
 * @param args Command string. If multiple arguments are specified, they are 
 *             concatenated
 */
declare function command(...args: any): number

/**
 * Runs a command as a specified source
 * @param source Command source
 * @param command Command string. If multiple arguments are specified, they are 
 *                concatenated
 */
declare function runAs(source: any | undefined, ...command: any): number

/**
 * Gets the current time in milliseconds. Identical to java's 
 * "System.currentTimeMillis()"
 * @returns Current time in milliseconds
 */
declare function currentTimeMillis(): number

/**
 * Gets the current time in seconds. The returned number is a floating point 
 * value where milliseconds come after the decimal.
 *
 * Use Math.floor to get rid of the millisecond digits
 *
 * @returns Current time in seconds
 */
declare function currentTimeSeconds(): number

/**
 * Gives all specified items to a specified target.
 *
 * Target can be either a string (Treated as a player's name), UUID (Treated as
 * a player's or entity's UUID), or any inventory holder type (Including a 
 * bukkit block)
 *
 * Any item in the items array can be a string (Treated as string item NBT) or 
 * an NBT object (Loaded into an item) or an ItemStack itself.
 *
 * @param target Target the items are given to.
 * @param items Items given to the target
 */
declare function giveItem(target: any, ...items: any): void;

/**
 * Renders any placeholders in the specified text.
 * 
 * The 'text' parameter will be converted into text no matter the input given.
 * 
 * Each of the keys tine the 'placeholders' parameter will be added to the list
 * of placeholders that may be rendered. If a value of a key in that object is
 * a function, that function will be called to render the placeholder. The 
 * function will also be given 2 parameter values: 'match' and 'context'. 
 * 
 * Those values are identical to the values regular 
 * 'net.forthecrown.text.placeholder.TextPlaceholder' implementations are given.
 * 
 * @param text Value to render placeholders inside of.
 * @param viewer Text viewer
 * @param placeholders Any special placeholders to render.
 * 
 * @returns A java text component.
 */
declare function renderPlaceholders(text: any, viewer: any | undefined, placeholders: object | undefined): any;

/**
 * Sends a message to a target.
 * 
 * If the 'target' parameter is a string, then it will be treated as a player's
 * name/nickname or string UUID, unless the string value is 'console' or 
 * 'server', then it refers to the console. This parameter can also be a player,
 * user or UUID.
 * 
 * The text parameter will be converted to a chat component. If it's an object,
 * it'll be turned to JSON and parsed from JSON back into a component, 
 * otherwise, it's simply toString'ed back to a component. If the parameter is
 * a chat component, it'll simply be sent.
 * 
 * @param target Message target
 * @param text Texts to send
 */
declare function sendMessage(target: any, ...text: any): void;

/**
 * Sends an action bar message to a target.
 * 
 * If the 'target' parameter is a string, then it will be treated as a player's
 * name/nickname or string UUID, unless the string value is 'console' or 
 * 'server', then it refers to the console. This parameter can also be a player,
 * user or UUID.
 * 
 * The text parameter will be converted to a chat component. If it's an object,
 * it'll be turned to JSON and parsed from JSON back into a component, 
 * otherwise, it's simply toString'ed back to a component. If the parameter is
 * a chat component, it'll simply be sent.
 * 
 * @param target Message target
 * @param text Text to send
 */
declare function sendActionBar(target: any, text: any): void

type TaskCallback = (task: ScheduledTask) => void;
type EventCallback = (event: any) => void;

interface EventHandler {
  register(eventClass: any, callbackfn: EventCallback);
}

interface Scheduler {
  run(callbackfn: TaskCallback): ScheduledTask

  runLater(delayTicks: number, callbackfn: TaskCallback): ScheduledTask

  runTimer(delayTicks: number, intervalTicks: number, callbackfn: TaskCallback): ScheduledTask
}

interface ScheduledTask {
  readonly cancelled: boolean;

  run(): void;
  
  cancel(): void;
}

declare const events: EventHandler;
declare const scheduler: Scheduler;