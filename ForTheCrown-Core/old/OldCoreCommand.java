@Override
    public boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) throws CrownException {
        if(args.length < 1) return false;

        switch (args[0]){
            case "resetcrown":
                Scoreboard scoreboard = FtcCore.getInstance().getServer().getScoreboardManager().getMainScoreboard();
                scoreboard.getObjective("crown").unregister();
                scoreboard.registerNewObjective("crown", "dummy");
                return true;

            case "save":
                if(args.length < 2){
                    FtcCore.saveFTC();
                    sender.sendMessage("FTC-Core has been saved");
                    return true;
                }

                switch (args[1]){
                    default: return false;
                    case "config":
                        FtcCore.getInstance().saveConfig();
                        sender.sendMessage("Main config has been saved");
                        return true;

                    case "balances":
                        FtcCore.getBalances().save();
                        sender.sendMessage("Balances have been saved");
                        return true;

                    case "announcer":
                        FtcCore.getAnnouncer().save();
                        sender.sendMessage("The AutoAnnouncer has been saved");
                        return true;

                    case "users":
                        for (FtcUser user : FtcCore.loadedUsers){ user.save(); }
                        sender.sendMessage("All loaded user data's have been saved");
                        return true;

                    case "signshops":
                        for(CrownSignShop shop : FtcCore.loadedShops){
                            try {
                                shop.save();
                            } catch (Exception ignored) {}
                        }
                        sender.sendMessage("All loaded SignShops have been saved");
                        return true;

                    case "blackmarket":
                        FtcCore.getBlackMarket().save();
                        sender.sendMessage("Black market saved");
                        return true;
                }

            case "reload":
                if(args.length < 2){
                    //FtcCore.reloadFTC();
                    sender.sendMessage(ChatColor.RED + "Warning! " + ChatColor.RESET + "You're about to reload one or all of the plugin's configs! Make sure you've saved!");
                    sender.sendMessage("Do /ftccore reloadconfirm to confirm");
                    return true;
                }

            case "reloadconfirm":
                if(args.length < 2){
                    FtcCore.reloadFTC();
                    sender.sendMessage("FTC-Core successfully reloaded!");
                    return true;
                }

                switch (args[1]){
                    default: return false;

                    case "config":
                        FtcCore.getInstance().reloadConfig();
                        sender.sendMessage("Main config has been reloaded");
                        return true;

                    case "balances":
                        FtcCore.getBalances().reload();
                        sender.sendMessage("Balances have been reloaded");
                        return true;

                    case "announcer":
                        FtcCore.getAnnouncer().reload();
                        sender.sendMessage("The AutoAnnouncer has been reloaded");
                        return true;

                    case "users":
                        for (FtcUser user : FtcCore.loadedUsers){ user.reload(); }
                        sender.sendMessage("All loaded user data's have been reloaded");
                        return true;

                    case "signshops":
                        for(CrownSignShop shop : FtcCore.loadedShops){ shop.reload(); }
                        sender.sendMessage("All loaded SignShops have been reloaded");
                        return true;

                    case "blackmarket":
                        FtcCore.getBlackMarket().reload();
                        sender.sendMessage("Black market reloaded");
                        return true;
                }

            case "announcer":
                if(args.length < 2) return false;

                if(args[1].contains("start")){
                    FtcCore.getAnnouncer().startAnnouncer();
                    sender.sendMessage("Announcer has been started!");
                    return true;
                } else {
                    FtcCore.getAnnouncer().stopAnnouncer();
                    sender.sendMessage("Announcer has been stopped!");
                    return true;
                }

            case "user":
                if(args.length < 3) return false;

                UUID id = FtcCore.getOffOnUUID(args[1]);
                if(id == null) throw new InvalidPlayerInArgument(sender, args[1]);
                CrownUser user = FtcCore.getUser(id);

                switch (args[2]){
                    case "addpet":
                        if(args.length < 4) return false;
                        List<String> tempList = user.getPets();
                        tempList.add(args[3]);
                        user.setPets(tempList);
                        return true;

                    case "rank":
                        if(args.length < 4 || args[3].contains("list")){
                            sender.sendMessage(args[1] + " has the following ranks: " + user.getAvailableRanks().toString());
                            return true;
                        }

                        if(args.length < 5) return false;

                        Rank rank;
                        try {
                            rank = Rank.valueOf(args[4].toUpperCase());
                        } catch (Exception e) { return false; }

                        if(args[3].contains("add")){
                            user.addRank(rank);
                            sender.sendMessage(args[4] + " was added to " + args[1]);
                        } else if (args[3].contains("remove")){
                            user.removeRank(rank);
                            sender.sendMessage(args[4] + " was removed from " + args[1]);
                        } else if(args[3].contains("set")){
                            user.setRank(rank, true);
                            sender.sendMessage(args[1] + " is now a " + rank.toString());
                        } else return false;
                        return true;

                    case "makebaron":
                        if(args.length < 4) {
                            sender.sendMessage("User isBaron: " + user.isBaron());
                            return true;
                        }

                        if(args[3].contains("true")){
                            user.setBaron(true);
                            sender.sendMessage(args[1] + " was made into a baron!");
                            return true;
                        }

                        if(args[3].contains("false")){
                            user.setBaron(false);
                            sender.sendMessage(args[1] + " is no longer a baron");
                            return true;
                        }
                        break;

                    case "canswapbranch":
                        if(args.length < 4){
                            sender.sendMessage(user.getName() + " canSwapBranch: " + user.getCanSwapBranch());
                            return true;
                        }

                        if(args[3].contains("true")){
                            user.setCanSwapBranch(true, true);
                            sender.sendMessage(args[1] + " is now allowed to swap branches");
                            return true;
                        }

                        if(args[3].contains("false")){
                            user.setCanSwapBranch(false, true);
                            sender.sendMessage(args[1] + " is no longer allowed to swap branches");
                            return true;
                        }
                        break;

                    case "branch":
                        if(args.length < 4){
                            sender.sendMessage(args[1] + "'s branch is: " + user.getBranch().toString());
                            return true;
                        }
                        try {
                            user.setBranch(Branch.valueOf(args[3].toUpperCase()));
                            sender.sendMessage(args[1] + " is now apart of the " + user.getBranch().getName());
                            return true;
                        } catch (Exception e){ return false; }

                    case "resetearnings":
                        user.resetEarnings();
                        sender.sendMessage("Earnings of user " + args[1] + " has been reset");
                        break;

                    case "totalreset":
                        user.delete();
                        user.unload();
                        sender.sendMessage("All user data for " + args[1] + " has been deleted");
                        break;

                    case "addgems":
                        if(args.length < 4) return false;

                        int gems;
                        try {
                            gems = Integer.parseInt(args[3]);
                        } catch (NullPointerException e) { return false; }

                        user.addGems(gems);
                        sender.sendMessage("Added " + gems + " gems to " + args[1]);
                        return true;
                }

            case "crownitem":
                if(!(sender instanceof Player)) throw new NonPlayerExecutor(sender);
                Player player = (Player) sender;
                if(args.length < 2) return false;

                switch (args[1]){
                    case "crown":
                        int level = 1;
                        if(args.length < 5) return false;

                        try {
                            level = Integer.parseInt(args[2]);
                        } catch (Exception e){
                            return false;
                        }

                        player.getInventory().addItem(CrownItems.getCrown(level, args[3], args[4]));
                        sender.sendMessage("You got a crown");
                        return true;

                    case "coin":
                        int amount = 100;
                        if(args.length > 2){
                            try {
                                amount = Integer.parseInt(args[2]);
                            } catch (Exception e){
                                return false;
                            }
                        }

                        player.getInventory().addItem(CrownItems.getCoins(amount));
                        sender.sendMessage("You got " + amount + " Rhines worth of coins");
                        return true;
                }
            default: return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> argList = new ArrayList<>();
        switch (args.length){
            case 1:
                argList.add("reload");
                argList.add("save");
                argList.add("announcer");
                argList.add("user");
                argList.add("resetcrown");
                argList.add("crownitem");
                break;

            case 2:
                switch (args[0]){
                    case "reloadconfirm":
                    case "save":
                        argList.add("announcer");
                        argList.add("balances");
                        argList.add("users");
                        argList.add("signshops");
                        argList.add("blackmarket");
                        argList.add("config");
                        break;
                    case "announcer":
                        argList.add("stop");
                        argList.add("start");
                        break;
                    case "user":
                        argList.addAll(getPlayerNameList());
                        break;
                    case "crownitem":
                        argList.add("crown");
                        argList.add("cutlass");
                        argList.add("royalsword");
                        argList.add("coin");
                        break;
                    default:
                        return null;
                }
                break;

            case 3:
                if(args[0].contains("user")){
                    argList.add("addpet");
                    argList.add("rank");
                    argList.add("makebaron");
                    argList.add("canswapbranch");
                    argList.add("branch");
                    argList.add("addgems");
                    argList.add("totalreset");
                    argList.add("resetearnings");
                }
                break;

            case 4:
                if(args[1].contains("crown") && args[0].contains("crownitem")){
                    argList.add("King");
                    argList.add("Queen");
                    break;
                }

                switch (args[2]){
                    case "rank":
                        argList.add("add");
                        argList.add("remove");
                        break;
                    case "branch":
                        for(Branch b : Branch.values()){
                            argList.add(b.toString().toLowerCase());
                        }
                        break;
                    case "makebaron":
                    case "canswapbranch":
                        argList.add("true");
                        argList.add("false");
                        break;
                    default:
                        return new ArrayList<>();
                }
                break;

            case 5:
                if(args[1].contains("crown")) return getPlayerNameList();
                if(args[2].contains("rank")){
                    for(Rank r : Rank.values()){
                        argList.add(r.toString().toLowerCase());
                    }
                }
                break;
        }

        return argList;
    }