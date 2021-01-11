Structure of the project:
    The project is mainly split into 3 branches, they are
        The Core, which handles most of the things the old DataPlugin handled
        Economy, which handles what ShopsReworked used to handle
        Chat... what do you think lol

    So the remnants of the old 3 plugins that were combined still exist and it's a bit confusing at times
    I did this mainly for ease of orginization, because this is a large project and having it be organized
    was big requirement for me. How I organized it is something you can lambaste me for if you wish.

In the main class, which you can do FtcCore.getInstance() to get, there are several getters, like getEconomy or getChat to get access to these different branches.
I just realized you could Chat.getInstance() as well, so you know, the methods in the main file are there if you wanna use em lol
FtcCore also has some useful methods like makeItem and getRandomNumberInRange, they're static methods so just FtcCore.makeItem(args n stuff) to use them

As of now the TODO list is the following:
    SignShops - fix the SignShop class itself, there's issues with reloading and saving, not to mention the non existent convertLegacyFiles method
    SignShopEvents - The class is a joke rn, I don't think anything except the SignChangeEvent works there
    FtcUser - the setAmountSold method should handle the itemPrice changing itself, maybe with something like a configurePrices method, idk
    Commands - There are still commands needed, stuff like setbranch and getbranch which were in the old DataPlugin. I don't actually know if they're needed or not
    Economy - As I understand it, the pirate blackmarket stuff is also handled in ShopsReworked, so I'll probably have to do something with that as well
    ShopCommand, SellShop and SellShopEvents - these all relate to the usage /shop and need to be tested and fixed, because there's 100% bugs in them, when aren't there

    Comments - The code is devoid of comments for the most part, the comments that exist are just me screaming in confusion to myself, and me being angry at being stupid
        I'm also counting the CommandDescription things we did as part of the comments, because I haven't added any, except for some commands in the Chat branch