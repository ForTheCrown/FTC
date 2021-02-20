package net.forthecrown.pirates.auctions;

public enum AuctionType {
    ADMIN ("&b=[Auction]=", "&c=[Auction]="),
    NORMAL ("&a=[Auction]=", "&4=[Auction]=");

    private final String label;
    private final String unclaimedLabel;
    AuctionType(String label, String unclaimedLabel){
        this.label = label;
        this.unclaimedLabel = unclaimedLabel;
    }

    public String getUnclaimedLabel() {
        return unclaimedLabel;
    }

    public String getLabel() {
        return label;
    }
}
