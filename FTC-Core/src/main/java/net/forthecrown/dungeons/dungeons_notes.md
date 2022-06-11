# Notes About The Dungeons
I just wanna leave a few notes here about the dungeons and what I had planned for them
before I gave all this up.  
  
## Structure
Firstly, I dislike the current Boss system. It's based around the same structure as
the entities in NMS, inheritance. I think the bosses should be more focused around
an Entity Component System or Framework. Under the current system, none of the bosses
can also be serialized, there's no code to allow for it. This will however be required
when the bosses become dynamic for the random Dungeons levels.  

## Rewards  
Secondly, I don't like the way rewards are given out right now for the bosses. I
had the idea of making a chest that appeared somewhere in each boss' room that would
have a name above it like 'Claim boss drops: 1x' or something, name would be different
for everyone and for the people that hadn't beaten the boss, there wouldn't be a chest.
  
BossLootBox is the class I tried to do this with, however I never got to testing it
or even finishing it.
  
## Evoker level
The evoker level isn't tested and needs to be modified so it could actually
be beaten by people. It shouldn't be hard or difficult, rather, it should be fun and
rewarding. The other bosses are more just slap fights of who dies first, that's why
the Evoker is very different.  
  
I showed Zama the Evoker boss and his idea was to add cover to the level, I don't know
how well that'd work, but you can try it.

## Levels
DungeonLevel was meant to be a class to expand the functionality of the levels themselves.
So that there might be a spawner view if you had to break those and other such things.
The ultimate goal was to extend them to be able to handle triggers and events to do
stuff to the level.