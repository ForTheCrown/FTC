private boolean legacyDataExists(){
        File oldFile = new File("plugins/DataPlugin/config.yml");
        if(!oldFile.exists()) return false;
        FileConfiguration oldYaml = YamlConfiguration.loadConfiguration(oldFile);
        return (oldYaml.get("players." + base.toString()) != null);
        }

private void convertLegacy(){
        File oldFile = new File("plugins/DataPlugin/config.yml");
        FileConfiguration oldYamlFile = YamlConfiguration.loadConfiguration(oldFile);
        ConfigurationSection oldYaml = oldYamlFile.getConfigurationSection("players." + getBase().toString());

        setRank(Rank.valueOf(oldYaml.getString("CurrentRank").toUpperCase()), false);
        setCanSwapBranch(oldYaml.getBoolean("CanSwapBranch"), false);
        setPets(oldYaml.getStringList("Pets"));
        setAllowsRidingPlayers(oldYaml.getBoolean("AllowsRidingPlayers"));
        setDeathParticle(oldYaml.getString("ParticleDeathActive"));
        setParticleDeathAvailable(oldYaml.getStringList("ParticleDeathAvailable"));
        setGems(oldYaml.getInt("Gems"));

        if(oldYaml.get("ParticleArrowActive") != null && !oldYaml.getString("ParticleArrowActive").contains("none")) setArrowParticle(Particle.valueOf(oldYaml.getString("ParticleArrowActive")));

        //branch conversion
        if(oldYaml.getString("ActiveBranch").contains("Knight")) setBranch(Branch.ROYALS);
        else setBranch(Branch.valueOf(oldYaml.getString("ActiveBranch").toUpperCase() + "S"));

        //Rank conversion
        Set<Rank> tempList = new HashSet<>();
        if(oldYaml.getList("PirateRanks") != null && oldYaml.getList("PirateRanks").size() > 0){
        for (String s : oldYaml.getStringList("PirateRanks")){
        try { tempList.add(Rank.valueOf(s.toUpperCase() + "S"));
        } catch (Exception ignored){}
        }
        }
        if(oldYaml.getList("KnightRanks") != null && oldYaml.getList("KnightRanks").size() > 0){
        for (String s : oldYaml.getStringList("KnightRanks")){
        if(s.toLowerCase().contains("baron")) addRank(Rank.BARONESS);

        try { tempList.add(Rank.valueOf(s.toUpperCase()));
        } catch (Exception ignored){}
        }
        }
        tempList.add(Rank.DEFAULT);
        setAvailableRanks(tempList);
        setBranch(getRank().getRankBranch());

        if(oldYaml.getList("ParticleArrowAvailable") != null){
        List<Particle> tempList1 = new ArrayList<>();
        for (String s : oldYaml.getStringList("ParticleArrowAvailable")){
        try { tempList1.add(Particle.valueOf(s.toUpperCase()));
        } catch (Exception ignored){}
        }
        setParticleArrowAvailable(tempList1);
        }

        if(oldYaml.getStringList("EmotesAvailable").size() > 0){
        for (String s : oldYaml.getStringList("EmotesAvailable")){
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + getName() + " permission set ftc.emotes." + s.toLowerCase());
        }
        }

        oldYamlFile.set("players." + getBase().toString(), null);
        try {
        oldYamlFile.save(oldFile);
        } catch (IOException e) {
        e.printStackTrace();
        }

        addRank(Rank.DEFAULT);

        permsCheck();
        save();
        }