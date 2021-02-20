package net.forthecrown.pirates.auctions;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public final class AuctionManager {

    private static final Map<String, PirateAuctionShop> auctions = new HashMap<>();

    public AuctionManager(){

    }

    public void registerAuction(String id, PirateAuctionShop auctionShop){
        auctions.put(id, auctionShop);
    }

    public void unregisterAuction(String id){
        auctions.remove(id);
    }

    public PirateAuctionShop getAuction(String id) throws NullPointerException{
        if(auctions.get(id) != null) return auctions.get(id);
        return null;
    }

    public PirateAuctionShop createAuction(Location location, AuctionType type, Integer startingBid){
        return new PirateAuctionShop(location, type, startingBid);
    }
}
