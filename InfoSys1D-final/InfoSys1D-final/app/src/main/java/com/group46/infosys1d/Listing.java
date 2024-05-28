

package com.group46.infosys1d;

public class Listing {
    private String listingName;
    private String listingDescription;
    private String listingPhotoURL;
    private String sellerID;
    private String ownerID;
    private String category;
    private String listingID;
    private boolean expanded;
    private String collapsedDescription;

    public String getListingDescription() {
        return listingDescription;
    }

    public String getListingName() {
        return listingName;
    }

    public String getListingPhotoURL() {
        return listingPhotoURL;
    }

    public String getSellerId() {
        return sellerID;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public String getListingID() { return listingID; }

    public String getCategory() { return category;}

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public String getCollapsedDescription() {
        return collapsedDescription;
    }

    public void setCollapsedDescription(String collapsedDescription) {
        this.collapsedDescription = collapsedDescription;
    }

//    public Object getOwnerId() {
//        return ownerID;
//    }

    // Other listing attributes

    // Constructor, getters, setters
}
