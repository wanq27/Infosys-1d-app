package com.group46.infosys1d;

import android.widget.ImageView;

public class UsersLikesModel {
    private String UserID; //to edit once likes class is done
    private static String ItemTitle;

    private static String ItemDescription;

    private ImageView UsersImage;

    public ImageView getUsersImage() {
        return UsersImage;
    }

    public void setUsersImage(ImageView usersImage) {
        UsersImage = usersImage;
    }

    public String getUserID() {
        return UserID;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }

    public static String getItemTitle() {
        return ItemTitle;
    }

    public void setItemTitle(String itemTitle) {
        ItemTitle = itemTitle;
    }

    public static String getItemDescription() {
        return ItemDescription;
    }

    public void setItemDescription(String itemDescription) {
        ItemDescription = itemDescription;
    }
}