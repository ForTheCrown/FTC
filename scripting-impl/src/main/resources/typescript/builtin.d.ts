
/**
 * If the value is a string, then it will be treated as a player's name/nickname
 * or string UUID, unless the string value is 'console' or 'server', then it 
 * refers to the console. 
 * 
 * This parameter can also be a player, user or UUID in the from of a wrapped 
 * Java Object
 */
type Audience = any;

type InventoryHolder = Audience | any;

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
declare function runAs(source: Audience | null, ...command: any): number

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
declare function giveItem(target: InventoryHolder, ...items: any): void;

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
 * @param renderer Already existing placeholder renderer to use (Can be either 
 *                 be a renderer or the render context)
 * 
 * @returns A java text component.
 */
declare function renderPlaceholders(
  text: any,
  viewer?: Audience,
  placeholders?: object,
  renderer?: any
): any;

/**
 * Sends a message to a target.
 * 
 * The text parameter will be converted to a chat component. If it's an object,
 * it'll be turned to JSON and parsed from JSON back into a component, 
 * otherwise, it's simply toString'ed back to a component. If the parameter is
 * a chat component, it'll simply be sent.
 * 
 * @param target Message target
 * @param text Texts to send
 */
declare function sendMessage(target: Audience, ...text: any): void;

/**
 * Sends an action bar message to a target.
 * 
 * The text parameter will be converted to a chat component. If it's an object,
 * it'll be turned to JSON and parsed from JSON back into a component, 
 * otherwise, it's simply toString'ed back to a component. If the parameter is
 * a chat component, it'll simply be sent.
 * 
 * @param target Message target
 * @param text Text to send
 */
declare function sendActionBar(target: Audience, text: any): void

/**
 * Plays a sound for a target.
 * 
 * @param target Target to play the sound for
 * @param sound Sound's namespaced ID
 * @param volume Volume, from 0 to 2, defaults to 1
 * @param pitch Pitch, from 0 to 2, defaults to 1
 * @param category Sound category, defaults to 'master'
 */
declare function playSound(
  target: Audience, 
  sound: string, 
  volume?: number, 
  pitch?: number, 
  category?: string
): void;

declare interface Vector<S> {
  
  add(val: number | S): S;

  sub(val: number | S): S;

  mul(val: number | S): S;

  div(val: number | S): S;

  length(): number;

  lengthSquared(): number;

  distance(val: Vector3d): number;

  distanceSquared(val: Vector3d): number;
}

declare interface Vector3d extends Vector<Vector3d> {
  x(): number;
  y(): number;
  z(): number;
}

declare interface Vector2d extends Vector<Vector2d>  {
  x(): number;
  y(): number;
}

/**
 * Linearly interpolates between 2 specified points. Parameters must be of the 
 * same type, else an error is thrown
 * 
 * @param v1 Point 1
 * @param v2 Point 2
 * @param progress Progress from point1 to point2 in range 0.0 to 1.0
 */
declare function lerp<S>(progress: number, v1: Vector<S> | number, v2: Vector<S> | number): S

/**
 * Creates a three dimensional vector out of a 2 dimensional vector.
 * 
 * Result:
 * x = v2.x
 * y = v2.y
 * z = z parameter
 * 
 * @param v2 Vec2
 * @param z Z component
 */
declare function vec3xy(v2?: Vector2d, z?: number): Vector3d;

/**
 * Creates a three dimensional vector out of a 2 dimensional vector.
 * 
 * Result:
 * x = v2.x
 * y = y parameter
 * z = v2.y
 * 
 * @param v2 Vec2
 * @param y Y component
 */
declare function vec3xz(v2?: Vector2d, y?: number): Vector3d;

/**
 * Creates a three dimensional vector. If any parameters are left unspecified,
 * they default to 0
 * 
 * @param x X component
 * @param y Y component
 * @param z Z component
 */
declare function vec3(x?: number, y?: number, z?: number): Vector3d;

/**
 * Creates a two dimensional vector out of the input's X and Y components
 * @param v3 Three dimensional vector
 */
declare function vec2xy(v3?: Vector3d): Vector2d;

/**
 * Creates a two dimensional vector out of the input's X and Z components
 * @param v3 Three dimensional vector
 */
declare function vec2xz(v3?: Vector3d): Vector2d;

/**
 * Creates a two dimensional vector. Any parameters left unspecified will 
 * default to 0
 * 
 * @param x X component
 * @param y Y component
 */
declare function vec2(x?: number, y?: number): Vector2d;

type TaskCallback = (task: ScheduledTask) => void;
type EventCallback = (event: any) => void;

interface EventHandler {

  /**
   * Registers an event listener.
   * 
   * For information about the event priority:
   * {@link https://bukkit.fandom.com/wiki/Event_API_Reference#Event_Priorities | Bukkit Event API}
   * 
   * @param eventClass Java event class
   * @param callbackfn Event callback
   * @param ignoreCancelled 'true' to not execute the callback function if the 
   *                        event was cancelled, 'false' otherwise
   * @param priority Event listener priority
   */
  register(
    eventClass: any, 
    callbackfn: EventCallback, 
    ignoreCancelled?: boolean, 
    priority?: string
  ): void;

  /**
   * Unregisters all listeners registered by the script
   */
  unregisterAll(): void;

  /**
   * Unregisters all listeners from the specified event class
   * @param eventClass Java Event class
   */
  unregisterFrom(eventClass: any): void;
}

interface Scheduler {

  /**
   * Executes a task
   * @param callbackfn Task callback
   */
  run(callbackfn: TaskCallback): ScheduledTask

  /**
   * Schedules a delayed task
   * @param delayTicks Execution delay, in ticks
   * @param callbackfn Task callback
   */
  runLater(delayTicks: number, callbackfn: TaskCallback): ScheduledTask

  /**
   * Schedules a task to be executed at a regular interval
   * @param delayTicks Initial execution delay, in ticks
   * @param intervalTicks Exectution interval delay, in ticks
   * @param callbackfn Task callback
   */
  runTimer(delayTicks: number, intervalTicks: number, callbackfn: TaskCallback): ScheduledTask
}

interface ScheduledTask {
  readonly cancelled: boolean;

  /** Executes the task */
  run(): void;
  
  /** Stops the task from being executed */
  cancel(): void;
}

declare const events: EventHandler;
declare const scheduler: Scheduler;