private boolean legacyFileExists() {
        File oldFile = new File("plugins/ShopsReworked/ShopData/" + fileName + ".yml");
        return oldFile.exists();
        }

private void convertLegacy() { //I have no idea
        File oldFile = new File("plugins/ShopsReworked/ShopData/" + fileName + ".yml");
        FileConfiguration oldConfig = YamlConfiguration.loadConfiguration(oldFile);

        Sign sign = (Sign) getBlock().getState();
        String line1 = sign.getLine(0).toLowerCase();

        setOwner(UUID.fromString(oldConfig.getString("Player")));

        if(line1.contains("=[buy]=")) setType(ShopType.ADMIN_BUY_SHOP);
        else if(line1.contains("=[sell]=")) setType(ShopType.ADMIN_SELL_SHOP);
        else if(line1.contains("-[sell]-")) setType(ShopType.SELL_SHOP);
        else setType(ShopType.BUY_SHOP);

        if(line1.contains(ChatColor.DARK_RED + "buy") || line1.contains(ChatColor.RED + "buy")) setOutOfStock(true);

        try {
        setPrice(Integer.parseInt(ChatColor.stripColor(sign.getLine(3)).replaceAll("[\\D]", "").replaceAll("\\$", "")));
        } catch (NullPointerException e){ setPrice(500); }

        for(ItemStack stack : (List<ItemStack>) oldConfig.getList("Inventory.content")){
        if(stack == null) continue;
        inventory.addItem(stack);
        }

        inventory.setExampleItem((ItemStack) oldConfig.getList("Inventory.shop").get(0));
        if(inventory.getExampleItem() == null){
        if(inventory.getShopContents().size() > 0) getInventory().setExampleItem(inventory.getShopContents().get(0));
        }

        oldFile.delete();

        save();
        reload();
        }