
//Everything relating to a specific user
                .then(literal("user")
                        .then(argument(USER_ARG, UserArgument.user())

                        //Save the user's data
                        .then(literal("save")
                        .executes(c -> {
                        CrownUser u = getUser(c);
                        u.save();
                        c.getSource().sendAdmin( "Saved data of " + u.getName());
                        return 0;
                        })
                        )

                        //Reload the user's data
                        .then(literal("reload")
                        .executes(c -> {
                        CrownUser u = getUser(c);
                        u.reload();
                        c.getSource().sendAdmin( "Reloaded data of " + u.getName());
                        return 0;
                        })
                        )

                        .then(CommandLore.compOrStringArg(literal("prefix"), (c, b) -> Suggestions.empty(), (c, prefix) -> {
                        CrownUser user = getUser(c);

                        user.setCurrentPrefix(prefix);

                        c.getSource().sendMessage(
                        Component.text("Set ")
                        .append(user.displayName())
                        .append(Component.text("'s prefix to be "))
                        .append(prefix)
                        );
                        return 0;
                        }))

                        //This alt shit can go fuck itself
                        .then(literal("alt")
                        .then(literal("list")
                        .executes(c -> {
                        CrownUser user = getUser(c);
                        UserManager um = Crown.getUserManager();

                        c.getSource().sendMessage(
                        Component.text(user.getName() + "'s alt accounts:")
                        .append(Component.newline())
                        .append(Component.text(um.getAlts(user.getUniqueId()).toString()))
                        );

                        return 0;
                        })
                        )
                        .then(literal("for")
                        .then(argument("altFor", UserArgument.user())

                        .executes(c -> {
                        CrownUser user = getUser(c);
                        CrownUser main = c.getArgument("altFor", UserParseResult.class).getUser(c.getSource(), false);
        UserManager um = Crown.getUserManager();

        um.addEntry(user.getUniqueId(), main.getUniqueId());
        user.unload();
        CrownUserAlt alt = new FtcUserAlt(user.getUniqueId(), main.getUniqueId());
        alt.save();

        c.getSource().sendAdmin( alt.getName() + " is now an alt for " + main.getName());
        return 0;
        })
        )
        )
        .then(literal("for_none")
        .executes(c -> {
        CrownUser user = getUser(c);
        UserManager um = Crown.getUserManager();

        if(!um.isAlt(user.getUniqueId())) throw FtcExceptionProvider.create(user.getName() + " is not an alt");
        um.removeEntry(user.getUniqueId());

        c.getSource().sendAdmin( user.getName() + " is no longer an alt");
        return 0;
        })
        )
        )

        .then(literal("married")
        .executes(c -> {
        CrownUser user = getUser(c);
        UserInteractions inter = user.getInteractions();

        if(inter.getSpouse() == null) throw FtcExceptionProvider.create(user.getName() + " is not married");

        c.getSource().sendMessage(
        Component.text()
        .append(user.displayName())
        .append(Component.text(" is married to "))
        .append(UserManager.getUser(inter.getSpouse()).displayName())
        .append(Component.text("."))
        .build()
        );
        return 0;
        })

        .then(literal("resetCooldown")
        .executes(c -> {
        CrownUser user = getUser(c);
        user.getInteractions().setLastMarriageChange(0L);

        c.getSource().sendAdmin(
        Component.text("Reset cooldown of ")
        .append(user.displayName())
        );
        return 0;
        })
        )

        .then(literal("divorce")
        .executes(c -> {
        CrownUser user = getUser(c);

        if(user.getInteractions().getSpouse() == null) throw FtcExceptionProvider.create("User is not married");

        CrownUser spouse = UserManager.getUser(user.getInteractions().getSpouse());

        spouse.getInteractions().setSpouse(null);
        user.getInteractions().setSpouse(null);

        c.getSource().sendAdmin(
        Component.text("Made ")
        .append(user.displayName())
        .append(Component.text(" divorce"))
        );
        return 0;
        })
        )

        .then(argument("target", UserArgument.user())
        .executes(c -> {
        CrownUser user = getUser(c);
        CrownUser target = UserArgument.getUser(c, "target");

        if(user.getUniqueId().equals(target.getUniqueId())) throw FtcExceptionProvider.create("Cannot make people marry themselves lol");

        user.getInteractions().setSpouse(target.getUniqueId());
        target.getInteractions().setSpouse(user.getUniqueId());

        c.getSource().sendAdmin(
        Component.text("Married ")
        .append(user.displayName())
        .append(Component.text(" to "))
        .append(target.displayName())
        );
        return 0;
        })
        )
        )

        .then(literal("balance")
        .executes(c-> { //Shows the balance
        CrownUser user = getUser(c);
        c.getSource().sendMessage(user.getName() + " has " + FtcFormatter.getRhines(bals.get(user.getUniqueId())) + " Rhines");
        return 0;
        })

        //Sets the balance
        .then(literal("set")
        .then(argument("sAmount", IntegerArgumentType.integer(0, maxMoney))
        .executes(c -> {
        CrownUser user = getUser(c);

        int amount = c.getArgument("sAmount", Integer.class);
        bals.set(user.getUniqueId(), amount);

        c.getSource().sendAdmin( "Set " + user.getName() + "'s balance to " + FtcFormatter.getRhines(bals.get(user.getUniqueId())));
        return 0;
        })
        )
        )
        //Adds to the balance
        .then(literal("add")
        .then(argument("aAmount", IntegerArgumentType.integer(1, maxMoney))
        .executes(c -> {
        CrownUser user = getUser(c);

        int amount = c.getArgument("aAmount", Integer.class);
        bals.add(user.getUniqueId(), amount, false);

        c.getSource().sendAdmin( "Added " + amount + " to " + user.getName() + "'s balance.");
        c.getSource().sendAdmin( "Now has " + FtcFormatter.getRhines(bals.get(user.getUniqueId())));
        return 0;
        })
        )
        )
        //Removes from the balance
        .then(literal("remove")
        .then(argument("rAmount", IntegerArgumentType.integer(1, maxMoney))
        .executes(c -> {
        CrownUser user = getUser(c);

        int amount = c.getArgument("rAmount", Integer.class);
        bals.add(user.getUniqueId(), -amount, false);

        c.getSource().sendAdmin( "Removed " + amount + " from " + user.getName() + "'s balance.");
        c.getSource().sendAdmin( "Now has " + FtcFormatter.getRhines(bals.get(user.getUniqueId())));
        return 0;
        })
        )
        )
        //Resets the balance, removes it from the BalanceMap so it doesn't take up as much data
        .then(literal("reset")
        .executes(c -> {
        CrownUser user = getUser(c);

        Economy bals = Crown.getEconomy();
        BalanceMap balMap = bals.getMap();

        balMap.remove(user.getUniqueId());
        bals.setMap((SortedBalanceMap) balMap);

        c.getSource().sendAdmin( "Reset balance of " + user.getName());
        return 0;
        })
        )
        )

        .then(literal("afk")
        .executes(c -> {
        CrownUser user = getUser(c);

        c.getSource().sendMessage(
        Component.text()
        .append(user.displayName())
        .append(Component.text(" is AFK: "))
        .append(Component.text(user.isAfk()))
        .build()
        );
        return 0;
        })

        .then(argument("bool", BoolArgumentType.bool())
        .executes(c -> {
        CrownUser user = getUser(c);
        boolean bool = c.getArgument("bool", Boolean.class);

        user.setAfk(bool, null);

        c.getSource().sendMessage(
        Component.text("Set ")
        .append(user.displayName())
        .append(Component.text(" afk: "))
        .append(Component.text(bool))
        );
        return 0;
        })
        )
        )

        .then(literal("baron")
        .then(argument("isBaron", BoolArgumentType.bool())
        .suggests(suggestMatching("true", "false"))

        .executes(c ->{
        CrownUser user = getUser(c);
        boolean isBaron = c.getArgument("isBaron", Boolean.class);
        if(user.isBaron() == isBaron) throw FtcExceptionProvider.create(user.getName() + "'s baron value is the same as entered!");

        user.setBaron(isBaron);
        c.getSource().sendMessage(user.getName() + " isBaron " + user.isBaron());
        return 0;
        })
        )
        .executes(c ->{
        CrownUser user = getUser(c);
        c.getSource().sendMessage(user.getName() + " is baron: " + user.isBaron());
        return 0;
        })
        )
        .then(literal("pets")
        .then(literal("list")
        .executes(c -> {
        CrownUser user = getUser(c);

        net.minecraft.network.chat.Component component = new TextComponent(user.getName() + "'s pets: " +
        ListUtils.join(user.getPets(), pet -> pet.toString().toLowerCase())
        );

        c.getSource().sendMessage(PaperAdventure.asAdventure(component));
        return 0;
        })
        )
        .then(argument("pet", PetArgument.PET)

        .then(literal("add")
        .executes(c -> {
        CrownUser user = getUser(c);
        Pet pet = c.getArgument("pet", Pet.class);

        if(user.hasPet(pet)) throw FtcExceptionProvider.create(user.getName() + " already has that pet");

        user.addPet(pet);
        c.getSource().sendAdmin( "Added " + pet.toString() + " to " + user.getName());
        return 0;
        })
        )
        .then(literal("remove")
        .executes(c -> {
        CrownUser user = getUser(c);
        Pet pet = c.getArgument("pet", Pet.class);

        if(!user.hasPet(pet)) throw FtcExceptionProvider.create(user.getName() + " doesns't have that pet");

        user.removePet(pet);
        c.getSource().sendAdmin( "Removed " + pet.toString() + " from " + user.getName());
        return 0;
        })
        )
        )
        )
        .then(literal("rank")
        .executes(c -> {
        CrownUser user = getUser(c);
        c.getSource().sendMessage(user.getName() + "'s rank is " + user.getRank().getPrefix());
        return 0;
        })

        .then(literal("set")
        .then(argument("rankToSet", FtcCommands.RANK)
        .executes(c -> {
        CrownUser user = getUser(c);
        Rank rank = c.getArgument("rankToSet", Rank.class);

        user.setRank(rank, true);

        c.getSource().sendAdmin("Set rank of " + user.getName() + " to " + rank.getPrefix());
        return 0;
        })
        )
        )

        .then(literal("list")
        .executes(c -> {
        CrownUser user = getUser(c);
        c.getSource().sendMessage(user.getName() + "'s ranks: " + ListUtils.join(user.getAvailableRanks(), r -> r.name().toLowerCase()));
        return 0;
        })
        )
        .then(literal("add")
        .then(argument("rankToAdd", FtcCommands.RANK)
        .executes(c ->{
        CrownUser user = getUser(c);
        Rank rank = c.getArgument("rankToAdd", Rank.class);

        user.addTitle(rank);
        c.getSource().sendAdmin( user.getName() + " now has " + rank.getPrefix());
        return 0;
        })
        )
        )
        .then(literal("remove")
        .then(argument("rankToRemove", FtcCommands.RANK)
        .executes(c ->{
        CrownUser user = getUser(c);
        Rank rank = c.getArgument("rankToRemove", Rank.class);

        user.removeRank(rank);
        c.getSource().sendAdmin( user.getName() + " no longer has " + rank.getPrefix());
        return 0;
        })
        )
        )
        )
        .then(literal("branch")
        .executes(c ->{
        CrownUser user = getUser(c);
        c.getSource().sendMessage(user.getName() + "'s branch is " + user.getFaction().toString());
        return 0;
        })
        .then(argument("branchToSet", FtcCommands.BRANCH)
        .executes(c ->{
        CrownUser user = getUser(c);
        Faction faction = c.getArgument("branchToSet", Faction.class);
        user.setFaction(faction);
        c.getSource().sendAdmin( user.getName() + " is now a " + user.getFaction().getSingularName());
        return 0;
        })
        )
        )
        .then(literal("addgems")
        .then(argument("gemAmount", IntegerArgumentType.integer())
        .executes(c ->{
        CrownUser user = getUser(c);
        int gems = c.getArgument("gemAmount", Integer.class);

        user.addGems(gems);
        c.getSource().sendAdmin( user.getName() + " now has " + user.getGems() + " gems");

        return 0;
        })
        )
        )
        .then(literal("resetearnings")
        .executes(c ->{
        CrownUser u = getUser(c);
        u.resetEarnings();
        c.getSource().sendAdmin( u.getName() + " earnings reset");
        return 0;
        })
        )
        .then(literal("delete")
        .executes(c ->{
        CrownUser user = getUser(c);

        user.delete();
        user.unload();
        c.getSource().sendAdmin(user.getName() + "'s user data has been deleted");
        return 0;
        })
        )
        )
        )
        .then(literal("announcer")
        .then(literal("start").executes(c -> announcerThing(c, true)))
        .then(literal("stop").executes(c -> announcerThing(c, false)))
        .then(literal("announce_all")
        .executes(c -> {
        Announcer announcer = Crown.getAnnouncer();

        for (Component comp: announcer.getAnnouncements()){
        announcer.announce(comp);
        }

        c.getSource().sendAdmin( "All announcements have been broadcast");
        return 0;
        })
        )
        )